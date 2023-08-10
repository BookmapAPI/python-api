import pyl1api as bm

alias_to_order_book = {}
alias_to_instrument = {}
alias_to_buy_order_id = {}
alias_to_sell_order_id = {}
alias_to_position = {}
req_id = 0
interval_count = 0
INITIAL_THRESHOLD = 10 # relative value, multiply it by size_multiplier to get actual value
INITIAL_ORDER_SIZE = 1 # same as INITIAL_THRESHOLD
INITIAL_SHIFT = 60 # absolute value, pips doesn't apply here


def handle_instrument_info(addon, alias, full_name, is_crypto, pips, size_multiplier, instrument_multiplier):
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
    bm.subscribe_to_depth(addon, alias, req_id)
    req_id += 1
    bm.subscribe_to_order_info(addon, alias, req_id)
    req_id += 1
    bm.subscribe_to_position_updates(addon, alias, req_id)
    print("Registered...", flush=True)


def handle_instrument_detached(addon, alias):
    del alias_to_order_book[alias]
    del alias_to_instrument[alias]


def handle_depth_info(addon, alias, is_bid, price, size):
    order_book = alias_to_order_book[alias]
    bm.on_depth(order_book, is_bid, price, size)


def order_update_handler(addon, event):
    status = event['status']
    if (status == 'WORKING'):
        if (event['isBuy'] == True):
            alias_to_buy_order_id[event['instrumentAlias']] = event['orderId']
            print("current buys " + str(alias_to_buy_order_id), flush=True)
        else:
            alias_to_sell_order_id[event['instrumentAlias']] = event['orderId']
            print("current sells " + str(alias_to_sell_order_id), flush=True)
    elif (status == 'CANCELLED'):
        if (event['isBuy'] == True):
            if (event['instrumentAlias'] in alias_to_buy_order_id):
                del alias_to_buy_order_id[event['instrumentAlias']]
            print("current buys " + str(alias_to_buy_order_id), flush=True)
        else:
            if (event['instrumentAlias'] in alias_to_sell_order_id):
                del alias_to_sell_order_id[event['instrumentAlias']]
            print("current sells " + str(alias_to_sell_order_id), flush=True)
    print("Order Update: " + str(event), flush=True)


def position_update_handler(addon, event):
    # here we receive postion size as integer value, with instruments
    # that may be decimal fractions apply size_multiplier to get actual position size
    alias_to_position[event['instrumentAlias']] = event['position']
    print("Position Update: " + str(event), flush=True)


def order_execute_handler(addon, alias, event):
    if (event['orderId'] in alias_to_buy_order_id.values()):
        del alias_to_buy_order_id[alias]
        print("current buys after execution " + str(alias_to_buy_order_id), flush=True)
    elif (event['orderId'] in alias_to_sell_order_id.values()):
        del alias_to_sell_order_id[alias]
        print("current sells after execution " + str(alias_to_sell_order_id), flush=True)
    print("Order execute alias: " + alias + " entity: " + str(event), flush=True)


# callback triggered with periodic interval, right not it is not configurable and used
def on_interval(addon):
    global interval_count
    interval_count += 1
    # interval is 0.1 sec, if we want to process orders once per 3 seconds then every 30 on_interval calls
    # we will perform trading related activity
    if interval_count % 30 == 0: 
        print("3 secs passed", flush=True)

        for alias, order_book in alias_to_order_book.items():
            if alias in alias_to_instrument:
                (best_bid_price_level, best_bid_size_level), (best_ask_price_level, best_ask_size_level) = bm.get_bbo(order_book)
                instrument = alias_to_instrument[alias]
                instrument_pips = instrument["pips"]
                print("pips = " + str(instrument_pips), flush=True)
                # note: alias_to_position[alias] is not actual postion size, actual postion size = alias_to_position[alias] * size_multiplier
                position = 0
                if (alias in alias_to_position):
                    position = alias_to_position[alias]
                # note: position is not actual postion size, actual postion size = position * size_multiplier
                if (position < INITIAL_THRESHOLD):
                    if (alias in alias_to_buy_order_id):
                        print("attempt to move buy order with price" + str(best_bid_price_level * instrument_pips - INITIAL_SHIFT), flush=True)
                        bm.move_order(addon, alias, alias_to_buy_order_id[alias], best_bid_price_level * instrument_pips - INITIAL_SHIFT)
                    else:
                        print("attempt to send buy order with price" + str(best_bid_price_level * instrument_pips - INITIAL_SHIFT), flush=True)
                        # note: INITIAL_ORDER_SIZE is not actual order size, actual order size = INITIAL_ORDER_SIZE * size_multiplier
                        bm.send_order(addon, alias, True, INITIAL_ORDER_SIZE, 'GTC', best_bid_price_level * instrument_pips - INITIAL_SHIFT)

                # note: position is not actual postion size, actual postion size = position * size_multiplier
                if (position > -INITIAL_THRESHOLD):
                    if (alias in alias_to_sell_order_id):
                        print("attempt to move sell order with price" + str(best_ask_price_level * instrument_pips + INITIAL_SHIFT), flush=True)
                        bm.move_order(addon, alias, alias_to_sell_order_id[alias], best_ask_price_level * instrument_pips + INITIAL_SHIFT)
                    else:
                        print("attempt to send sell order with price" + str(best_ask_price_level * instrument_pips + INITIAL_SHIFT), flush=True)
                        # note: INITIAL_ORDER_SIZE is not actual order size, actual order size = INITIAL_ORDER_SIZE * size_multiplier
                        bm.send_order(addon, alias, False, INITIAL_ORDER_SIZE, 'GTC', best_ask_price_level * instrument_pips + INITIAL_SHIFT)


if __name__ == "__main__":
    addon = bm.create_addon()
    bm.add_depth_handler(addon, handle_depth_info)
    bm.add_on_interval_handler(addon, on_interval)
    bm.add_on_order_executed_handler(addon, order_execute_handler)
    bm.add_on_order_updated_handler(addon, order_update_handler)
    bm.add_on_position_update_handler(addon, position_update_handler)
    bm.start_addon(addon, handle_instrument_info, handle_instrument_detached)
    bm.wait_until_addon_is_turned_off(addon)
