import bookmap as bm  # mandatory BM import

# dict [alias, trade volume)
cvd_accumulator = {}
# dict [alias, id of indicator]
alias_to_cvd_indicator_id = {}
# dict [alias, size granularity]
alias_to_size_granularity = {}
# alias to request id to indentify proper indicator
req_id_to_alias = {}
# req_id
req_id = 1


# mandatory callback used to register instrument info inside the python
def handle_subscribe_instrument(addon, alias: str, full_name: str, is_crypto: bool, pips: float, size_granularity: float,
                                instrument_multiplier: float, supported_features: dict[str, object]):
    global cvd_accumulator
    global alias_to_size_granularity
    global req_id
    global req_id_to_alias

    # set accumulator for this alias to zero
    cvd_accumulator[alias] = 0
    # remember size multiplier for this alias. It is important to depict size correctly, since Bookmap sends size in
    # ticks, not in real price. thus, to get a real size all sizes received from Bookmap should be divided on size
    # granularity
    alias_to_size_granularity[alias] = size_granularity

    # remember mapping between indicator_req_id and alias
    indicator_req_id = req_id + 1
    req_id_to_alias[indicator_req_id] = alias
    # register indicator which will be displayed in Bookmap. minimal version of indicator registered here,
    # it is bound to current alias, has name 'Python CVD' and will be depicted on the bottom chart. third parameter
    # is request id - redundant here, since we have only one indicator, so only one bm.handle_indicator_response is
    # expected. In case number of indicators used by the script is more than 1, request id helps to match response
    # with registering request
    bm.register_indicator(addon, alias, indicator_req_id,
                          "Python CVD", "BOTTOM")

    # subscription to trade, third parameter is request id which is useful when you do multiple subscriptions state of which you need to track
    bm.subscribe_to_trades(addon, alias, 1)


# callback for bm.register_indicator... it contains request id you specifed in the method and indicator id required
# to specify every time you want to update indicator value
def handle_indicator_response(addon, request_id, indicator_id):
    global alias_to_cvd_indicator_id
    global req_id_to_alias
    alias = req_id_to_alias[request_id]
    # remember indicator id to be able to update it
    alias_to_cvd_indicator_id[alias] = indicator_id


# callback for trade events received by an instrument
def handle_trades(addon, alias: str, price: float, size: int, is_otc: bool, is_bid: bool, is_execution_start: bool, is_execution_end: bool, aggressor_order_id: str,
                  passive_order_id: str):
    global cvd_accumulator
    global alias_to_cvd_indicator_id
    global alias_to_size_granularity

    # ignore callback if there is no info about instrument, usually should not be the case
    if alias not in cvd_accumulator:
        print("Lack of instrument...", flush=True)
        return

    # get cvd indicator id for this alias
    cvd_indicator_id = alias_to_cvd_indicator_id[alias]
    # get size granularity to save sizes in correct form
    size_granularity = alias_to_size_granularity[alias]

    # by (size / size_granularity) size in ticks is converted to size in fiat currency in case if pair is traded in fiat currency
    if is_bid:
        cvd_accumulator[alias] += size / size_granularity
    else:
        cvd_accumulator[alias] -= size / size_granularity
    # finally indicator is notified that we need to draw a new point,
    # alias and cvd indicator id are taken from the above method, and current value of cvd_accumulator is specified
    bm.add_point(addon, alias, cvd_indicator_id, cvd_accumulator[alias])


# callback notifying that unsubscription appeared
def handle_unsubscribe_instrument(addon, alias):
    global cvd_accumulator
    del cvd_accumulator[alias]
    print("Detached " + alias, flush=True)


if __name__ == "__main__":
    addon = bm.create_addon()  # creating an addon, main first statement
    bm.add_trades_handler(addon, handle_trades)  # register callback for trades
    # register indicator response callback
    bm.add_indicator_response_handler(addon, handle_indicator_response)
    bm.start_addon(addon, handle_subscribe_instrument,
                   handle_unsubscribe_instrument)  # starting an addon
    # give control over python scrip to Bookmap, so it won't be finished until it is turned off from Bookmap
    bm.wait_until_addon_is_turned_off(addon)
