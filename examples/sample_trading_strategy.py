import pyl1api as bm

INITIAL_THRESHOLD = 3  # relative value, multiply it by size_multiplier to get actual value
INITIAL_ORDER_SIZE = 1  # same as INITIAL_THRESHOLD
ORDER_OFFSET = 20  # relative value, multiply it by pips to get actual offset

req_id = 0
interval_count = 0

alias_to_instrument = {}
"""Maps alias (which is the name of the instrument) to dictionary holding certain information about the instrument."""

alias_to_order_book = {}
"""Maps alias to the order book."""

alias_to_buy_order_id = {}
"""Maps alias to the current BUY order ID (we only have a single buy order in this strategy)."""

alias_to_sell_order_id = {}
"""Maps alias to the current SELL order ID (we only have a single sell order in this strategy)."""

alias_to_position = {}
"""Maps alias to the current instrument's position."""


def handle_instrument_info(addon, alias, full_name, _is_crypto, pips, size_multiplier, _instrument_multiplier):
    global req_id

    alias_to_instrument[alias] = {
        "alias": alias,
        "full_name": full_name,
        "pips": pips,
        "size_multiplier": size_multiplier,
    }
    alias_to_order_book[alias] = bm.create_order_book()
    req_id += 1
    bm.subscribe_to_depth(addon, alias, req_id)
    req_id += 1
    bm.subscribe_to_order_info(addon, alias, req_id)
    req_id += 1
    bm.subscribe_to_position_updates(addon, alias, req_id)
    print("Registered...", flush=True)


def handle_instrument_detached(_addon, alias):
    del alias_to_order_book[alias]
    del alias_to_instrument[alias]


def handle_depth_info(_addon, alias, is_bid, price, size):
    bm.on_depth(alias_to_order_book[alias], is_bid, price, size)


def order_update_handler(_addon, event):
    status = event['status']
    is_buy = event['isBuy']
    alias = event['instrumentAlias']
    order_id = event['orderId']

    if status == 'WORKING':
        if is_buy:
            alias_to_buy_order_id[alias] = order_id
            print("current buys " + str(alias_to_buy_order_id), flush=True)
        else:
            alias_to_sell_order_id[alias] = order_id
            print("current sells " + str(alias_to_sell_order_id), flush=True)
    elif status == 'CANCELLED':
        if is_buy:
            if alias in alias_to_buy_order_id:
                del alias_to_buy_order_id[alias]
            print("current buys " + str(alias_to_buy_order_id), flush=True)
        else:
            if alias in alias_to_sell_order_id:
                del alias_to_sell_order_id[alias]
            print("current sells " + str(alias_to_sell_order_id), flush=True)
    print("Order Update: " + str(event), flush=True)


def position_update_handler(_addon, event):
    # here we receive position size as integer value, with instruments
    # that may be decimal fractions apply size_multiplier to get actual position size
    alias_to_position[event['instrumentAlias']] = event['position']
    print("Position Update: " + str(event), flush=True)


def order_execute_handler(_addon, alias, event):
    possible_open_buy_order_id = alias_to_buy_order_id.get(alias)
    possible_open_sell_order_id = alias_to_sell_order_id.get(alias)
    order_id = event['orderId']

    if order_id == possible_open_buy_order_id:
        del alias_to_buy_order_id[alias]
        print("current buys after execution " + str(alias_to_buy_order_id), flush=True)
    elif order_id == possible_open_sell_order_id:
        del alias_to_sell_order_id[alias]
        print("current sells after execution " + str(alias_to_sell_order_id), flush=True)
    print("Order execute alias: " + alias + " entity: " + str(event), flush=True)


def on_interval(addon):
    """
    Callback triggered with periodic interval, right now it is not possible to configure interval period, and it is
    fixed 0.1 sec.
    """
    global interval_count
    interval_count += 1

    # interval is 0.1 sec, if we want to process orders once per 3 seconds then every 30 on_interval calls
    # we will perform trading related activity
    if interval_count % 30 != 0:
        return

    print("3 secs passed", flush=True)

    for alias, order_book in alias_to_order_book.items():
        if alias in alias_to_instrument:
            (best_bid_price_level, best_bid_size_level), (best_ask_price_level, best_ask_size_level) = \
                bm.get_bbo(order_book)
            instrument = alias_to_instrument[alias]
            instrument_pips = instrument["pips"]

            # note: alias_to_position[alias] is not actual position size,
            # actual position size = alias_to_position[alias] * size_multiplier
            position = 0
            if alias in alias_to_position:
                position = alias_to_position[alias]
            # note: position is not actual position size, actual position size = position * size_multiplier
            if position < INITIAL_THRESHOLD:
                new_price = (best_bid_price_level - ORDER_OFFSET) * instrument_pips
                if alias in alias_to_buy_order_id:
                    print("attempt to move buy order with price" + str(new_price), flush=True)
                    bm.move_order(addon, alias, alias_to_buy_order_id[alias], new_price)
                else:
                    print("attempt to send buy order with price" + str(new_price), flush=True)
                    # note: INITIAL_ORDER_SIZE is not actual order size, actual order size = INITIAL_ORDER_SIZE *
                    # size_multiplier
                    bm.send_order(addon, alias, True, INITIAL_ORDER_SIZE, 'GTC', new_price)

            # note: position is not actual position size, actual position size = position * size_multiplier
            if position > -INITIAL_THRESHOLD:
                new_price = (best_ask_price_level + ORDER_OFFSET) * instrument_pips
                if alias in alias_to_sell_order_id:
                    print("attempt to move sell order with price" + str(new_price), flush=True)
                    bm.move_order(addon, alias, alias_to_sell_order_id[alias], new_price)
                else:
                    print("attempt to send sell order with price" + str(new_price), flush=True)
                    # note: INITIAL_ORDER_SIZE is not actual order size, actual order size = INITIAL_ORDER_SIZE *
                    # size_multiplier
                    bm.send_order(addon, alias, False, INITIAL_ORDER_SIZE, 'GTC', new_price)


if __name__ == "__main__":
    addon = bm.create_addon()
    bm.add_depth_handler(addon, handle_depth_info)
    bm.add_on_interval_handler(addon, on_interval)
    bm.add_on_order_executed_handler(addon, order_execute_handler)
    bm.add_on_order_updated_handler(addon, order_update_handler)
    bm.add_on_position_update_handler(addon, position_update_handler)
    bm.start_addon(addon, handle_instrument_info, handle_instrument_detached)
    bm.wait_until_addon_is_turned_off(addon)
