# Python API reference


## What is Python API.

The Python API gives you the ability to create your own custom Bookmap indicators using Python
directly within Bookmap, without the need to use an external IDE or a code editor.

Needed is only a basic knowledge of Python and a clear idea of what you want to build.

Please see the
[Python API Quick Guide](https://docs.google.com/document/d/178YRno3iKKdbuvVjVh380ayR-VsSUlQGZt2tDFjjD3A)
for a step-by-step guide on how to use Python API.

Also see the [examples](examples) directory for some examples of Python API usage.

## Technical requirements

To run the addon only latest stable Bookmap version is required and Python >= 3.7 installed on the
machine. Earlier versions of Python 3 are not supported officially, but may still work.

> Python 2 is not supported.

## API reference

### Library import

Every Bookmap API script has to import the `pyl1api` library. You may want to set the alias to `bm`
for convenience.

```python
import pyl1api as bm
```

Additionally, since we use the type hints below to denote parameter types, you may want to import
the `typing` library. This is not required, but can make your code more readable.
```python
from typing import Any, Callable, Dict, List, NoReturn, Optional, Tuple
```

### Basic functions

#### create_addon

```python
# Call this to initialize your addon state object.
addon = bm.create_addon()
```

This is usually the first thing to call. It creates and returns an addon object which is then used
as the entry point to other Bookmap Python API functionality. It must be called one time only.

The returned addon state object is used in many other functions below.

#### start_addon
```python
# Call this to start the communication between your addon and Bookmap.
bm.start_addon(addon, handle_subscribe_instrument, handle_unsubscribe_instrument)
```
It starts the communication between Bookmap and your Python script. Call it once you have added all
your [Event handlers](#Event-handlers).

`handle_subscribe_instrument` is a function that you should define. It will be called each time you
enable the addon in Bookmap for a certain instrument. All handlers, including this one, must have
a proper signature (a list of parameters), as defined below.

`handle_unsubscribe_instrument` is similar to above, except is called when the user disables the addon
for a certain instrument.

Example:
```python
import pyl1api as bm

def handle_subscribe_instrument(
    addon: Any,
    alias: str,
    full_name: str,
    is_crypto: bool,
    pips: float,
    size_multiplier: float,
    instrument_multiplier: float
) -> None:
    """
    This function is called each time the user enables your addon for a certain instrument.

    :param addon: The addon state object that you received when calling `create_addon`.
    :param alias: The alias (name) of the instrument.
    :param full_name: The full name of the instrument.
    :param is_crypto: Whether the instrument is a crypto instrument (this can mostly be ignored).
    :param pips: The minimum price increment of the subscribed instrument.
    :param size_multiplier: Inverse of the minimum size increment of the subscribed instrument.
    :param instrument_multiplier: Contract size multiplier (useful for futures).
    """
    print("Subscribing to the instrument " + alias, flush=True)

def handle_unsubscribe_instrument(
    addon: Any,
    alias: str
) -> None:
    """
    This function is called each time the user disables your addon for a certain instrument.
    
    :param addon: The addon state object that you received when calling `create_addon`.
    :param alias: The alias (name) of the instrument.
    """
    print("Unsubscribing from the instrument " + alias, flush=True)


if __name__ == "__main__":
    addon = bm.create_addon()
    # start addon, requires 3 arguments - addon itself, handler for subscribe event
    # and handler for unsubscribe event
    bm.start_addon(addon, handle_subscribe_instrument, handle_unsubscribe_instrument)
    bm.wait_until_addon_is_turned_off(addon)
```

#### wait_until_addon_is_turned_off

```python
# Call this at the end of the main block, to wait until the addon is turned off.
bm.wait_until_addon_is_turned_off(addon)
```

This function is used to block main thread until addon is turned off. Call this function at the end
of the main block of your script.

### How Bookmap handles prices and sizes

The Bookmap API doesn't use floating point numbers to represent prices and sizes. Instead, it uses
integers, where each integer represents a certain number of the minimum price increments (pips) or
the minimum size increments (which is an inverse of the `size_multiplier`). We call them a
"price level" and a "size level".

How to convert these levels to a real price or size? The rule are simple:
1. Every time you receive a "price level", simply *multiply* it by the instrument's `pips` value
   to get the price.
2. Every time you receive a "size level", simply *divide* it by the instrument's `size_multiplier`
   value to get the size.

> Why aren't these levels and not prices / sizes directly? Floating point numbers in binary
> computers cannot represent all decimal values precisely, so we want to avoid them until the last
> moment.

How to know what the instrument's `pips` and `size_multiplier` values are? You receive them via the
[handle_subscribe_instrument](#start_addon) callback when the user subscribes to an instrument.

### Event handlers

Below are listed are many more event handlers, similar to the ones above, which Bookmap calls to
give your addon certain data about the instrument (order book updates, trades, etc.).

Each handler is responsible for handling a certain type of event. You should implement all handlers
that have the data you are interested in.

There are two important requirements to handlers: 
  1. Your handlers must follow the exact parameters specified here.
  2. Your handlers should never block for too long / do some complex time-consuming code.
     All the handlers are called from the same thread, so if one handler blocks for too long, it
     will block all other handlers as well. In other words, never do `time.time(...)` or something
     similar in your handlers, but instead spin up a separate thread to do time-consuming code.

All handlers except two in [start_addon](#start_addon) are optional.

Note that you are allowed to register more than one handler for each event, but it is not
recommended, to avoid making your addon code too complex.

#### add_depth_handler
```python
# Call this if you need to receive depth (order book) events. Create your own `on_depth` function.
bm.add_depth_handler(addon, on_depth)
```

Adds handler for depth (order book) events. Each depth event represents change on the single price
level. For more details see [subscribe_depth](#subscribe_depth).

When first enabling the addon / subscribing to an instrument, Bookmap will send you the current
order book snapshot by calling your function for each order book price level.

Example of handler:

```python
def on_depth(
    addon: Any,
    alias: str,
    is_bid: bool,
    price_level: int,
    size_level: int
) -> None:
    """
    This function is called each time there is a change in the order book.
    
    :param addon:       The addon state object that you received when calling `create_addon`.
    :param alias:       The alias (name) of the instrument.
    :param is_bid:      Whether the price level is a bid (True) or an ask (False).
    :param price_level: The price level of the order book. Multiply it by `pips` to get the price.
    :param size_level:  The size level of the order book. Divide it by `size_multiplier` to get the size.
    """
```

#### add_trades_handler
```python
# Call this if you need to receive trade events. Create your own `on_trade` function.
bm.add_trades_handler(addon, on_trade)
```

Adds a handler for trade events. It gets called every time a trade happens in the market.
See [subscribe_to_trades](#subscribe_to_trades).

Example of handler:

```python
def handle_trades(
   addon: Any,
   alias: str,
   price_level: float,
   size_level: int,
   is_otc: bool,
   is_bid: bool,
   is_execution_start: bool,
   is_execution_end: bool,
   aggressor_order_id: Optional[str],
   passive_order_id: Optional[str]
) -> None:
    """
    This function is called each time a trade happens in the market.
    
    :param addon:                The addon state object that you received when calling `create_addon`.
    :param alias:                The alias (name) of the instrument.
    :param price_level:          The price of the trade. Multiply it by `pips` to get the price.
    :param size_level:           The size of the trade. Divide it by `size_multiplier` to get the size.
    :param is_otc:               Whether the trade is OTC (True) or not (False), can mostly be ignored.
    :param is_bid:               Whether the trade was a buy (True) or a sell (False).
    :param is_execution_start:   Whether this is the start of a batch. Can mostly be ignored.
    :param is_execution_end:     Whether this is the end of a batch. Can mostly be ignored.
    :param aggressor_order_id:   Which order ID was aggressive. Available only in certain cases, for MBO data.
    :param passive_order_id:     Which order ID was passive. Available only in certain cases, for MBO data.
    """
```

#### add_response_data_handler

```python
# Call this to receive a response from Bookmap that data has been successfully subscribed.
bm.on_response_data_handler(addon, on_response_data)
```

Called when Bookmap informs your addon that a certain handler has been successfully added. This is
response handler for requests such as: [subscribe_to_depth](#subscribe_to_depth),
[subscribe_to_trades](#subscribe_to_trades), [subscribe_to_mbo](#subscribe_to_mbo).

It is not mandatory to handle it, but receiving response through this handler you can be sure that
your request was received by Bookmap successfully, and data you can expect your other handlers to be
called.

Example of handler:

```python
def add_subscribe_data_response_handler(
    addon: Any,
    req_id: int
) -> None:
    """
    Gets called when Bookmap informs your addon that a certain handler has been successfully added.
    
    :param addon:  The addon state object that you received when calling `create_addon`.
    :param req_id: The request id specified in subscribe_* function.
    """
```

#### add_on_interval_handler

```python
# Call this if you need to receive interval events. Create your own `on_interval` function.
bm.add_on_interval_handler(addon, on_interval)
```

Adds an interval event handler. Interval is a special type of event sent by Bookmap to the addon
every 1 second. This is useful handler if you have to do some actions (like updating of indicator,
calculating some value) every fixed time interval.

Example of handler:

```python
def on_interval(
   addon: Any
) -> None:
    """
    This function is called at each 1 second time interval.
    
    :param addon: The addon state object that you received when calling `create_addon`.
    """
```

#### add_indicator_response_handler

```python
# Call this if you need to receive indicator events. Create your own `handle_indicator_response` function.
bm.add_indicator_response_handler(addon, handle_indicator_response)
```

Adds a handler responsible that gets called after you create an indicator, in response to [register_indicator](#register_indicator)).

Example of handler:
```python
# addon - entity received from create_addon function
# request_id - int; id of the request specified in register_indicator function
# indicator_id - int; dynamic integer value assigned to each registered indicator to identify it. 
#                   It is specified during each request to the registered indicator, so it should be remembered. 
def handle_indicator_response(
   addon: Any,
   request_id: int,
   indicator_id: int
) -> None:
    """
    This function is called after you create an indicator, in response to `register_indicator`.
    
    :param addon:        The addon state object that you received when calling `create_addon`.
    :param request_id:   The request id specified in `register_indicator`.
    :param indicator_id: The indicator id assigned to the indicator.
    """
```

#### add_on_settings_change_handler
```python
# Call this if you need to receive settings change events. Create your own `on_settings_change_handler` function.
bm.add_on_settings_change_handler(addon, on_settings_change_handler)
```

Adds a handler that gets called each time a certain addon settings value is changed by the user. 
You should ignore any settings changes which aren't supposed to be handled (TODO: clarify what this
means).

Example of handler:

```python
liquidity_sizes = {}
# addon - entity received from create_addon function
# alias - string; it defines unique name of respective instrument
# fields_type - str; specifies exact type of the field, valid value: "NUMBER", "BOOLEAN", "COLOR", "STRING"
# new_value - any; depending on field type can be int, bool, str or tuple representing color
def on_settings_change_handler(
    addon: Any,
    alias: str,
    setting_name: str,
    field_type: str,
    new_value: Any
) -> None:
    print(
         "Received settings changed " + str(alias) + " " + str(setting_name) + " " + str(field_type) + " " + str(new_value),
         flush=True
    )

    if setting_name == "Liquidity size":
        liquidity_sizes[alias] = int(new_value)
```

#### add_mbo_handler
```python
# Call this if you need to receive market-by-order (MBO) order book events. Create your own
# `handle_mbo_event` function.
# Only a small subset of exchanges / connectivities support such detailed order book data.
bm.add_mbo_handler(addon, on_mbo)
```

Adds handler for MBO events. MBO events are sent in case of provider supports MBO data. See
[subscribe_to_mbo](#subscribe_to_mbo).

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
def on_mbo(
    addon: Any,
    alias: str,
    event_type: str,
    order_id: str,
    price_level: int,
    size_level: int
) -> None:
    """
    This function is called each time there is a change in the order book.
    
    :param addon:       The addon state object that you received when calling `create_addon`.
    :param alias:       The alias (name) of the instrument.
    :param event_type:  The type of the MBO event.
    :param order_id:    The unique order id.
    :param price_level: The price level of the order book. Multiply it by `pips` to get the price.
    :param size_level:  The size level of the order book. Divide it by `size_multiplier` to get the size.
    """
```

### Subscribe data (TODO: Move this higher)

Below are examples of how to subscribe to market data. Note that you should always register your
handlers *before* subscribing to data, otherwise you might miss certain events.

The common approach is to trigger any of subscribe related methods from
[handle_subscribe_instrument](#start_addon).

Note that Bookmap uses special alias to identify instruments. Hard-coding the alias of instrument
you wish to subscribe is bad error-prone practice. Always use alias received from
[handle_subscribe_instrument](#start_addon).


#### subscribe_to_depth

```python
# Call this to subscribe to depth data. The `alias` is the instrument alias you receive in
# `handle_subscribe_instrument`. The `req_id` is the request ID you can set yourself to identify
# the subscription.
bm.subscribe_to_depth(addon, alias, req_id)
```

Sends a request for depth data. Depth data represents a flow of order book updates. Each update
depicts a price level and corresponding size. All depth events are always real time events, except
when first subscribing to depth data, in which case you first receive a snapshot of the order book.
To see how to handle depth events, check [add_depth_handler](#add_depth_handler).

Once this function is triggered, the following two things occur:
1. A message about a successful subscription is sent. It can be handled via
   [add_response_data_handler](#add_response_data_handler). It will have the same `req_id` as the
   request.
2. The initial depth events (snapshot) will be sent to reconstruct order book. You will receive many
   depth events to populate the latest state of order book.

Note that all events are received with integer price and size. See
[Prices and Sizes in Bookmap](#Prices and Sizes in Bookmap) for more details. For more info how to
keep tracking order book state on Python side see [create_order_book](#create_order_book).

Sometimes even after getting response, you might not receive depth data even though depth is updated
on Bookmap chart. Often it means that your provider does not report depth data, but instead reports
MBO (market by order) data. See [subscribe_to_mbo](#subscribe_to_mbo).


#### subscribe_to_mbo
```python
# Call this to subscribe to MBO data. The `alias` is the instrument alias you receive in
# `handle_subscribe_instrument`. The `req_id` is the request ID you can set yourself to identify
# the subscription.
bm.subscribe_to_mbo(addon, alias, req_id)
```

Sends a request for depth data represented by MBO events. Each MBO event depicts a change in order
book. It shows a specific order change instead a whole price level change. With MBO data feed order
book can be represented as it is, as a set of independent orders constructing a market. MBO events
are always real time events, except when first subscribing, since you receive a snapshot of the
order book.

To handle MBO events see [add_mbo_handler](#add_mbo_handler). General rules applied to this function
are the same as for [subscribe_to_depth](#subscribe_to_depth) i.e. the same response will be sent
once it is subscribed, events for order book reconstruction will be sent as well.

Note that depending on the provider, depth data can be sent together with MBO events, i.e. both
depth and MBO data can be supported. However, usually only one mode is supported either depth or
MBO.

#### subscribe_to_trades
```python
# Call this to subscribe to trade data. The `alias` is the instrument alias you receive in
# `handle_subscribe_instrument`. The `req_id` is the request ID you can set yourself to identify
# the subscription.
bm.subscribe_to_trades(addon, alias, req_id)
```

Sends a request for trade data. Each trade event represents trade that happened in the market. Each
trade event is always a real time event. It operates in the same way as
[subscribe_to_depth](#subscribe_to_depth) i.e. it sends a response via special callback
once it is subscribed.

### Indicators
Indicators allow you to draw curves on the Bookmap heatmap or on the bottom chart.

#### register_indicator
```python
# Call this method to register a new indicator.
#
# alias:                  str   - The instrument alias you receive in `handle_subscribe_instrument`.
# req_id:                 int   - Request id, useful to identify request response.
# indicator_name:         str   - Name of the indicator you pick, which will be displayed on Bookmap side.
# graph_type:             str   - Where should chart will be drawn, either heatmap of the bottom chart. Valid values: "PRIMARY", "BOTTOM".
#                                 PRIMARY - define heatmap indicators, "BOTTOM" - defines subchart indicators.
# color:                  tuple - The color of indicator in RGB format (red, green, blue). By default (0,255,0).
# line_style:             str   - The style of an indicator line, valid values: "SOLID", "SHORT_DASH","LONG_DASH", "DOT", "DASH_DOT" (by default SOLID).
# initial value:          float - The initial indicator value (by default 0.0).
bm.register_indicator(
     addon, alias, req_id, indicator_name, graph_type, color=(0, 255, 0), line_style="SOLID",
     initial_value=0.0
)
```

The method allows to register an indicator in Bookmap. After calling it, you will receive a
callback in [add_indicator_response_handler](#add_indicator_response_handler) which you must handle.
That response contains **indicator_id** integer value, which is used to identify the indicator.
Every indicator in scope of a single addon run will have a unique ID, across all instruments. 

A certain instrument is allowed to have multiple indicators.

#### add_point
```python
# Call this method to add a new point (value) to the indicator. The `alias` is the instrument alias
# you receive in `handle_subscribe_instrument`. The `indicator_id` is the indicator ID you receive
# in `handle_register_indicator`. The `point` should be the latest value of the indicator you want
# to set.
bm.add_point(addon, alias, indicator_id, point)
```

This function draws a new indicator point. Note that the point should be the price you want to draw
at the current time but you have to multiply it divide it `pips` in order to convert it to the price
level.

In other words, if you want to draw your indicator on the primary chart (heatmap) at price $1,100,
and the instrument's `pips` value is 0.01, you have to set `point` to 11,000.

See also [Prices and Sizes in Bookmap](#prices-and-sizes-in-bookmap).

### Addon Settings

See below how to create your custom addon settings that get displayed on the addon config panel.
Each user can set different values for each setting.

For example, you can add a setting which controls the minimum trade size acceptable by your addon.

All settings are per alias, so it is recommended to specify them in instrument callback. See
[start_addon](#start_addon) for details.

Supported setting types:
- Number
- String
- Boolean
- Color

Note that settings will be saved to the Bookmap's workspace, so no need to specify exact value every
time you restart Bookmap.

Each UI change of a certain setting value is handled via a special handler added using
[add_on_settings_change_handler](#add_on_settings_change_handler).

#### add_number_settings_parameter
```python
# Call this method to add a new number settings parameter.
#
# alias:            str   - The instrument alias you receive in `handle_subscribe_instrument`.
# parameter_name:   str   - The name of the parameter (will be displayed in the config panel).
# default_value:    float - The default value of the parameter.
# minimum:          float - The minimum allowed parameter value.
# maximum:          float - The maximum allowed parameter value.
# step:             float - The minimum allowed step between values.
bm.add_number_settings_parameter(
    addon, alias, parameter_name, default_value, minimum, maximum, step
)
```

Adds a number settings parameter to the addon. Allows you to have and configure through UI a number parameter.

#### add_boolean_settings_parameter
```python
# Call this method to add a new boolean settings parameter.
#
# alias:            str   - The instrument alias you receive in `handle_subscribe_instrument`.
# parameter_name:   str   - The name of the parameter (will be displayed in the config panel).
# default_value:    bool  - The default value of the parameter.
bm.add_boolean_settings_parameter(addon, alias, parameter_name, default_value)
```

Adds a checkbox with a label (parameter name) to the configuration panel.

#### add_string_settings_parameter
```python
# Call this method to add a new string settings parameter.
#
# alias:            str   - The instrument alias you receive in `handle_subscribe_instrument`.
# parameter_name:   str   - The name of the parameter (will be displayed in the config panel).
# default_value:    str   - The default value of the parameter.
bm.add_string_settings_parameter(addon, alias, parameter_name, default_value)
```

Adds named string configuration field.

#### add_color_settings_parameter
```python
# Call this method to add a new color settings parameter.
#
# alias:            str   - The instrument alias you receive in `handle_subscribe_instrument`.
# parameter_name:   str   - The name of the parameter (will be displayed in the config panel).
# default_value:    tuple - The default value of the parameter in RGB format (red, green, blue).
bm.add_color_settings_parameter(addon, alias, parameter_name, default_value)
```

Adds a color selection field to the configuration panel.

### Utils
#### create_order_book

```python
# Call this method to create an order book entity.
order_book = bm.create_order_book()
```

This function creates a data structure object that represents an order book (non-MBO). It is a
helper for dealing with tracking the order book. `bm.create_order_book()` returns a dictionary
representing bids and asks price levels. The dictionary contains two keys: "bids" and "asks".

Value for each pair is a [SortedDict](https://grantjenks.com/docs/sortedcontainers/sorteddict.html).
Each sorted dict is a dictionary of keys representing price levels (which are integers) and values
representing size (TODO: size levels?). It is sorted by keys, i.e. by price (TODO: are bids reverse
sorted?). You never should write to this dictionary directly, and normally you do not read from it
directly as well.

#### on_depth

```python
# Call this method to update your order book object with a new depth event.
bm.on_depth(order_book, is_bid, price_level, size_level)
```

This function updates the order book object according to received depth event.

#### get_bbo
```python
# Call this method to get the current best bid and ask levels.
# Returns ((best_bid, bid_size), (best_ask, ask_size)), or None instead of side tuple.
(best_bid_price_level, best_bid_size_level), (best_ask_price_level, best_ask_size_level) = bm.get_bbos(order_book)
```

This function is used to get best bid and ask levels at the current moment.

#### get_sum
```python
# Call this method to get the sum of size levels up to `levels_num` levels, per side.
# Returns (bids sum, asks sum).
bids_size_levels_sum, asks_size_levels_sum = bm.get_sum(order_book, levels_num)
```

Returns the sum of size levels per side, up to `levels_num` levels, where each level is one `pips`.

For example, if `levels_num` is 5, then the function will return the sum of size levels of 5 first
price levels, separately for bids and asks. 

If `levels_num` is 1, you will receive size levels at the best bid and ask (same size levels as
`get_bbo` returns).

Note that a certain price level will be taken into account even if it has a zero size level. For
example, if `levels_num` is 2, and best bid is at price level 100 of size level 1, 99 is empty,
the method will compute `1 + 0` and return `1`.

#### create_mbo_book()

```python
# Call this method to create an MBO order book entity.
mbo_order_book = bm.create_mbo_book()
```

This function creates a data structure object that represents a MBO order book. It is a
helper for dealing with tracking the MBO order book. `bm.create_mbo_book()` returns a dictionary
representing orders in the order book.

The first key is `"orders"`, it contains
[SortedDict](https://grantjenks.com/docs/sortedcontainers/sorteddict.html) where keys are order
ids and values are tuple with info about order, `(side: bool, price: int, size: int)`.

The second key is `"mbp_book"`. It is possible to this MBP order book (it's same to the one created
via [create_order_book](#create_order_book)).

#### on_new_order
```python
# Call this method to update your MBO order book object with a new order event.
bm.on_new_order(mbo_order_book, order_id, is_bid, price_level, size_level)
```

Use this function when receiving a new MBO event with the `ASK_NEW` or `BID_NEW` event type.
See [add_mbo_handler](#add_mbo_handler) for more details.

The order ID must not yet exist in `mbo_order_book. `ValueError` will be raised otherwise.

#### on_replace_order
```python
# Call this method to update your MBO order book object with a replace order event.
bm.on_replace_order(mbo_order_book, order_id, new_price, new_size)
```

Use this function when receiving a new MBO event with the `REPLACE` event type. See
[add_mbo_handler](#add_mbo_handler) for more details.

The order ID must exist in `mbo_order_book`. `ValueError` will be raised otherwise.

#### on_remove_order
```python
# Call this method to update your MBO order book object with a remove order event.
bm.on_remove_order(mbo_order_book, order_id)
```

Use this function when receiving a new MBO event with the `CANCEL` event type. See
[add_mbo_handler](#add_mbo_handler) for more details.

The order ID must exist in `mbo_order_book`. `ValueError` will be raised otherwise.

#### get_all_order_ids
```python
# Call this method to get all order IDs in the order book.
all_order_ids = bm.get_all_order_ids(mbo_order_book)
```

This function returns all order ids registered in the order book.

#### has_order
```python
# Call this method to check if the order with specified order id exists in the order book.
bm.has_order(mbo_order_book, order_id)
```

This function returns true if the order with specified order ID exists.

#### get_order
```python
# Call this method to get order info by order id.
order_info_or_none = bm.get_order(mbo_order_book, order_id)
if order_info_or_none is not None:
    side, price_level, size_level = order_info_or_none
```

This function returns side, price level and size level in tuple if order with the specified order ID
exists, otherwise it returns None.

#### get_order_price
```python
# Call this method to get price level of a certain order ID.
price_level = bm.get_order_price(mbo_order_book, order_id)
```

This function returns the order's price level, or raises `ValueError` if there is no such order.

#### get_order_size
```python
# Call this method to get size level of a certain order ID.
size_level = bm.get_order_size(mbo_order_book, order_id)
```

This function returns the order's size level, or raises `ValueError` if there is no such order.

#### get_order_side
```python
# Call this method to get side of a certain order ID.
side = bm.get_order_side(mbo_order_book, order_id)
```

This function returns order side, True if bid, otherwise False. Throw `ValueError` if there is no such order.
