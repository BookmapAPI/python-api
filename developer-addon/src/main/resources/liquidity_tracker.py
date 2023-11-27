import bookmap as bm

alias_to_order_book = {}
alias_to_instrument = {}
alias_to_bid_liquidity_indicator = {}
alias_to_ask_liquidity_indicator = {}
request_id_to_related_indicator_alias = {}
liquidity_sizes = {}
req_id = 0
DEFAULT_LIQUIDITY_SIZE = 10


def handle_subscribe_instrument(addon, alias, full_name, is_crypto, pips, size_multiplier, instrument_multiplier, supported_features):
    global req_id

    instrument = {
        "alias": alias,
        "full_name": full_name,
        "is_crypto": is_crypto,
        "pips": pips,
        "size_multiplier": size_multiplier,
        "instrument_multiplier": instrument_multiplier
    }
    alias_to_instrument[alias] = instrument
    alias_to_order_book[alias] = bm.create_order_book()
    req_id += 1
    request_id_to_related_indicator_alias[req_id] = (alias, True)
    bm.register_indicator(addon, alias, req_id, "Bid sum",
                          "BOTTOM", color=(204, 204, 0))
    req_id += 1
    request_id_to_related_indicator_alias[req_id] = (alias, False)
    bm.register_indicator(addon, alias, req_id, "Ask sum",
                          "BOTTOM", color=(0, 0, 204))
    req_id += 1
    bm.subscribe_to_depth(addon, alias, req_id)
    liquidity_sizes[alias] = DEFAULT_LIQUIDITY_SIZE
    bm.add_number_settings_parameter(
        addon, alias, "Liquidity size", DEFAULT_LIQUIDITY_SIZE, 5, 25, 1)
    print("Registered...", flush=True)


def handle_unsubscribe_instrument(addon, alias):
    del alias_to_order_book[alias]
    del alias_to_instrument[alias]


def handle_depth_info(addon, alias, is_bid, price, size):
    order_book = alias_to_order_book[alias]
    bm.on_depth(order_book, is_bid, price, size)


# callback triggered with periodic interval, right not it is not configurable and used
def on_interval_draw_liquidity_info(addon, alias):
    for alias, order_book in alias_to_order_book.items():
        if alias in alias_to_ask_liquidity_indicator and alias in alias_to_bid_liquidity_indicator:
            ask_liquidity_indicator = alias_to_ask_liquidity_indicator[alias]
            bid_liquidity_indicator = alias_to_bid_liquidity_indicator[alias]
            if alias in alias_to_instrument and alias in liquidity_sizes:
                instrument = alias_to_instrument[alias]
                instrument_size_multiplier = instrument["size_multiplier"]
                liquidity_size = liquidity_sizes[alias]
                bid_liquidity_in_ticks, ask_liquidity_in_ticks = bm.get_sum(
                    order_book, liquidity_size)
                bm.add_point(addon, alias, ask_liquidity_indicator, float(
                    ask_liquidity_in_ticks) / instrument_size_multiplier)
                bm.add_point(addon, alias, bid_liquidity_indicator, float(
                    bid_liquidity_in_ticks) / instrument_size_multiplier)


def handle_register_indicator_response(addon, request_id, indicator_id):
    alias, is_bid = request_id_to_related_indicator_alias[request_id]
    if is_bid:
        alias_to_bid_liquidity_indicator[alias] = indicator_id
    else:
        alias_to_ask_liquidity_indicator[alias] = indicator_id


# handler called each time respective UI settings are changed. Value type depends on initial registered type,
# might be floated, boolean, str, or tuple representing color
def on_settings_change_handler(addon, alias: str, setting_name: str, field_type: str, new_value):
    print("Received settings changed " + str(alias) + " " + str(setting_name) +
          " " + str(field_type) + " " + str(new_value), flush=True)

    if setting_name == "Liquidity size":
        liquidity_sizes[alias] = int(new_value)


if __name__ == "__main__":
    addon = bm.create_addon()
    bm.add_depth_handler(addon, handle_depth_info)
    bm.add_on_interval_handler(addon, on_interval_draw_liquidity_info)
    bm.add_indicator_response_handler(
        addon, handle_register_indicator_response)
    bm.add_on_setting_change_handler(addon, on_settings_change_handler)
    bm.start_addon(addon, handle_subscribe_instrument, handle_unsubscribe_instrument)
    bm.wait_until_addon_is_turned_off(addon)
