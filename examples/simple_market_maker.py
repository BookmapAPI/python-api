from dataclasses import dataclass

import bookmap as bm


@dataclass
class Settings:
    threshold: int = 0
    order_size: int = 0
    price_offset: int = 0
    interval_secs: float = 0.0

    was_threshold_set: bool = False
    was_order_size_set: bool = False
    was_price_offset_set: bool = False
    was_interval_secs_set: bool = False

    def set_threshold(self, value): self.threshold = value; self.was_threshold_set = True
    def set_order_size(self, value): self.order_size = value; self.was_order_size_set = True
    def set_price_offset(self, value): self.price_offset = value; self.was_price_offset_set = True
    def set_interval_secs(self, value): self.interval_secs = value; self.was_interval_secs_set = True

    def all_settings_set():
        return was_threshold_set and was_order_size_set and was_price_offset_set and was_interval_secs_set


req_id = 0
interval_count = 0

alias_to_instrument = {}
"""Maps alias (which is the name of the instrument) to dictionary holding certain information about the instrument."""

alias_to_order_book = {}
"""Maps alias to the order book."""

alias_to_buy_order = {}
"""Maps alias to the current BUY order (we only have a single buy order in this strategy)."""

alias_to_sell_order = {}
"""Maps alias to the current SELL order (we only have a single sell order in this strategy)."""

alias_to_position = {}
"""Maps alias to the current instrument's position."""

alias_to_settings = {}
"""Maps alias to the addon settings which the user selects in GUI."""

alias_to_interval_count = {}


def handle_instrument_info(addon, alias, full_name, _is_crypto, pips, size_multiplier, _instrument_multiplier, _supported_features):
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

    # it is relative value, to get actual value multiply it by size_multiplier
    bm.add_number_settings_parameter(
        addon, alias, "Threshold",
        3, 1, 1000, 1)

    # it is relative value, to get actual value multiply it by size_multiplier
    bm.add_number_settings_parameter(
        addon, alias, "Order size",
        1, 1, 1000, 1)

    # it is relative value, to get actual value multiply it by pips
    bm.add_number_settings_parameter(
        addon, alias, "Price offset",
        20, 1, 1000, 1)

    # interval is measured in seconds, the step is 0.1 sec
    bm.add_number_settings_parameter(
        addon, alias, "Interval", 3, 1, 300, 0.1)
    alias_to_interval_count[alias] = 0

    alias_to_settings[alias] = Settings()

    print("Registered...", flush=True)


def handle_instrument_detached(_addon, alias):
    del alias_to_order_book[alias]
    del alias_to_instrument[alias]


def handle_depth_info(_addon, alias, is_bid, price, size):
    bm.on_depth(alias_to_order_book[alias], is_bid, price, size)


def order_update_handler(_addon, event):
    status = event["status"]
    is_buy = event["isBuy"]
    alias = event["instrumentAlias"]
    order_id = event["orderId"]
    limit_price = event["limitPrice"]
    client_id = event["clientId"]

    if status == "WORKING":
        if is_buy:
            if alias in alias_to_buy_order:
                if client_id == alias_to_buy_order[alias]["client_id"]:
                    alias_to_buy_order[alias]["order_id"] = order_id
                    alias_to_buy_order[alias]["limit_price"] = limit_price
        else:
            if alias in alias_to_sell_order:
                if client_id == alias_to_sell_order[alias]["client_id"]:
                    alias_to_sell_order[alias]["order_id"] = order_id
                    alias_to_sell_order[alias]["limit_price"] = limit_price

    elif status == "CANCELLED" or status == "FILLED":
        possible_open_buy_order = alias_to_buy_order.get(alias)
        possible_open_sell_order = alias_to_sell_order.get(alias)

        if possible_open_buy_order is not None:
            possible_open_buy_order_id = possible_open_buy_order.get("order_id")
        else:
            possible_open_buy_order_id = None

        if possible_open_sell_order is not None:
            possible_open_sell_order_id = possible_open_sell_order.get("order_id")
        else:
            possible_open_sell_order_id = None

        if event["orderId"] == possible_open_buy_order_id:
            del alias_to_buy_order[alias]
            print("current buys " + str(alias_to_buy_order), flush=True)
        elif event["orderId"] == possible_open_sell_order_id:
            del alias_to_sell_order[alias]
            print("current sells " + str(alias_to_sell_order), flush=True)
    print("Order Update: " + str(event), flush=True)


def position_update_handler(_addon, event):
    # here we receive position size as integer value, with instruments
    # that may be decimal fractions apply size_multiplier to get actual position size
    alias_to_position[event["instrumentAlias"]] = event["position"]
    print("Position Update: " + str(event), flush=True)


def on_interval(addon, alias):
    """
    Callback triggered with periodic interval, right now it is not possible to configure interval period, and it is
    fixed 0.1 sec.
    """

    settings = alias_to_settings.get(alias)
    if not settings or not settings.all_settings_set:
        # Don't do anything until the API sends us all the settings.
        return

    alias_to_interval_count[alias] += 1

    # interval is 0.1 sec, if we want to process orders once per 3 seconds then every 30 on_interval calls
    # we will perform trading related activity

    if alias_to_interval_count[alias] != round(settings.interval_secs * 10):
        return

    print(str(settings.interval_secs) + " secs passed", flush=True)
    alias_to_interval_count[alias] = 0

    order_book = alias_to_order_book.get(alias)
    if order_book is None:
        print(f"on_interval called for alias '{alias}' which doesn't have any order book")
        return

    instrument = alias_to_instrument.get(alias)
    if instrument is None:
        print(f"on_interval called for alias '{alias}' which doesn't have any instrument")
        return

    (best_bid_price_level, best_bid_size_level), (best_ask_price_level, best_ask_size_level) = bm.get_bbo(order_book)
    instrument = alias_to_instrument[alias]
    instrument_pips = instrument["pips"]
    threshold = settings.threshold
    order_size = settings.order_size
    price_offset = settings.price_offset

    # note: alias_to_position[alias] is not actual position size, actual position size = alias_to_position[
    # alias] * size_multiplier
    position = 0
    if alias in alias_to_position:
        position = alias_to_position[alias]
    # note: position is not actual position size, actual position size = position * size_multiplier
    if position < threshold:
        new_price = (best_bid_price_level - price_offset) * instrument_pips
        if alias in alias_to_buy_order:
            order_id = alias_to_buy_order[alias]["order_id"]
            if order_id is not None:
                old_price = alias_to_buy_order[alias]["limit_price"]
                if not almost_equals(old_price, new_price):
                    print("attempt to move buy order with price" + str(new_price), flush=True)
                    bm.move_order(addon, alias, order_id, new_price)
        else:
            print("attempt to send buy order with price" + str(new_price), flush=True)
            # note: INITIAL_ORDER_SIZE is not actual order size, actual order size = INITIAL_ORDER_SIZE *
            # size_multiplier
            order = bm.OrderSendParameters(alias, True, order_size)
            order.limit_price = new_price
            order.client_id = "buy-order-123"
            alias_to_buy_order[alias] = {"order_id": None, "limit_price": None, "client_id": "buy-order-123"}
            bm.send_order(addon, order)

    # note: position is not actual position size, actual position size = position * size_multiplier
    if position > -threshold:
        new_price = (best_ask_price_level + price_offset) * instrument_pips
        if alias in alias_to_sell_order:
            order_id = alias_to_sell_order[alias]["order_id"]
            if order_id is not None:
                old_price = alias_to_sell_order[alias]["limit_price"]
                if not almost_equals(old_price, new_price):
                    print("attempt to move sell order with price" + str(new_price), flush=True)
                    bm.move_order(addon, alias, order_id, new_price)
        else:
            print("attempt to send sell order with price" + str(new_price), flush=True)
            # note: INITIAL_ORDER_SIZE is not actual order size, actual order size = INITIAL_ORDER_SIZE *
            # size_multiplier
            order = bm.OrderSendParameters(alias, False, order_size)
            order.limit_price = new_price
            order.client_id = "sell-order-123"
            alias_to_sell_order[alias] = {"order_id": None, "limit_price": None, "client_id": "sell-order-123"}
            bm.send_order(addon, order)


def on_settings_change_handler(addon, alias, setting_name, field_type, new_value):
    print("Received settings changed " + str(alias) + " " + str(setting_name) +
          " " + str(field_type) + " " + str(new_value), flush=True)

    settings = alias_to_settings[alias]

    if setting_name == "Threshold":
        settings.set_threshold(int(new_value))
    elif setting_name == "Order size":
        settings.set_order_size(int(new_value))
    elif setting_name == "Price offset":
        settings.set_price_offset(int(new_value))
    elif setting_name == "Interval":
        settings.set_interval_secs(float(new_value))
    else:
        print("Unrecognized setting:", setting_name)


def almost_equals(old_price, new_price):
    return abs(old_price - new_price) < 10 ** -9


if __name__ == "__main__":
    addon = bm.create_addon()
    bm.add_depth_handler(addon, handle_depth_info)
    bm.add_on_interval_handler(addon, on_interval)
    bm.add_on_order_updated_handler(addon, order_update_handler)
    bm.add_on_position_update_handler(addon, position_update_handler)
    bm.start_addon(addon, handle_instrument_info, handle_instrument_detached)
    bm.add_on_setting_change_handler(addon, on_settings_change_handler)
    bm.wait_until_addon_is_turned_off(addon)
