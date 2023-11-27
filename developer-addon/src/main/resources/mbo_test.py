import bookmap as bm

alias_to_mbo_book = {}


def handle_subscribe_instrument(addon, alias, full_name, is_crypto, pips, size_multiplier, instrument_multiplier, supported_features):
    global alias_to_mbo_book
    alias_to_mbo_book[alias] = bm.create_mbo_book()
    bm.subscribe_to_mbo(addon, alias, 1)
    print("Hello world from " + alias, flush=True)


def handle_unsubscribe_instrument(addon, alias):
    global alias_to_mbo_book
    print("Goodbye world from " + alias, flush=True)
    del alias_to_mbo_book[alias]


def handle_mbo_event(addon, alias, event_type, order_id, price, size):
    global alias_to_mbo_book
    book = alias_to_mbo_book[alias]
    if event_type == "ASK_NEW":
        print("Ask order: %s, price - %d, size - %d" %
              (order_id, price, size), flush=True)
        bm.on_new_order(book, order_id, False, price, size)
        return
    if event_type == "BID_NEW":
        print("Bid order: %s, price - %d, size - %d" %
              (order_id, price, size), flush=True)
        bm.on_new_order(book, order_id, False, price, size)
        return
    if event_type == "REPLACE":
        old_order = bm.get_order(book, order_id)
        # order format is tupple having values like (is_bid, price, size)
        print("Replaced order: %s, side - %s, old price - %d  new price - %d, old size - %d  new size - %d"
              % (order_id, "BID" if old_order[0] else "ASK", old_order[1], price, old_order[2], size), flush=True)
        bm.on_replace_order(book, order_id, price, size)
        return
    if event_type == "CANCEL":
        print("Canceled order: %s, side - %s, price - %d, size - %d" %
              (
                  order_id,
                  "BID" if bm.get_order_side(book, order_id) else "ASK",
                  bm.get_order_price(book, order_id),
                  bm.get_order_size(book, order_id)
              ),
              flush=True
              )
        bm.on_remove_order(book, order_id)


if __name__ == "__main__":
    addon = bm.create_addon()
    bm.add_mbo_handler(addon, handle_mbo_event)
    # start addon, requires 3 arguments - addon itself, handler for subscribe event
    # and handler for unsubscribe event
    bm.start_addon(addon, handle_subscribe_instrument,
                   handle_unsubscribe_instrument)
    # block python execution giving control over the script to Bookmap only, so you
    # do not risk, that your script will be turned off earlier that you decide
    bm.wait_until_addon_is_turned_off(addon)
