# Python API reference
___


## What is Python API.
___
<p>Bookmap's primary technology is Java. Ninety percent of all functionality is written in Java including indicators, strategies, and other addons. The Python API addon allows you to develop and run Bookmap addons while avoiding Java concepts as much as possible. Instead of Java, it is possible to write an addon using only Python 3, so no knowledge of Java syntax, runtime and build process are required.
Thus, potential developers need only basic knowledge of Python and a clear idea of what they want from Bookmap.
</p>


## Quick start
___
See [this doc](https://docs.google.com/document/d/178YRno3iKKdbuvVjVh380ayR-VsSUlQGZt2tDFjjD3A) for quick start guide.

## Technical requirements
To run the addon only latest stable Bookmap version is required and python >= 3.7.14 installed on the machine.
 Lower versions of Python 3 are not supported officially, but still may work. Python 2 is not supported and won't work.
## Detailed API reference
___
Reference to all publicly available functions provided here.

### Basic functions
___
#### create_addon

```python
def create_addon(connection_type=TCP_SOCKET, settings=None) -> typing.Dict[str, typing.Any]:
```
This is initial function that is usually called from main from the beginning. Creates addon and return addon entity being entry point to
Bookmap addon configuration. It is supposed to be called only once in the beginning of addon configuration. You
MUST NOT call it two or more times per single addon run.

Addon object represents dictionary containing state of the addon. Values of this dictionary should never be accessed
directly, unless you are really dived into details and know what you are doing. The API is developed in the way to hide as
much tech details from common users as possible, so the only purpose of the dictionary from this function is to pass it
to a bunch of other functions as a state.

If you feel that you need to get access to this dictionary directly, which means that public API is not enough for
you, then we encourage to request missing functionality from Bookmap team.

This method receives two parameters:
1. connection_type=TCP_SOCKET
2. settings=None

Editing of these parameters is not supported right now, so common usage of this functions from the script
looks like `bm.create_addon()`

#### start_addon
```python
def start_addon(addon: typing.Dict[str, typing.Any],
                add_instrument_handler: typing.Callable[
                    [typing.Dict[str, typing.Any], str, str, bool, float, float, float], typing.NoReturn],
                detach_instrument_handler: typing.Callable[[str], typing.NoReturn]):
```
Start addon is second most important function after [create_addon](#create_addon). It is responsible for starting of communication
between Bookmap application and Python script. You must call it only once after you are done with addon configuration. Usually it happens when you
add all event handlers to the addon (see [Event handlers](#Event handlers)).

The function receives three parameters: 
1. addon - is the addon itself, state object received by calling [create_addon](#create_addon).
2. add_instrument_handler - is the function responsible for initial instrument handling. It will be called each time you
   enable addon from Bookmap application. It is your responsibility to define such function with a proper signature.
   This is one of two handlers that must be implemented for every addon.  See below example.
3. detach_instrument_handler - is the function responsible for notification about disabling addon for this instrument,
   alternatively it can mean that instrument is not subscribed anymore. Once this function is triggered, you should
   not do any actions in favor of detached instrument.

Example:
```python
import pyl1api as bm

# addon - entity received from [create_addon](#create_addon)
# alias - string; it defines unique name of respective instrument
# full_name - string; it defines display name of the instrument
# is_crypto - bool; it defines whether this instrument is crypto related or not
# pips - float; it defines minimal tick size of respective instrument.
# size_multiplier - float; it defines minimal size change visible for the instrument.
# instrument_multiplier - float; it defines contact size multiplier, useful for futures.
def handle_subscribe_instrument(addon, alias, full_name, is_crypto, pips, size_multiplier, instrument_multiplier):
    print("Hello world from " + alias, flush=True)

# addon - entity received from [create_addon](#create_addon)
# alias - string; it defines unique name of respective instrument
def handle_unsubscribe_instrument(addon, alias):
    print("Goodbye world from " + alias, flush=True)


if __name__ == "__main__":
    addon = bm.create_addon()
    # start addon, requires 3 arguments - addon itself, handler for subscribe event
    # and handler for unsubscribe event
    bm.start_addon(addon, handle_subscribe_instrument, handle_unsubscribe_instrument)
    bm.wait_until_addon_is_turned_off(addon)
```

#### wait_until_addon_is_turned_off
```python
# addon - entity received from create_addon function
def wait_until_addon_is_turned_off(addon: typing.Dict[str, object]) -> typing.NoReturn:
```
Typical life cycle of the addon should be controlled by Bookmap application only.
In other words, addon should be enabled when user clicks on checkbox to enable, and should be disabled, when user turns it off. To avoid low priority daemon threads, this method
just blocks main thread until it is killed (usually by Bookmap application). You should place this
statement in the end of each main block if you want correct addon life cycle.

### Event handlers
___
Handlers are crucial concept for Bookmap Python API. Each handler is responsible for handling specific type of event
received from Bookmap application. Any handler is represented as a function that addon developer defines in the Python script.
There are two important requirements to handlers: 
  1. Handler for each event has strictly defined set of parameters. This signature should be repeated by any custom handler.
  2. Handler should never block its thread or do some complex (time consuming) computations. The problem here is that all
     handlers work in the same thread, so next event won't be handled until the previous event handling is finished. Since BM
     application is focused on real time data, delays created
     due to complex computation might impact on stability dramatically.

As was mentioned above, all handlers are triggered from the same thread, so if you share context with several handlers, you should not typically worry about 
concurrency issues.

All handlers except two are optional and can be ignored. For two mandatory handlers see [start_addon](#start_addon).

It is possible to find public function responsible for adding handlers for each type of handler. You can find the list of such functions below.
Note that more than 1 handler can be registered for each event.

#### add_depth_handler
```python
def add_depth_handler(addon: typing.Dict[str, typing.Any],
                      handler: typing.Callable[[str, bool, int, int], typing.NoReturn]) -> typing.NoReturn:
```

Adds handler for depth events. Depth event represents change on the single price level. For more details see [subscribe_depth](#subscribe_depth).

Example of handler:

```python
# addon - entity received from create_addon function
# alias - string; it defines unique name of respective instrument
# is_bid - bool; it defines side of the price level, True if bid, otherwise false
# price - int; it defines price in ticks
# size - int; it defines a new size of the price level
def handle_depth_info(addon, alias: str, is_bid: bool, price: int, size: int):
   ...
```

#### add_mbo_handler
```python
def add_mbo_handler(addon: typing.Dict[str, typing.Any],
                     handler: typing.Callable[[str, str, str, int, int], typing.NoReturn]) -> typing.NoReturn:
    ...
```

Adds handler for MBO events. MBO events are sent in case of provider supports MBO data. See [subscribe_to_mbo](#subscribe_to_mbo).

Example of handler:

```python
# addon - entity received from create_addon function
# alias - string; it defines unique name of respective instrument
# event_type - string; possible values can be 
#               ASK_NEW - new ASK order,
#               BID_NEW - new BID order,
#               REPLACE - order updated (size or price is changed),
#               CANCEL - order removed
# order_id - string; unique order id
# price - int; integer price
# size - int; integer size
def handle_mbo_event(addon, alias: str, event_type: str, order_id: str, price: int, size: int):
    ...
```

#### add_trades_handler
```python
add_trades_handler(addon: typing.Dict[str, object], handler: typing.Callable[
    [str, float, int, bool, bool, bool, bool, str, str], typing.NoReturn]) -> typing.NoReturn:
```

Adds handler for trade events. Each trade event might represent separate trade as well as aggregated trade. It depens on
provider. See [subscribe_to_trades](#subscribe_to_trades).

Example of handler:

```python
# addon - entity received from create_addon function
# alias - string; it defines unique name of respective instrument
# price - float; price in currency
# size - int; integer size
# is_otc - bool; defines whether this is OTC or not. True if OTC, False by default
# is_bid - bool; defines aggressor side unless aggressor_order_id/passive_order_id can be checked
# is_execution_start: bool; defines whether this event is a part of large trade, usually it is always   
#                     true and should be taken into account only if next parameter is false, however it is not strict rule
#                     do handling only if you are sure that provider gives you this information.
# is_execution_end: bool; defines whether this event is a part of large trade, similar to is_execution_start
# aggressor_order_id: string; aggressor order provided only if such information is supported by the provider, by default empty string
# passive_order_id: string; conceptually the same as aggressor_order_id, just points to passive order
def handle_trades(addon, alias: str, price: float, size: int, is_otc: bool, is_bid: bool, is_execution_start: bool, is_execution_end: bool, aggressor_order_id: str,
                  passive_order_id: str):
    ...
```

#### add_response_data_handler
```python
def add_response_data_handler(addon: typing.Dict[str, object],
                              handler: typing.Callable[[int], typing.NoReturn]) -> typing.NoReturn:
```

Handles response from Bookmap that data has been successfully subscribed. This is response handler for requests done via:
[subscribe_to_depth](#subscribe_to_depth), [subscribe_to_trades](#subscribe_to_trades), [subscribe_to_mbo](#subscribe_to_mbo).

It is not mandatory to handle it, but receiving response through this handler you can be sure that your request passed
to Bookmap and data can be expected.

Example of handler:

```python
# addon - entity received from create_addon function
# req_id - int; request id specified in subscribe_* function 
def add_subscribe_data_response_handler(addon, req_id: int):
    ...
```

#### add_on_interval_handler

```python
def add_on_interval_handler(addon: typing.Dict[str, object],
                            handler: typing.Callable[[], typing.NoReturn]) -> typing.NoReturn:
```

Adds interval event handler. Interval is a special type of event sent by Bookmap application to the script every second.
This is useful handler if you have to do some actions (like updating of indicator, calculating some value) every small period of time.
As was mentioned, right now it supports only 1 second interval, however future updates will have this value configurable.

Example of handler:

```python
# addon - entity received from create_addon function
def on_interval(addon):
    ...
```

#### add_indicator_response_handler

```python
def add_indicator_response_handler(addon: typing.Dict[str, object],
                                   handler: typing.Callable[[int, int], typing.NoReturn]) -> typing.NoReturn:
```

Adds handler responsible for receiving and processing event about created indicator (in response on [register_indicator](#register_indicator)).

Example of handler:
```python
# addon - entity received from create_addon function
# request_id - int; id of the request specified in register_indicator function
# indicator_id - int; dynamic integer value assigned to each registered indicator to identify it. 
#                   It is specified during each request to the registered indicator, so it should be remembered. 
def handle_indicator_response(addon, request_id: int, indicator_id: int):
    ...
```

#### add_on_settings_change_handler
```python
def add_on_setting_change_handler(addon: typing.Dict[str, object],handler: typing.Callable[
    [str, str, str, object], typing.NoReturn]) -> typing.NoReturn:
```

Adds handler called each time settings value is changed on UI side. Handler registered via this method
will be called for any type of settings parameters. It means that potential handler should be able to handle all types of
added settings or at least should ignore any settings which it is not supposed to be handle.

Example of handler:
```python
liquidity_sizes = {}
# addon - entity received from create_addon function
# alias - string; it defines unique name of respective instrument
# fields_type - str; specifies exact type of the field, valid value: "NUMBER", "BOOLEAN", "COLOR", "STRING"
# new_value - any; depending on field type can be int, bool, str or tuple representing color
def on_settings_change_handler(addon, alias: str, setting_name: str, field_type: str, new_value: typing.Any):
    global liquidity_sizes
    print("Received settings changed " + str(alias) + " " + str(setting_name) + " " + str(field_type) + " " + str(
        new_value), flush=True)

    if setting_name == "Liquidity size":
        liquidity_sizes[alias] = int(new_value)
```

### Subscribe data
___
Crucial functionality of Bookmap is possibility to show as much market data as exchange can provide. In order to be able
to consume exact market data as Bookmap application consumes, Python API provides several simple functions.

It is important to understand that each type of event  will be sent as soon as possible once it is received by Bookmap. 
Thus, it is common thing to register proper handler before any attempt to subscribe to data. Opposite sequence of actions, i.e. 
registering handler after data subscription might cause lack of important market data. In best, just few update will be missing on Python side happens, the worst is inconsistent order book will trigger crash of the whole application.

The common approach is to trigger any of subscribe related methods from [handle_subscribe_instrument](#start_addon).

Note that Bookmap uses special alias format, so hardcoding the alias of instrument you wish to subscribe is bad error-prone practice.
Always use alias received from [handle_subscribe_instrument](#start_addon).


#### subscribe_to_depth
```python
# alias - str; represents unique alias of the instrument
# req_id - int, request id, useful to identify request response
def subscribe_to_depth(addon: typing.Dict[str, object], alias: str, req_id: int)
```
Sends request for depth data. Depth data represents a flow of order book updates. Each update depicts a price level and 
corresponding size. All depth events are always real time events (with few exceptions), i.e. delay is usually equal to delay
of communication between your machine and exchange server. To see how to handle depth events, check [add_depth_handler](#add_depth_handler).

Once this function is triggered 2 things occur.
1. Message about successful subscription is sent. It can be handled via [add_response_data_handler](#add_response_data_handler).
It will have the same `req_id` as the request.
2. Initial depth events will be sent to reconstruct order book. It will be a high number of depth events representing a latest
state of existing price levels in order book.

Note, that all events are received with integer price and sized. See [Prices and Sizes in Bookmap](#Prices and Sizes in Bookmap)
for more details. For more info how to keep tracking order book state on Python side see [create_order_book](#create_order_book).

Sometimes even after getting response, you might not receive depth data even though depth is updated on Bookmap chart.
Often it means that your provider does not report depth data, but instead reports MBO(market by order) data. It is more
detailed and complex form of data, but can be much more useful for market analyse. See [subscribe_to_mbo](#subscribe_to_mbo).


#### subscribe_to_mbo
```python
# alias - str; represents unique alias of the instrument
# req_id - int, request id, useful to identify request response
def subscribe_to_mbo(addon: typing.Dict[str, object], alias: str, req_id: int):
```
Sends request for depth data represented by MBO events. Each MBO event depicts a change in order book. It shows a specific order
change instead a whole price level change. With MBO data feed order book can be represented as it is, as a set of independant orders
constructing a market. MBO events are always real time events.

To handle MBO events see [add_mbo_handler](#add_mbo_handler). General rules applied to this function are the same as for 
[subscribe_to_depth](#subscribe_to_depth) i.e. the same response will be sent once it is subscribed, events for order book
reconstruction will be sent as well.

Note that depending on the provider, depth data can be sent together with MBO events i.e. support two different modes of depth
subscription. However, usually only one mode is supported either depth or MBO. The best approach is to subscribe to
depth and MBO if you just need to receive as much as possible market data and to be ready to handle all types of data.

#### subscribe_to_trades
```python
# alias - str; represents unique alias of the instrument
# req_id - int, request id, useful to identify request response
def subscribe_to_trades(addon: typing.Dict[str, object], alias: str, req_id: int):
```
Sends request for trade data. Each trade event represents trade happened on the market. Each trade event is always real time
event. It operates in the same way as [subscribe_to_depth](#subscribe_to_depth) i.e. sends response via special callback
once it is subscribed.

### Indicators
Indicators allow you to draw curves on the heatmap or on the subchart during trading sessions.

#### register_indicator
```python
# alias - str; represents unique alias of the instrument
# req_id - int; request id, useful to identify request response
# indicator_name - str; name of the indicator which will be displayed on Bookmap side
# graph_type - str; represents where chart will be drawn, either heatmap of subchart. Valid values: "PRIMARY", "BOTTOM".
#              PRIMARY - define heatmap indicators, "BOTTOM" - defines subchart indicators.
# color - tuple; represents color of indicator in RGB format (red, green, blue). By default (0,255,0).
# line_style - str; represents style of an indicator line, valid values: "SOLID", "SHORT_DASH","LONG_DASH", "DOT", "DASH_DOT" (by default SOLID).
# initial value - float; represents value which will be assigned from the beginning (by defaul 0.0).
# show_line_by_default - bool; not used for now, should be always True.
# show_widget_by_default - bool; not used for now, should be always True
# is_modifiable - bool; not used for now, should be always default
def register_indicator(addon: typing.Dict[str, object], 
                       alias: str,
                       req_id: int,
                       indicator_name: str,
                       graph_type: str,
                       color=(0, 255, 0),
                       line_style="SOLID",
                       initial_value=0.0,
                       show_line_by_default=True,
                       show_widget_by_default=True,
                       is_modifiable=False):
```
The method allows to register indicator in Bookmap. In case of success each call of this method produces event
depicting successful registration. This event MUST be handled if you want to use this indicator.
See [add_indicator_response_handler](#add_indicator_response_handler). Response contains **indicator_id** integer value.
This value is used to map request for indicator update with exact indicator. It should be remembered by the script. It is guaranteed that every indicator 
in scope of a single addon run will have unique id across all instruments. 

One instrument can have more than one indicator.

#### add_point
This function is to draw via specific indicator.
```python
# alias - str; represents unique alias of the instrument
# indicator_id - int; indicator id received as a response for register_indicator
# point - float; Y-axis point
def add_point(addon: typing.Dict[str, object], alias: str, indicator_id: int, point: float) -> typing.NoReturn:
```
Note that even though point value is supposed to be a float, if you draw on the heatmap and want to have your value being depicted
according to a price level, you need to specify not the real price, but integer representation of the price see [Prices and Sizes in Bookmap](#prices-and-sizes-in-bookmap).

### Settings panel
It is also possible to create custom settings with Python API. These settings can be used to provide the way
to change configurable values of the addon from Bookmap GUI. For example, you can add number settings field which 
will specify the minimum size of trades which should be handled by your addon. All settings are per alias, so it is recommended
to specify them in instrument callback. See [start_addon](#start_addon) for details.

String, number, boolean and color values are supported. Note that settings can be saved to Bookmap workspace, so no need
to specify exact value every time you restart Bookmap.

Each UI change of config value is handled by special handler added via 
[add_on_settings_change_handler](#add_on_settings_change_handler).

#### add_number_settings_parameter
```python
# alias: str; unique alias of the instrument
# parameter_name: str; depicts parameter name (will be displayed on the config panel)
# default_value: float; initial value, will be overwritten via callback if workspace contains another value
# minimum: float; minimal valid value
# maximum: float: maximum valid value
# step: float: step between values
# reload_if_change: bool; is not used, keep it as it is.
def add_number_settings_parameter(addon: typing.Dict[str, object],
                                  alias: str,
                                  parameter_name: str,
                                  default_value: float,
                                  minimum: float,
                                  maximum: float,
                                  step: float,
                                  reload_if_change=True):
```
Adds number settings parameter to the addon. Allows you to have and configure through UI a number parameter.

#### add_boolean_settings_parameter
```python
# alias: str; unique alias of the instrument
# parameter_name: str; depicts parameter name (will be displayed on the config panel)
# default_value: bool; depicts initial state of the checkbox
# reload_if_change: bool; is not used, keep it as it is.
def add_boolean_settings_parameter(addon: typing.Dict[str, object],
                                   alias: str,
                                   parameter_name: str,
                                   default_value: bool,
                                   reload_if_change=True):
```
Adds named checkbox to the configuration panel.

#### add_string_settings_parameter
```python
# alias: str; unique alias of the instrument
# parameter_name: str; depicts parameter name (will be displayed on the config panel)
# default_value: str; depicts initial state of the text area
# reload_if_change: bool; is not used, keep it as it is.
def add_string_settings_parameter(addon: typing.Dict[str, object],
                                  alias: str,
                                  parameter_name: str,
                                  default_value: str,
                                  reload_if_change=True):
```
Adds named string configuration field.

#### add_color_settings_parameter
```python
# alias: str; unique alias of the instrument
# parameter_name: str; depicts parameter name (will be displayed on the config panel)
# default_value: tuple(int, int, int); depicts default color in the form of RGB color 
# reload_if_change: bool; is not used, keep it as it is.
def add_color_settings_parameter(addon: typing.Dict[str, object],
                                 alias: str,
                                 parameter_name: str,
                                 default_value: typing.Tuple[int, int, int],
                                 reload_if_change=True) -> typing.NoReturn:
```
Adds color palette configuration.

### Utils
#### create_order_book
```python
def create_order_book() -> typing.Dict[str, SortedDict]:
```
This function creates MBP(market by price) order book entity which can be used to reduce complexity of handling order book data.
This function just returns dictionary representing bids and asks price levels. Dictionary contains two keys: "bids" and "asks".
Value for each pair is [SortedDict](https://grantjenks.com/docs/sortedcontainers/sorteddict.html). Each sorted dict is dictionary of keys
representing integer price and values representing size. It is sorted by keys, i.e. by price. You never should write to this dictionary directly
and normally you do not read from it directly as well.
#### on_depth

```python
# order_book: entity created by create_order_book() function
# is_bid: bool; represents side of the price level, True if bid, False otherwise
# price: int; represents price of the price level
# size: int; represents size of the price level
def on_depth(order_book: typing.Dict[str, SortedDict], is_bid: bool, price: int, size: int) -> typing.NoReturn:
```
This function updates order book according to received depth event.

#### get_bbo
```python
# order_book: entity create by create_order_book() function
# returns ((best_bid, bid_size), (best_ask, ask_size)), or None instead of side tuple
def get_bbo(order_book: typing.Dict[str, SortedDict]) -> typing.Tuple[int or None, int or None]:
```
This function is used to get best bid and ask levels at the moment.

#### get_sum
```python
# order_book: entity create by create_order_book() function
# levels_num: int; number of levels that should be counted
# returns (bid side sum, best ask size sum)
def get_sum(order_book: typing.Dict[str, SortedDict], levels_num: int) -> typing.Tuple[int, int]:
```
Returns sum of sizes from each side according to `levels_num` parameter. For example, if `levels_num = 5`, then
sum of sizes of 5 first price levels for bids and asks will be returned. Sums is calculated according to BBO, i.e. 5
price levels for bid are best bid and four next levels below. In the same way, 5 price levels for ask are best ask and 
four price levels above. 

Note that price levels are counted not by number of liquid levels, but by pips step, so if between two price levels exist
empty prices levels, they will be taken into account as zeros.

#### create_mbo_book()

```python
def create_mbo_book() -> typing.Dict[str, typing.Any]:
```
Creates and return order book in order to help to handle MBO data. It represents dictionary with two keys. The first key
is `"orders"`, it contains [SortedDict](https://grantjenks.com/docs/sortedcontainers/sorteddict.html) where keys are order
ids and values are tuple with info about order, `(side: bool, price: int, size: int)`. The second key is `"mbp_book"`. It is possible
to get common mbp book (the same as created via [create_order_book](#create_order_book)).

#### on_new_order
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
# is_bid: bool; size of the order
# price: int; integer price of the order
# size: int; integer size of the order
def on_new_order(mbo_order_book: typing.Dict[str, typing.Any],
                 order_id: str,
                 is_bid: bool,
                 price: int,
                 size: int) -> typing.NoReturn:
```
This function is used to handle new MBO event with `ASK_NEW` or `BID_NEW` event type.
See [add_mbo_handler](#add_mbo_handler) for more details. It throws `ValueError` if this order is already exists.

#### on_replace_order
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
# price: int; integer price of the order
# size: int; integer size of the order
def on_replace_order(mbo_order_book: typing.Dict[str, typing.Any],
                     order_id: str,
                     new_price: int,
                     new_size: int) -> typing.NoReturn:
```
This function is used to handle MBO event with `REPLACE` event type. See [add_mbo_handler](#add_mbo_handler) for more details.
It throws `ValueError` if this order does not exist.
#### on_remove_order
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
def on_remove_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> typing.NoReturn:
```
This function is used to handle MBO event with `CANCEL` event type. See [add_mbo_handler](#add_mbo_handler) for more details.
It throws `ValueError` if this order does not exist.
#### get_all_order_ids
```python
# mbo_order_book: entity create by create_mbo_book()
def get_all_order_ids(mbo_order_book: typing.Dict[str, typing.Any]) -> typing.List[str]:
```
This function returns all order ids registered in the order book.

#### has_order
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
def has_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> bool:
```
This function returns true if the order with specified order id exists, otherwise false.

#### get_order
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
def get_order(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> typing.Tuple[bool, int, int] | None:
```
This function returns side, integer price and integer size in tuple if order with the specified order id exists, otherwise it returns None.

#### get_order_price
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
def get_order_price(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> int:
```
This function returns order price or throw `ValueError` if there is no such order.

#### get_order_size
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
def get_order_size(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> int:
```
This function returns order size or throw `ValueError` if there is no such order. 

#### get_order_side
```python
# mbo_order_book: entity create by create_mbo_book()
# order_id: str; unique id of the order in scope of a single instrument and single trading session
def get_order_side(mbo_order_book: typing.Dict[str, typing.Any], order_id: str) -> bool:
```

This function returns order side, True if bid otherwise False. Throw `ValueError` if there is no such order.

## Prices and Sizes in Bookmap
___
If you use Bookmap API actively, you probably found that Bookmap rarely sends prices and sizes as it is, 
i.e. represented by some floating point numbers depicting price in dollars/euro/whatever. This paragraph is about to explain
why it is done in this way and how to convert integer values to 'real values' and vice versa.

For most of cases prices are represented in integers. It is done in this way because of performance issues. Internal
constant calculation of floating point numbers requires too many resources, while integer calculation is usually much faster.

Bookmap crucial feature is to be able to handle hundreds of thousands of updates per second having decent, but
not top hardware, affordable by most people. Avoiding of floating point arithmetic helps with this task. So real
conversion from integer to floating point number happens only when it is really needed int the moment prices should be showed
to the end user.

Bookmap provides different kind of information about the instrument in the moment of subscription (handled by [handle_instrument_subscription](#start_addon)).
Specifically each instrument reports its **pips** value or **tick size** (as it is depicted on GUI side). This value defines 
the minimal visible step of price movement.

**For example**, if best bid is 300.25$ and pips is 0.25$, then minimal best bid change can be either to 300.50$ or 300.00$.

Integer price in Bookmap is calculated as *RealPrice / pips*. In other words, the above price of 300.25$ with pips
equal to 0.25 will be represented in integer numbers like *1201* (300.25 / 0.25 = 1201). Hence, if you 
receive depth update in integer, you just need to multiply it on pips value to receive real price. 

**For example**: 1201 * 0.25 = 300.25 in the above case.

The same idea lays in size representation. To calculate size properly **instrument_multiplier** is introduced. The same
calculations should be done as with price.



