import bookmap as bm

alias_to_order_book = {}


def handle_subscribe_instrument(addon, alias, full_name, is_crypto, pips, size_multiplier, instrument_multiplier, supported_features):
    print("Instrument received " + alias, flush=True)
    # create order book for each subscribed instrument
    alias_to_order_book[alias] = bm.create_order_book()
    req_id = 1
    # subscribe to depth update for this instrument
    bm.subscribe_to_depth(addon, alias, req_id)
    print("Data subscribed", flush=True)


def handle_unsubscribe_instrument(addon, alias):
    print("The instrument " + alias + " has been removed", flush=True)


# handler for depth. it receives information about side, price and size.
def handle_depth_info(addon, alias, is_bid, price, size):
    order_book = alias_to_order_book[alias]
    # on_depth method updates internal order book
    bm.on_depth(order_book, is_bid, price, size)
    print("Depth: bid: " + str(is_bid) + ", price: " +
          str(price) + ", size: " + str(size), flush=True)
    # get liquidity for 20 levels from both side from current order book
    bid, ask = bm.get_sum(order_book, 20)
    print("Bid sum: " + str(bid) + ", ask sum: " + str(ask), flush=True)


if __name__ == "__main__":
    addon = bm.create_addon()
    print("Addon created", flush=True)
    bm.add_depth_handler(addon, handle_depth_info)
    bm.start_addon(addon, handle_subscribe_instrument, handle_unsubscribe_instrument)
    bm.wait_until_addon_is_turned_off(addon)
