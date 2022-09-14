import pyl1api as bm

def handle_subscribe_instrument(addon, alias, full_name, is_crypto, pips, size_multiplier, instrument_multiplier):
	print("Hello world from " + alias, flush=True)

def handle_unsubscribe_instrument(addon, alias):
	print("Goodbye world from " + alias, flush=True)


if __name__ == "__main__":
	addon = bm.create_addon()
	# start addon, requires 3 arguments - addon itself, handler for subscribe event
	# and handler for unsubscribe event
	bm.start_addon(addon, handle_subscribe_instrument, handle_unsubscribe_instrument)
	# block python execution giving control over the script to Bookmap only, so you
	# do not risk, that your script will be turned off earlier that you decide
	bm.wait_until_addon_is_turned_off(addon)


