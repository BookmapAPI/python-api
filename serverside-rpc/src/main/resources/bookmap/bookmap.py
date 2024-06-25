import io
import json
import queue
import sys
import threading
import time
import typing
import socket
import traceback
import select

from os.path import exists
from socket import socket, AF_INET, SOCK_STREAM
from .sortedcontainers import SortedDict
from .dto import OrderSendParameters

BUF_SIZE = 1024
LOCAL_PROCESS = 0
UNIX_SOCKET = 1
TCP_SOCKET = 2
FIELD_SEPARATOR = "\uE000"
WAITING_DATA_TIMEOUT_IN_SECONDS = 2
AF_UNIX = 1

# addon status
RUN = False

MSGS_BUFFER = bytearray("", "utf-8")

# MSG TYPES
CLIENT_INIT = "0"
SERVER_INIT = "1"
INSTRUMENT_INFO = "2"
REQ_DATA = "3"
RESP_DATA = "4"
TRADE = "5"
BAR = "6"
INSTRUMENT_DETACHED = "7"
DEPTH = "8"
REGISTER_INDICATOR = "9"
INDICATOR_RESPONSE = "10"
ADD_POINT_TO_INDICATOR = "11"
ON_INTERVAL = "12"
FINISHED_INITIALIZATION = "13"
ADD_SETTING_FIELD = "14"
ON_SETTINGS_PARAMETER_CHANGED = "15"
MBO = "18"
SEND_ORDER = "19"
EXECUTE_ORDER = "20"
UPDATE_ORDER = "21"
ORDER_INFO = "22"
CANCEL_ORDER = "23"
MOVE_ORDER = "24"
MOVE_ORDER_TO_MARKET = "25"
RESIZE_ORDER = "26"
BALANCE_UPDATE = "27"
POSITION_UPDATE = "28"
BROADCASTING = "29"
REGISTER_BROADCASTING_PROVIDER = "30"
PROVIDERS_STATUS = "31"
BROADCASTING_SETTINGS = "32"
SEND_USER_MESSAGE = "33"
ERROR = "-1"

counter_lock = threading.Lock()
event_counter = 0

exit_code = 0


def _connect_as_local_process() -> (io.TextIOWrapper, io.TextIOWrapper):
    # local process communicates via std.in and sdt.out, to allow to use 'print' method
    # without writing to the server, std.in and std.out for this process is replaced by
    # in,out of the debug file
    inp, out = sys.stdin, sys.stdout
    return inp, out


def _connect_as_unix_socket_client(socket_file) -> socket:
    if not exists(socket_file):
        raise Exception("No unix socket file, did server initialized it?")
    sock = socket(AF_UNIX, SOCK_STREAM)
    sock.connect(socket_file)
    return sock


def _connect_as_tcp_socket_client(port: int) -> socket:
    sock = socket(AF_INET, SOCK_STREAM)
    sock.connect(('127.0.0.1', port))
    return sock


# reading shall be done inside one thread
def _start_reading_task(inpt: object) -> typing.Tuple[threading.Thread, queue.Queue]:
    msg_queue = queue.Queue()
    running_thread = threading.Thread(
        target=_run_task_until_stop, args=(_read_from_server_and_push_to_queue_msg, (inpt, msg_queue))
    )
    running_thread.start()
    return running_thread, msg_queue


def _read_from_server_and_push_to_queue_msg(out: object, working_queue: queue.Queue) -> None:
    global WAITING_DATA_TIMEOUT_IN_SECONDS
    global MSGS_BUFFER
    if isinstance(out, io.TextIOWrapper):  # handle process communication
        msg = out.readline().rstrip('\n')
        working_queue.put(msg)
    elif isinstance(out, socket):  # handle unix sockets
        # wait limited small amount of time avoiding forever blocking of the task
        ready = select.select([out], [], [], WAITING_DATA_TIMEOUT_IN_SECONDS)
        if ready[0]:
            data = out.recv(256)
            MSGS_BUFFER.extend(data)
            begin_index = 0
            for i in range(0, len(MSGS_BUFFER)):
                if MSGS_BUFFER[i] == 10:  # 10 - \n symbol
                    is_win_ending = i != 0 and MSGS_BUFFER[i - 1] == 13
                    msg = MSGS_BUFFER[begin_index:(i - 1 if is_win_ending else i)].decode("utf-8")
                    working_queue.put(msg)
                    begin_index = i + 1
            del MSGS_BUFFER[:begin_index]
    else:
        raise Exception("Unknown reading channel")


def _process_event(addon: typing.Dict[str, object], msg: str) -> None:
    try:
        index = msg.find(FIELD_SEPARATOR)
        if index < 0:
            type_token = msg
        else:
            type_token = msg[:index]
        if type_token in addon:
            handlers = addon[type_token]
            params = _get_parameters_from_msg(type_token, msg)
            for handler in handlers:
                handler(addon, *params)
    except Exception:
        print("Last msg - " + msg, file=sys.stderr)
        traceback.print_exc()
        _stop_addon()


def _start_processing_of_messages(msg_queue: queue.Queue, addon: typing.Tuple[str, object]) -> threading.Thread:
    working_thread = threading.Thread(
        target=_run_task_until_stop, args=(_get_msg_from_queue_and_pass_to_processing, (msg_queue, addon))
    )
    working_thread.start()
    return working_thread


def _count_processed_events() -> None:
    global event_counter
    global RUN
    while RUN:
        with counter_lock:
            print("Events processed for the period " + str(event_counter), flush=True)
            event_counter = 0
        time.sleep(1)


def _get_msg_from_queue_and_pass_to_processing(msg_queue: queue.Queue,
                                               addon: typing.Dict[str, object]) -> None:
    global event_counter
    global WAITING_DATA_TIMEOUT_IN_SECONDS
    try:
        msg = msg_queue.get(timeout=WAITING_DATA_TIMEOUT_IN_SECONDS)
        _process_event(addon, msg)
        with counter_lock:
            event_counter += 1
    except queue.Empty:
        pass


def _get_msg_from_sending_queue_and_send_it(output: object, msg_queue: queue) -> None:
    global WAITING_DATA_TIMEOUT_IN_SECONDS
    try:
        msg = msg_queue.get(timeout=2)
        _send_msg(output, msg)
    except queue.Empty:
        pass


def _run_task_until_stop(task, params):
    global RUN
    if len(params) == 0:
        while RUN:
            task()
    else:
        while RUN:
            task(*params)


def _send_msg(output: object, msg: str):
    if isinstance(output, io.TextIOWrapper):
        output.write(msg + '\n')
        output.flush()
    elif isinstance(output, socket):
        output.send((msg + '\n').encode("utf-8"))
    else:
        raise Exception("Unknown output")


# writing to a server by the addon should be done in a one thread
def _start_writing_task(server_in: object):
    msg_queue = queue.Queue()
    running_thread = threading.Thread(
        target=_run_task_until_stop, args=(_get_msg_from_sending_queue_and_send_it, (server_in, msg_queue))
    )
    running_thread.start()
    return running_thread, msg_queue


def _push_msg(queue: queue.Queue, msg: str):
    queue.put(msg)


# handler(addon, event)
def _add_event_handler(addon: typing.Dict[str, object], event_type: str, handler: typing.Callable) -> None:
    if event_type not in addon:
        event_handlers = [handler]
    else:
        event_handlers = addon[event_type]
        event_handlers.append(handler)
    addon[event_type] = event_handlers


def _handle_event_sending_it_to_server(addon, event_type, raw_msg_event):
    send_msg_queue = addon["send_msg_queue"]
    _push_msg(send_msg_queue, raw_msg_event)


def _request_data(addon, alias, req_id, event_type, params):
    msg = FIELD_SEPARATOR.join([REQ_DATA, alias, str(req_id), event_type, FIELD_SEPARATOR.join(params)])
    _push_msg_to_event_queue(addon, msg)


def _get_parameters_from_msg(type_token: str, msg: str):
    tokens = msg.split(FIELD_SEPARATOR)
    if type_token == DEPTH:
        # alias, isBid, price in ticks, size in ticks
        return tokens[1], tokens[2] == "1", int(tokens[3]), int(tokens[4])
    elif type_token == MBO:
        # alias, event_type, order_id, price, size
        return tokens[1], tokens[2], tokens[3], int(tokens[4]), int(tokens[5])
    elif type_token == TRADE:
        # alias, price, size, is_otc, is_bid_aggressor, is_execution_start, is_execution_end, aggressor_order_id, passive_order_id
        return tokens[1], float(tokens[2]), int(tokens[3]), tokens[4] == "1", tokens[5] == "1", tokens[6] == "1", \
                                                            tokens[7] == "1", str(tokens[8]), str(tokens[9])
    elif type_token == INSTRUMENT_INFO:
        # alias, full_name, is_crypto, pips, size_multiplier, instrument_multiplier
        return tokens[1], tokens[2], tokens[3] == "1", float(tokens[4]), float(tokens[5]), float(tokens[6]), json.loads(tokens[7])
    elif type_token == INDICATOR_RESPONSE:
        # request_id, indicator_id
        return int(tokens[1]), int(tokens[2])
    elif type_token == ON_INTERVAL:
        return [tokens[1]]
    elif type_token == RESP_DATA:
        # request_id
        return [int(tokens[1])]
    elif type_token == INSTRUMENT_DETACHED:
        # alias
        return [tokens[1]]
    elif type_token == SERVER_INIT:
        return []
    elif type_token == ON_SETTINGS_PARAMETER_CHANGED:
        # alias, setting name, field type, new value
        if tokens[3] == "NUMBER":
            new_value = float(tokens[4])
        elif tokens[3] == "COLOR":
            new_value = tuple(int(color_part) for color_part in ",".split(tokens[4]))
        elif tokens[3] == "BOOLEAN":
            new_value = "true" == tokens[4]
        else:
            new_value = tokens[4]
        return tokens[1], tokens[2], tokens[3], new_value
    elif type_token == EXECUTE_ORDER:
        return tokens[1], json.loads(tokens[2])
    elif type_token == UPDATE_ORDER:
        return [json.loads(tokens[1])]
    elif type_token == BALANCE_UPDATE:
        return [json.loads(tokens[1])]
    elif type_token == POSITION_UPDATE:
        return [json.loads(tokens[1])]
    elif type_token == BROADCASTING:
        return tokens[1], json.loads(tokens[2])
    elif type_token == PROVIDERS_STATUS:
        return [json.loads(tokens[1])]
    elif type_token == BROADCASTING_SETTINGS:
        return tokens[1], json.loads(tokens[2])
    else:
        # default case when there should not be any parameter parsing, but instead the msg should be sent to a server
        return type_token, msg


################ PUBLIC FUNCTIONS ##########################


def _push_msg_to_event_queue(addon: typing.Dict[str, object], msg: str) -> None:
    event_queue = addon["event_queue"]
    _push_msg(event_queue, msg)


def create_addon(connection_type=TCP_SOCKET, settings=None):
    global RUN
    if RUN:
        # TODO: single script several addons? Might be useful when BM will provide the way to dynamically load jar files.
        raise Exception("Addon is already created, only one addon is supported")
    RUN = True
    if connection_type == LOCAL_PROCESS:
        server_in, server_out = _connect_as_local_process()
        state_dict = {"server_in": server_in, "server_out": server_out}
    elif connection_type == UNIX_SOCKET:
        socket_file = sys.argv[1]
        sock = _connect_as_unix_socket_client(socket_file)
        state_dict = {"socket": sock}
    elif connection_type == TCP_SOCKET:
        socket_port = sys.argv[1]
        sock = _connect_as_tcp_socket_client(int(socket_port))
        state_dict = {"socket": sock}
    else:
        raise Exception("Unknown connection type")
    return state_dict


def start_addon(addon: typing.Dict[str, object],
                add_instrument_handler: typing.Callable[
                    [typing.Dict[str, object], str, str, bool, float, float, float], None],
                detach_instrument_handler: typing.Callable[[str], None]):
    # register event handler for events which should be sent to the server from the client
    _add_event_handler(addon, CLIENT_INIT, _handle_event_sending_it_to_server)
    _add_event_handler(addon, REQ_DATA, _handle_event_sending_it_to_server)
    _add_event_handler(addon, REGISTER_INDICATOR, _handle_event_sending_it_to_server)
    _add_event_handler(addon, ADD_POINT_TO_INDICATOR, _handle_event_sending_it_to_server)
    _add_event_handler(addon, FINISHED_INITIALIZATION, _handle_event_sending_it_to_server)
    _add_event_handler(addon, ADD_SETTING_FIELD, _handle_event_sending_it_to_server)
    _add_event_handler(addon, SEND_ORDER, _handle_event_sending_it_to_server)
    _add_event_handler(addon, CANCEL_ORDER, _handle_event_sending_it_to_server)
    _add_event_handler(addon, MOVE_ORDER, _handle_event_sending_it_to_server)
    _add_event_handler(addon, MOVE_ORDER_TO_MARKET, _handle_event_sending_it_to_server)
    _add_event_handler(addon, RESIZE_ORDER, _handle_event_sending_it_to_server)
    _add_event_handler(addon, INSTRUMENT_INFO, _get_default_add_instrument_handler(add_instrument_handler))
    _add_event_handler(addon, INSTRUMENT_DETACHED, detach_instrument_handler)
    _add_event_handler(addon, REGISTER_BROADCASTING_PROVIDER, _handle_event_sending_it_to_server)
    _add_event_handler(addon, SEND_USER_MESSAGE, _handle_event_sending_it_to_server)

    if "server_in" in addon and "server_out" in addon:
        server_in = addon["server_in"]
        server_out = addon["server_out"]
    elif "socket" in addon:
        server_in = addon["socket"]
        server_out = addon["socket"]
    else:
        raise Exception("No communication method found")

    reading_task, event_queue = _start_reading_task(server_in)
    writing_task, send_msg_queue = _start_writing_task(server_out)

    addon["send_msg_queue"] = send_msg_queue
    addon["event_queue"] = event_queue

    _start_processing_of_messages(event_queue, addon)
    _push_msg(send_msg_queue, CLIENT_INIT)

    addon["reading_task"] = reading_task
    addon["writing_task"] = writing_task


def _get_default_add_instrument_handler(add_instrument_handler: typing.Callable[
    [typing.Dict[str, object], str, str, bool, float, float, float, typing.Dict[str, object]], None]) -> typing.Callable[
    [typing.Dict[str, object], str, str, bool, float, float, float, typing.Dict[str, object]], None]:
    def _default_instrument_handler(addon, alias, fullname, is_crypto, pips, size_multiplier,
                                    instrument_multiplier, supported_features):
        add_instrument_handler(addon, alias, fullname, is_crypto, pips, size_multiplier,
                               instrument_multiplier, supported_features)
        _finish_initialization(addon, alias)

    return _default_instrument_handler


def _finish_initialization(addon: typing.Dict[str, object], alias: str):
    msg = FIELD_SEPARATOR.join((FINISHED_INITIALIZATION, alias))
    _push_msg_to_event_queue(addon, msg)


def _stop_addon():
    global RUN, exit_code
    if not RUN:
        raise Exception("Addon is stopped already")
    RUN = False
    print("Addon has been stopped", flush=True)
    exit_code = 1


## data subscriber wrappers
def subscribe_to_trades(addon: typing.Dict[str, object], alias: str, req_id: int):
    _request_data(addon, alias, req_id, TRADE, ())


# TODO: does not work
def subscribe_to_bars(addon: typing.Dict[str, object], alias: str, req_id: int, interval_in_seconds: int):
    _request_data(addon, alias, req_id, BAR, [str(interval_in_seconds)])


def subscribe_to_depth(addon: typing.Dict[str, object], alias: str, req_id: int):
    _request_data(addon, alias, req_id, DEPTH, ())


def subscribe_to_mbo(addon: typing.Dict[str, object], alias: str, req_id: int):
    _request_data(addon, alias, req_id, MBO, ())


def subscribe_to_order_info(addon: typing.Dict[str, object], alias: str, req_id: int):
    _request_data(addon, alias, req_id, ORDER_INFO, ())


def subscribe_to_balance_updates(addon: typing.Dict[str, object], alias: str, req_id: int):
    _request_data(addon, alias, req_id, BALANCE_UPDATE, ())


def subscribe_to_position_updates(addon: typing.Dict[str, object], alias: str, req_id: int):
    _request_data(addon, alias, req_id, POSITION_UPDATE, ())


####### INDICATORS
def register_indicator(addon: typing.Dict[str, object], alias: str, req_id: int, indicator_name: str, graph_type: str,
                       color=(0, 255, 0),
                       line_style="SOLID",
                       initial_value=0.0,
                       show_line_by_default=True,
                       show_widget_by_default=True,
                       is_modifiable=False):
    msg = FIELD_SEPARATOR.join(
        (REGISTER_INDICATOR,
         alias,
         str(req_id),
         indicator_name,
         graph_type,
         str(initial_value),
         str(1 if show_line_by_default else 0),
         str(1 if show_widget_by_default else 0),
         ",".join(map(str, color)),
         line_style,
         str(1 if is_modifiable else 0)))
    _push_msg_to_event_queue(addon, msg)


# TODO: this should be improved. It is better to specify settings during addon creation, it looks simpler
def add_number_settings_parameter(addon: typing.Dict[str, object], alias: str, parameter_name: str,
                                  default_value: float,
                                  minimum: float, maximum: float, step: float,
                                  reload_if_change=True):
    field_type = "NUMBER"
    converted_minimum = float(minimum)
    converted_maximum = float(maximum)
    converted_step = float(step)
    converted_def_val = float(default_value)

    msg = FIELD_SEPARATOR.join(
        (ADD_SETTING_FIELD,
         alias,
         field_type,  # TODO: change it making a field dynamic
         parameter_name,
         "1" if reload_if_change else "0",
         str(converted_def_val),
         str(converted_minimum),
         str(converted_maximum),
         str(converted_step)))
    _push_msg_to_event_queue(addon, msg)


def add_boolean_settings_parameter(addon: typing.Dict[str, object], alias: str, parameter_name: str,
                                   default_value: bool,
                                   reload_if_change=True):
    msg = FIELD_SEPARATOR.join(
        (ADD_SETTING_FIELD,
         alias,
         "BOOLEAN",
         parameter_name,
         "1" if reload_if_change else "0",
         "1" if default_value else "0"
         ))
    _push_msg_to_event_queue(addon, msg)


def add_string_settings_parameter(addon: typing.Dict[str, object], alias: str, parameter_name: str, default_value: str,
                                  reload_if_change=True):
    msg = FIELD_SEPARATOR.join(
        (ADD_SETTING_FIELD,
         alias,
         "STRING",
         parameter_name,
         "1" if reload_if_change else "0",
         "1" if default_value else "0"
         ))
    _push_msg_to_event_queue(addon, msg)


def add_color_settings_parameter(addon: typing.Dict[str, object], alias: str, parameter_name: str,
                                 default_value: typing.Tuple[int, int, int], reload_if_change=True) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (ADD_SETTING_FIELD,
             alias,
             "COLOR",
             parameter_name,
             "1" if reload_if_change else "0",
             ",".join(str(color_token) for color_token in default_value),
             )
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


def add_label_to_settings(addon: typing.Dict[str, object], alias: str, label_value: str) -> None:
    msg = FIELD_SEPARATOR.join(
        (ADD_SETTING_FIELD,
         alias,
         "LABEL",
         label_value,
         "0",    # reload_on_change is not used for labels
         "0"     # default value is not used for labels
         )
    )
    _push_msg_to_event_queue(addon, msg)


def add_point(addon: typing.Dict[str, object], alias: str, indicator_id: int, point: float) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (ADD_POINT_TO_INDICATOR,
             alias,
             str(indicator_id),
             str(point))
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


def send_order(addon: typing.Dict[str, object],
               order_send_parameters: OrderSendParameters) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (SEND_ORDER,
             order_send_parameters.to_json()
             )
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


# if you want to specify canceling few orders in batch, you should put is_batch_end and batch_id arguments
# to this method (choose your own batch_id for it). If only order_id is specified, only single order will be cancelled
# if you specify is_batch_end to False, then unique batch_id will be generated automatically
def cancel_order(addon: typing.Dict[str, object],
                 alias: str,
                 order_id: str,
                 is_batch_end: bool = True,
                 batch_id: int = float("nan")) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (CANCEL_ORDER,
             alias,
             order_id,
             str(is_batch_end),
             str(batch_id)
             )
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


# specify new limit_price and new stop_price, specify limit_price to nan if the order doesn't have limit price,
# specify stop_price to nan if the order doesn't have stop price, stop_price is nan by default
def move_order(addon: typing.Dict[str, object],
               alias: str,
               order_id: str,
               limit_price: float,
               stop_price: float = float("nan")) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (MOVE_ORDER,
             alias,
             order_id,
             str(limit_price),
             str(stop_price)
             )
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


# specify offset for the depth, order should move into the market
def move_order_to_market(addon: typing.Dict[str, object],
                         alias: str,
                         order_id: str,
                         offset: int) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (MOVE_ORDER_TO_MARKET,
             alias,
             order_id,
             str(offset)
             )
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


def resize_order(addon: typing.Dict[str, object],
                 alias: str,
                 order_id: str,
                 size: int) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (RESIZE_ORDER,
             alias,
             order_id,
             str(size)
             )
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


def subscribe_to_generator(addon: typing.Dict[str, object], provider_name: str, generator_name: str = None,
                           does_require_filtering: bool = False) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (REGISTER_BROADCASTING_PROVIDER,
             provider_name,
             str(generator_name),
             str(does_require_filtering))
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


def subscribe_to_provider(addon: typing.Dict[str, object], provider_name: str) -> None:
    subscribe_to_generator(addon, provider_name, None, False)


def send_user_message(addon: typing.Dict[str, object], alias: str, message: str) -> None:
    try:
        msg = FIELD_SEPARATOR.join(
            (SEND_USER_MESSAGE,
             alias,
             message)
        )
        _push_msg_to_event_queue(addon, msg)
    except Exception:
        traceback.print_exc()
        _stop_addon()


######

def wait_until_addon_is_turned_off(addon: typing.Dict[str, object]) -> None:
    global exit_code
    if "reading_task" not in addon or "writing_task" not in addon:
        raise Exception("State was not set properly. Did you start addon?")
    addon["reading_task"].join()
    print("Reading task stopped", flush=True, file=sys.stderr)
    addon["writing_task"].join()
    print("Addon is turned off", flush=True)
    sys.exit(exit_code)


### HANDLERS wrappers
def add_trades_handler(addon: typing.Dict[str, object], handler: typing.Callable[
    [str, float, int, bool, bool, bool, bool, str, str], None]) -> None:
    _add_event_handler(addon, TRADE, handler)


def add_bar_handler(addon, handler) -> None:
    _add_event_handler(addon, BAR, handler)


def add_mbo_handler(addon: typing.Dict[str, typing.Any],
                    handler: typing.Callable[[str, str, str, int, int], None]) -> None:
    _add_event_handler(addon, MBO, handler)


def add_depth_handler(addon: typing.Dict[str, object],
                      handler: typing.Callable[[str, bool, int, int], None]) -> None:
    _add_event_handler(addon, DEPTH, handler)


def add_indicator_response_handler(addon: typing.Dict[str, object],
                                   handler: typing.Callable[[int, int], None]) -> None:
    _add_event_handler(addon, INDICATOR_RESPONSE, handler)


def add_on_interval_handler(addon: typing.Dict[str, object],
                            handler: typing.Callable[[], None]) -> None:
    _add_event_handler(addon, ON_INTERVAL, handler)


def add_response_data_handler(addon: typing.Dict[str, object],
                              handler: typing.Callable[[int], None]) -> None:
    _add_event_handler(addon, RESP_DATA, handler)


def add_on_setting_change_handler(addon: typing.Dict[str, object], handler: typing.Callable[
    [str, str, str, object], None]) -> None:
    _add_event_handler(addon, ON_SETTINGS_PARAMETER_CHANGED, handler)


def add_on_order_executed_handler(addon: typing.Dict[str, typing.Any],
                                  handler: typing.Callable[[str, str, typing.Dict[str, typing.Any]], None]) -> None:
    _add_event_handler(addon, EXECUTE_ORDER, handler)


def add_on_order_updated_handler(addon: typing.Dict[str, typing.Any],
                                 handler: typing.Callable[[str, typing.Dict[str, typing.Any]], None]) -> None:
    _add_event_handler(addon, UPDATE_ORDER, handler)


def add_on_balance_update_handler(addon: typing.Dict[str, typing.Any],
                                  handler: typing.Callable[[str, typing.Dict[str, typing.Any]], None]) -> None:
    _add_event_handler(addon, BALANCE_UPDATE, handler)


def add_on_position_update_handler(addon: typing.Dict[str, typing.Any],
                                   handler: typing.Callable[[str, typing.Dict[str, typing.Any]], None]) -> None:
    _add_event_handler(addon, POSITION_UPDATE, handler)


def add_broadcasting_handler(
        addon: typing.Dict[str, object],
        handler: typing.Callable[[str, object], None]
) -> None:
    _add_event_handler(addon, BROADCASTING, handler)


def add_broadcasting_provider_status_handler(
        addon: typing.Dict[str, object],
        handler: typing.Callable[[str, typing.Dict[str, typing.Any]], None]
) -> None:
    _add_event_handler(addon, PROVIDERS_STATUS, handler)


def add_broadcasting_settings_handler(
        addon: typing.Dict[str, object],
        handler: typing.Callable[[str, object], None]
) -> None:
    _add_event_handler(addon, BROADCASTING_SETTINGS, handler)


################ Util objects
### Order book
def create_order_book() -> typing.Dict[str, SortedDict]:
    return {"asks": SortedDict(), "bids": SortedDict(lambda x: -x)}


def on_depth(order_book: typing.Dict[str, SortedDict], is_bid: bool, price: int, size: int) -> None:
    if not isinstance(price, int) or not isinstance(size, int):
        raise ValueError("Order book is supposed to be filled in by values in Ticks")

    try:
        side_dict_key = "bids" if is_bid else "asks"
        side_dicts = order_book[side_dict_key]
        if size == 0:
            if price in side_dicts:
                del side_dicts[price]
            else:
                pass
        else:
            side_dicts[price] = size
    except Exception:
        traceback.print_exc()
        _stop_addon()


# returns ((best_bid, bid_size), (best_ask, ask_size)), or None instead of side tuple
def get_bbo(order_book: typing.Dict[str, SortedDict]) -> typing.Tuple[int or None, int or None]:
    try:
        bid_dict = order_book["bids"]
        ask_dict = order_book["asks"]
        return (bid_dict.keys()[0], bid_dict.values()[0]) if len(bid_dict) > 0 else None, \
            (ask_dict.keys()[0], ask_dict.values()[0]) if len(ask_dict) > 0 else None
    except Exception:
        traceback.print_exc()
        _stop_addon()


# returns (bid side sum, best ask size sum)
def get_sum(order_book: typing.Dict[str, SortedDict], levels_num: int) -> typing.Tuple[int, int]:
    try:
        bids, asks = order_book["bids"], order_book["asks"]
        bids_keys, asks_keys = bids.keys(), asks.keys()
        bids_size, asks_size = len(bids_keys), len(asks_keys)
        best_bid, best_ask = bids_keys[0] if bids_size > 0 else -1, asks_keys[0] if asks_size > 0 else -1
        bids_sum, asks_sum = bids[best_bid] if best_bid != -1 else 0, asks[best_ask] if best_ask != -1 else 0
        for _ in range(1, levels_num):
            best_bid -= 1
            best_ask += 1
            if best_bid in bids:
                bids_sum += bids[best_bid]
            if best_ask in asks:
                asks_sum += asks[best_ask]
        return bids_sum, asks_sum
    except Exception:
        traceback.print_exc()
        _stop_addon()


def create_mbo_book() -> typing.Dict[str, typing.Any]:
    mbp_book = create_order_book()
    return {"orders": SortedDict(), "mbp_book": mbp_book}


def on_new_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str, is_bid: bool, price: int,
                 size: int) -> None:
    orders = mbo_order_book["orders"]

    if order_id in orders:
        raise ValueError("Order already has order:" + order_id + ". Should not you use on_replace_order?")
    if size == 0:
        raise ValueError("Size can't be zero, if use on_rewove_order function instead")

    orders[order_id] = (is_bid, price, size)
    order_book = mbo_order_book["mbp_book"]

    side_dict_key = "bids" if is_bid else "asks"
    side_dicts = order_book[side_dict_key]
    current_size = 0
    if price in side_dicts:
        current_size = side_dicts[price]
    on_depth(order_book, is_bid, price, current_size + size)


def on_replace_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str, new_price: int,
                     new_size: int) -> None:
    orders = mbo_order_book["orders"]
    if order_id not in orders:
        raise ValueError("Order book does not have order: " + order_id)
    if new_size == 0:
        raise ValueError("Size is zero, use on_remove_order instead")

    replaced_order = orders[order_id]
    is_order_bid = replaced_order[0]
    old_price = replaced_order[1]
    old_size = replaced_order[2]
    orders[order_id] = (is_order_bid, new_price, new_size)

    # update MBP book
    mbp_book = mbo_order_book["mbp_book"]
    side_dicts = mbp_book["bids" if is_order_bid else "asks"]
    orders[order_id] = (is_order_bid, new_price, new_size)
    old_price_level_size = side_dicts[old_price] - old_size
    new_price_level_size = (side_dicts[new_price] if new_price in side_dicts else 0) + new_size
    on_depth(mbp_book, is_order_bid, old_price, old_price_level_size)
    on_depth(mbp_book, is_order_bid, new_price, new_price_level_size)


def on_remove_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> None:
    orders = mbo_order_book["orders"]
    if order_id not in orders:
        raise ValueError("Order book does not have order: " + order_id)

    removed_order = orders[order_id]
    del orders[order_id]

    is_bid = removed_order[0]
    price = removed_order[1]
    size = removed_order[2]

    mbp_order_book = mbo_order_book["mbp_book"]
    side_dicts = mbp_order_book["bids" if is_bid else "asks"]
    new_size = side_dicts[price] - size
    on_depth(mbp_order_book, is_bid, price, new_size)


def get_all_order_ids(mbo_order_book: typing.Dict[str, typing.Any]) -> typing.List[str]:
    orders = mbo_order_book["orders"]
    return orders.keys()


def has_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> bool:
    orders = mbo_order_book["orders"]
    return order_id in orders


def get_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> typing.Tuple[bool, int, int]:
    orders = mbo_order_book["orders"]
    if order_id not in orders:
        return None
    return orders[order_id]


def get_order_price(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> int:
    orders = mbo_order_book["orders"]
    if order_id not in orders:
        raise ValueError("Order book does not have order: " + order_id)

    return orders[order_id][1]


def get_order_size(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> int:
    orders = mbo_order_book["orders"]
    if order_id not in orders:
        raise ValueError("Order book does not have order: " + order_id)

    return orders[order_id][2]


def get_order_side(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> bool:
    orders = mbo_order_book["orders"]
    if order_id not in orders:
        raise ValueError("Order book does not have order: " + order_id)

    return orders[order_id][0]
