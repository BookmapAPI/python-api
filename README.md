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

To run the addon only latest stable Bookmap version is required and Python >= 3.6 installed on the
machine.

> Python 2 is not supported.

## API reference

### Library import

Every Bookmap API script has to import the `bookmap` library. You may want to set the alias to `bm`
for convenience.

```python
import bookmap as bm
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
import bookmap as bm

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

***Important note: every time, when you see size in integer format instead of float, the actual size
is the product of multiplication `size_multiplier` * `size`***

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

Below are listed many more event handlers, similar to the ones above, which Bookmap calls to
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
every 0.1 second. This is useful handler if you have to do some actions (like updating of indicator,
calculating some value) every fixed time interval.

Example of handler:

```python
def on_interval(
   addon: Any
   alias: str
) -> None:
    """
    This function is called at each 0.1 second time interval.

    :param addon: The addon state object that you received when calling `create_addon`.
    :param alias: The unique instrument name you receive in `handle_subscribe_instrument`.
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

#### add_on_order_executed_handler
```python
# Call this if you need to receive events of yours order execution. Create your own
# `on_order_executed` function.
bm.add_on_order_executed_handler(addon, on_order_executed)
```

Adds handler for order execution events. See
[subscribe_to_order_info](#subscribe_to_order_info).

Example of handler:

```python
# addon - entity received from create_addon function
# alias - string; it defines unique name of respective instrument
# event - dictionary; it represents an order execution event. It has this structure:
#               orderId: '3427547165' (string value)
#               size: 1 (integer value) (size_multiplier applied here)
#               price: 29232.3 (floating-point value)
#               executionId: '87f74e91935c4b82983b86afeb9edd4a' (string value)
#               time: 1691144611797 (integer value)
#               isSimulated: False (boolean value)
def on_order_executed(
    addon: Any,
    alias: str,
    event: Dict[str, Any]
) -> None:
    """
    This function is called each time there is a change in the order book.

    :param addon:   The addon state object that you received when calling `create_addon`.
    :param alias:   The alias (name) of the instrument.
    :param event:   The event dictionary representing an order execution event with the following keys:
                    - 'orderId': (string) unique order id.
                    - 'size': (int) integer size.
                    - 'price': (float) floating-point price.
                    - 'executionId': (string) execution id.
                    - 'time': (int) integer timestamp.
                    - 'isSimulated': (bool) boolean value representing whether the event is simulated.
    """
```

#### add_on_order_updated_handler
```python
# Use this method to set up a handler that will be triggered whenever there is an update to one of your orders.
# Create your own `on_order_updated` function to process the updated order information.
bm.add_on_order_updated_handler(addon, on_order_updated)
```
This function adds a handler for order update events. Whenever there is a change in the status
or attributes of an order, the on_order_updated function will be called.

Example of the handler function:

```python
# addon - An entity received from the create_addon function
# order_update - A dictionary representing the updated order. It has the following structure:
#                {
#                    'filledChanged': True/False (boolean value),
#                    'unfilledChanged': True/False (boolean value),
#                    'averageFillPriceChanged': True/False (boolean value),
#                    'durationChanged': True/False (boolean value),
#                    'statusChanged': True/False (boolean value),
#                    'limitPriceChanged': True/False (boolean value),
#                    'stopPriceChanged': True/False (boolean value),
#                    'stopTriggeredChanged': True/False (boolean value),
#                    'modificationTimeChanged': True/False (boolean value),
#                    'instrumentAlias': 'TOMOUSDT@BNF' (string value),
#                    'orderId': '36392434' (string value), USE THIS ID FOR ANY TRADING RELATED CALLS
#                    'isBuy': True/False (boolean value),
#                    'type': 'LMT' (string value),
#                    'clientId': '36392434' (string value),
#                    'doNotIncrease': True/False (boolean value),
#                    'filled': 0 (integer value),
#                    'unfilled': 5 (integer value),
#                    'averageFillPrice': nan (float value),
#                    'duration': 'GTC' (string value),
#                    'status': 'WORKING' (string value),
#                    'limitPrice': 1.0945 (float value),
#                    'stopPrice': nan (float value),
#                    'stopTriggered': True/False (boolean value),
#                    'modificationUtcTime': 1691395710837 (integer value),
#                    'isSimulated': False (boolean value),
#                    'isDuplicate': False (boolean value)
#                }
def on_order_updated(
    addon: Any,
    order_update: Dict[str, Any]
) -> None:
    """
    This function is called whenever there is an update to one of your orders.

    :param addon:        The addon state object that you received when calling `create_addon`.
    :param order_update: The dictionary representing the updated order with various key-value pairs.
    """
```
Using the add_on_order_updated_handler method, you can conveniently handle and respond to any changes or
updates in your orders within Bookmap.

#### add_on_position_update_handler
```python
# Use this method to add a handler for position update events. When a position update occurs,
# your custom `on_position_update` function will be called to process the updated position information.
bm.add_on_position_update_handler(addon, on_position_update)
```
This function adds a handler for position update events. The on_position_update function will be triggered
within particular interval. This is not a function that will be triggered ONLY if some changes in position
occurred.

Example of the handler function:

```python
# addon - An entity received from the create_addon function
# position_update - A dictionary representing the updated position. It has the following structure:
#                   {
#                       'instrumentAlias': 'BTCUSDT@BNF' (string value),
#                       'unrealizedPnl': -4.179 (float value),
#                       'realizedPnl': 1.888 (float value),
#                       'position': 2 (integer value),          (size_multiplier applied here)
#                       'averagePrice': 29276.95 (float value),
#                       'volume': 0 (integer value),            (size_multiplier applied here)
#                       'workingBuys': 0 (integer value),
#                       'workingSells': 0 (integer value),
#                       'isDuplicate': False (boolean value)
#                   }
def on_position_update(
    addon: Any,
    position_update: Dict[str, Any]
) -> None:
    """
    This function is called whenever there is an update to your position in an instrument.

    :param addon:           The addon state object that you received when calling `create_addon`.
    :param position_update: The dictionary representing the updated position with various key-value pairs.
    """
```
With the add_on_position_update_handler method, you can easily manage and respond to any changes in your
positions within Bookmap.

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

#### subscribe_to_order_info
```python
# Use this method to subscribe to order data. The `alias` parameter represents the instrument alias you receive
# in the `handle_subscribe_instrument` callback. The `req_id` is an identifier that you can set yourself to
# uniquely identify this subscription.
bm.subscribe_to_trades(addon, alias, req_id)
```

Sends a request for order data. There are two core events related to your orders: order update and order execute.
Subscribing to order data automatically triggers the `on_order_updated_handler` and
`on_order_executed_handler` callbacks, which are set up using the [add_on_order_updated_handler](#add_on_order_updated_handler)
and [add_on_order_executed_handler](#add_on_order_executed_handler) methods, respectively.

#### subscribe_to_position_updates
```python
# Use this method to subscribe to position update events. The `alias` parameter represents the instrument alias you receive
# in the `handle_subscribe_instrument` callback. The `req_id` is an identifier that you can set yourself to
# uniquely identify this subscription.
bm.subscribe_to_position_updates(addon, alias, req_id)
```
This function sends a request to subscribe to position update events. Each position update event provides
information about changes in your position for a specific instrument and is always a real-time event.
Similar to other subscription methods, a response is sent through a special callback once the
subscription is successfully established.

Once subscribed to position updates, the system will automatically trigger the `on_position_update` callback,
which is set up using the `add_on_position_update_handler` method.

By utilizing the `subscribe_to_position_updates` method, you can stay informed about changes in your positions,
enabling you to make timely decisions and efficiently manage your trading portfolio.

### Trading
The callbacks below will allow you to create custom trading strategies within Python API.
Note that it is mandatory to check `Is trading strategy` checkbox to allow addon to trade.

#### send_order
```python
def send_order(addon: Dict[str, Any],
               order_send_parameters: OrderSendParameters
               ) -> None:
```
This function allows you to send different types of orders for a specific instrument in Bookmap.

Parameters:

- addon: The addon state object that you received when calling `create_addon`.
- order_send_parameters: The class that represents an order you want to send.

#### OrderSendParameters

The `OrderSendParameters` class is designed to encapsulate the parameters required for placing
different types of trading orders within Bookmap. This class is used in conjunction with the
`send_order` function to provide flexibility when creating various types of orders for a specific financial instrument.

#### Class Attributes:

1. `alias` (str): A unique instrument name for the order.

2. `is_buy` (bool): A boolean value representing whether the order is a buy order (True) or a sell order (False).

3. `size` (int): An integer representing the quantity of the order to be placed.
   The actual size is calculated as `size_multiplier` from the `handle_subscribe_instrument` multiplied by `size`.
   For example, if you choose a size multiplier of 0.001 on a specific instrument and set `size` to 5,
   the order size will be calculated as 0.001 * 5 = 0.005.

4. `limit_price` (float, optional): The float value representing the
   limit price for limit or stop-limit orders.

5. `stop_price` (float, optional): The float value representing the stop price for stop or stop-limit
   orders.

6. `take_profit_offset` (int, optional): The offset for setting the take-profit price, if applicable.

7. `stop_loss_offset` (int, optional): The offset for setting the stop-loss price, if applicable.

8. `stop_loss_trailing_step` (int, optional): The step value for trailing stop-loss orders, if applicable.

9. `take_profit_client_id` (str, optional): The client identifier for take-profit orders, if applicable.

10. `stop_loss_client_id` (str, optional): The client identifier for stop-loss orders, if applicable.

11. `duration` (str, optional): A string representing the duration of the order (e.g., 'GTC' for Good
    'Til Cancelled, 'IOC' for Immediate or Cancel). Supported order types include GTC, ATO, ATC, DAY, DYP,
    FOK, GCP, GDP, GTD, GTT, IOC, and GTC_PO. Default is GTC.

12. `client_id` (str, optional): The client identifier for the order.

#### Constructor: `__init__`

The class constructor initializes the `OrderSendParameters` object with the following parameters:

- `alias` (str): The unique instrument name for the order.

- `is_buy` (bool): A boolean value indicating whether the order is a buy order (True) or a sell order (False).

- `size` (int): An integer representing the quantity of the order to be placed.

#### Usage:

The `OrderSendParameters` class is used to create an instance with the necessary parameters for a trading order.
Depending on the type of order you want to place, you may set attributes such as
`limit_price`, `stop_price`, `take_profit_offset`, `stop_loss_offset`, `stop_loss_trailing_step`,
`take_profit_client_id`, `stop_loss_client_id`, `duration`, and `client_id`.

#### Example:

```python
import bookmap as bm
# Creating an OrderSendParameters instance for a limit order to buy 10 units of a BTCUSDT@BNF with a limit price of 50.00
order_params = bm.OrderSendParameters("BTCUSDT@BNF", True, 10)
order_params.limit_price = 50.0
order_params.duration = "GTC"
order_params.client_id = "Client1"
```

#### Passing the order_params object to the send_order function to place the order

```python
import bookmap as bm
# Create an OrderSendParameters instance for a limit order to buy 10 units of BTCUSDT@BNF with a limit price of 30000.
order_params_limit_buy = bm.OrderSendParameters("BTCUSDT@BNF", True, 10)
order_params_limit_buy.limit_price = 30000

# Send the limit buy order
bm.send_order(addon, order_params_limit_buy)

# Create an OrderSendParameters instance for a stop-limit order to sell 5 units of
# BTCUSDT@BNF with a limit price of 31000 and a stop price of 30500.
order_params_stop_limit_sell = bm.OrderSendParameters("BTCUSDT@BNF", False, 5)
order_params_stop_limit_sell.limit_price = 31000
order_params_stop_limit_sell.stop_price = 30500

# Send the stop-limit sell order
bm.send_order(addon, order_params_stop_limit_sell)

# Create an OrderSendParameters instance for a stop order to buy 3 units of BTCUSDT@BNF with a stop price of 29000.
order_params_stop_buy = bm.OrderSendParameters("BTCUSDT@BNF", True, 3)
order_params_stop_buy.stop_price = 29000

# Send the stop buy order
bm.send_order(addon, order_params_stop_buy)
```

#### cancel_order
```python
def cancel_order(addon: Dict[str, Any],
                 alias: str,
                 order_id: str,
                 is_batch_end: bool = True,
                 batch_id: int = float("nan")) -> None:
```
This function is used to cancel one or multiple orders for a specific instrument in Bookmap.
If you want to cancel several orders in batch, you can specify the is_batch_end and batch_id
arguments. If only order_id is specified, only a single order will be cancelled.
If is_batch_end is set to False, a unique batch_id will be automatically generated.

Parameters:

- addon: The addon state object that you received when calling `create_addon`.
- alias: The instrument alias (name) for which you want to cancel the order.
- order_id: The unique identifier of the order to be cancelled. For batch cancellation,
  you will need to call several `bm.cancel_order` with proper specification of `is_batch_end` and `batch_id`.
- is_batch_end: A boolean value indicating whether this is the last order to cancel in the batch (default is True).
- batch_id: (Optional) An integer value representing the batch ID for the order cancellation
  (default is `float("nan")`). If not provided, a unique batch ID will be generated automatically.

Description:

The cancel_order function allows you to efficiently cancel orders for a specific instrument.
By using the batch cancellation feature (`is_batch_end=True`), you can cancel multiple orders
simultaneously by providing the appropriate `batch_id`. Alternatively, set `is_batch_end=False`
to generate an automatic batch ID. It is recommended to create your own `batch_id`

Example:

```python
import bookmap as bm
# Cancel a single order with order ID '4567' for instrument 'BTCUSDT@BNF'.
bm.cancel_order(addon, 'BTCUSDT@BNF', '4567')

# Cancel multiple orders in batch with order IDs '1234', '2345', '3456' for instrument 'ETHUSDT@BNF'.
bm.cancel_order(addon, 'ETHUSDT@BNF', '1234', is_batch_end=False, batch_id=123)
bm.cancel_order(addon, 'ETHUSDT@BNF', '2345', is_batch_end=False, batch_id=123)
bm.cancel_order(addon, 'ETHUSDT@BNF', '3456', is_batch_end=True, batch_id=123)
```

#### move_order
```python
def move_order(addon: Dict[str, Any],
               alias: str,
               order_id: str,
               limit_price: float,
               stop_price: float = float("nan")) -> None:
```
This function is used to move an existing order for a specific instrument in Bookmap.
By specifying the `limit_price` and `stop_price`, you can update the order with new values.
Set limit_price to `float("nan")` if the order doesn't have a limit price, and set `stop_price`
to `float("nan")` if the order doesn't have a stop price (`stop_price` is nan by default).

Parameters:

- addon: The addon state object that you received when calling `create_addon`.
- alias: The instrument alias (name) for which you want to move the order.
- order_id: The unique identifier of the order to be moved.
- limit_price: The float value representing the new limit price for the order.
  Set it to `float("nan")` if the order doesn't have a limit price.
- stop_price: (Optional) The float value representing the new stop price for the order.
  Set it to `float("nan")` if the order doesn't have a stop price (default is `float("nan")`).

Description:

The move_order function allows you to update the limit price and stop price of an existing order
for a specific instrument. If you want to modify the limit price only, provide the new `limit_price`
and set `stop_price` to `float("nan")`. If you want to modify both the limit and stop prices,
provide the new values for both `limit_price` and `stop_price`.

Example:

```python
import bookmap as bm
# Move an order with order ID '7890' for instrument 'BTCUSDT@BNF' with a new limit price of 35000.
bm.move_order(addon, 'BTCUSDT@BNF', '7890', 35000)

# Move an order with order ID '1234' for instrument 'ETHUSDT@BNF' with a new limit price of 1.300 and a new stop price of 1.250.
bm.move_order(addon, 'ETHUSDT@BNF', '1234', 1.300, 1.250)

# Move an order with order ID '4567' for instrument 'XRPUSDT@BNF' with a new stop price of 0.900. Order will no longer be limit
bm.move_order(addon, 'XRPUSDT@BNF', '4567', float("nan"), 0.900)
```

#### resize_order
```python
def resize_order(addon: Dict[str, Any],
                 alias: str,
                 order_id: str,
                 size: int) -> None:
```
This function is used to resize an existing order for a specific instrument in Bookmap.
By specifying the `size`, you can update the quantity of the order.

Parameters:

- addon: The addon state object that you received when calling `create_addon`.
- alias: The instrument alias (name) for which you want to resize the order.
- order_id: The unique identifier of the order to be resized.
- size: The integer value representing the new size (quantity) for the order (size_multiplier applied here)

Description:

The resize_order function allows you to modify the size (quantity) of an existing order for a specific instrument.
By providing the new size, you can increase or decrease the quantity of the order as needed.

Example:

```python
import bookmap as bm
# Resize an order with order ID '7890' for instrument 'BTCUSDT@BNF' to a new size of 15 units.
bm.resize_order(addon, 'BTCUSDT@BNF', '7890', 15)

# Resize an order with order ID '1234' for instrument 'ETHUSDT@BNF' to a new size of 8 units.
bm.resize_order(addon, 'ETHUSDT@BNF', '1234', 8)

# Resize an order with order ID '4567' for instrument 'XRPUSDT@BNF' to a new size of 20 units.
bm.resize_order(addon, 'XRPUSDT@BNF', '4567', 20)
```

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
