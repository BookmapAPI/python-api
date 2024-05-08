import json

import bookmap as bm

is_subscribed_to_market_pulse = False
"""Flag whether we are subscribed to Market Pulse"""

provider = 'com.bookmap.addons.marketpulse.app.MarketPulse'
"""Market Pulse provider full name"""

generator_name_to_is_subscribed = {}
"""Maps generator name to the flag whether we want to subscribe to the generator"""

generator_name_to_is_connected = {}
"""Maps generator name to the flag whether we are connected to the generator"""

generator_name_to_is_available = {}
"""Maps generator name to the flag whether the generator is available"""

generator_name_to_settings = {}
"""Maps generator name to the settings"""

generator_name_to_buy_signal = {}
"""Maps generator name to the flag whether the generator signals to buy"""

generator_name_to_sell_signal = {}
"""Maps generator name to the flag whether the generator signals to sell"""

alias_to_settings = {}
"""Maps alias to the settings"""

alias_to_signal_state = {}
"""Maps alias to the signal state where 0 - sell, 1 - buy, 2 - undefined signal"""


def handle_subscribe_instrument(_addon, alias, _full_name, _is_crypto, _pips, _size_multiplier, _instrument_multiplier,
                                _supported_features):
    global alias_to_settings, alias_to_signal_state

    alias_to_settings[alias] = {}
    alias_to_signal_state[alias] = 2
    # You can use html tags in the label e.g. <br> for new line or <b> for bold text
    bm.add_label_to_settings(addon, alias,
                             'This is an example of add-on that uses <b>Broadcasting</b> API<br><br>'
                             ' It uses Market Pulse widgets data to indicate buy/sell signals'
                             ' When you start adding Market Pulse widgets in the Market Pulse add-on, '
                             ' they will appear below this label with checkboxes. Checkboxes indicate whether '
                             ' you want to subscribe to the particular Market Pulse widget. '
                             ' Labels near checkboxes indicates the widget unique id (e.g. priceChange:123456) '
                             ' and an instrument alias separated by space.'
                             ' After you subscribed to some widgets (e.g. price change and volume pressure) the'
                             ' algo starts to listen to the Market Pulse events and when all subscribed widgets'
                             ' signal to buy or sell, the algo sends a message to the Bookmap.<br><br>')


def handle_unsubscribe_instrument(_addon, alias):
    print('Alias ' + alias + ' disconnected', flush=True)


def setting_change_handler(_addon, alias, setting_name, field_type, value):
    print('Setting change: ' + alias + ' ' + setting_name + ' ' + field_type + ' ' + str(value), flush=True)

    settings = alias_to_settings[alias]
    if setting_name not in settings:
        print('Setting ' + setting_name + ' not found', flush=True)
    else:
        settings[setting_name] = bool(value)
        print('Setting ' + setting_name + ' changed to ' + str(bool(value)), flush=True)
        if settings[setting_name]:
            print('Setting ' + setting_name + ' is true', flush=True)
            # Setting name contains generator name and alias separated by space, we need only generator name (id)
            bm.subscribe_to_generator(addon, provider, setting_name.split()[0], False)
            generator_name_to_is_subscribed[setting_name.split()[0]] = True
        else:
            print('Setting ' + setting_name + ' is false', flush=True)
            # There are no direct way to unsubscribe from the generator, so we need to set the flag to false
            generator_name_to_is_subscribed[setting_name.split()[0]] = False
            clear_signals_for_generator(setting_name.split()[0])


def clear_signals_for_generator(generator_name):
    if generator_name in generator_name_to_buy_signal:
        del generator_name_to_buy_signal[generator_name]
    if generator_name in generator_name_to_sell_signal:
        del generator_name_to_sell_signal[generator_name]


def broadcasting_status_handler(_addon, status):
    """
    This function is called when the status of the broadcasting provider is changed
    :param _addon:
    :param status:
    :return:
    """
    global is_subscribed_to_market_pulse, alias_to_settings, provider, \
        generator_name_to_buy_signal, generator_name_to_sell_signal, generator_name_to_is_connected
    print('Broadcasting status: ' + str(status), flush=True)
    for provider_name in status:
        if provider_name == provider:
            # Before connecting to any generator, we need to check if we are already subscribed to market pulse
            if not is_subscribed_to_market_pulse:
                is_subscribed_to_market_pulse = True
                print('Connecting to market pulse' + str(status), flush=True)
                # If not subscribed to market pulse, we need to subscribe to it
                bm.subscribe_to_generator(addon, provider)
                break
            else:
                # If subscribed to market pulse, we want to save all available generators
                available_generators = [json.loads(item) for item in status[provider]]
                # Get all available generator names, we can use them to subscribe to the generator
                available_generator_names = [item['generatorName'] for item in available_generators]
                for generator in available_generators:
                    generator_name = generator['generatorName']
                    generator_name_to_is_available[generator_name] = True
                    generator_settings = generator['settings']
                    alias = get_alias_from_generator_settings(generator_settings)
                    # If we don't know about this generator, we need to add it to our settings
                    if generator_name not in generator_name_to_is_subscribed:
                        # The overall state of the generator is unsubscribed
                        generator_name_to_is_subscribed[generator_name] = False
                        generator_name_to_is_connected[generator_name] = False
                        generator_name_to_settings[generator_name] = generator_settings
                        # Store the value for settings panel for particular aliases
                        settings = alias_to_settings[alias]
                        # Construct a name that will be shown in the settings panel
                        # It contains generator unique id and alias separated by space
                        generator_settings_name = generator_name + " " + alias
                        settings[generator_settings_name] = False
                        bm.add_boolean_settings_parameter(addon, alias, generator_settings_name, False)
                    else:
                        # If we already know about the generator, and the checkbox in add-on settings checked
                        # we need to subscribe to this generator
                        if alias_to_settings[alias][generator_name + ' ' + alias]:
                            print('Connecting to generator ' + generator_name, flush=True)
                            bm.subscribe_to_generator(addon, provider, generator_name, False)
                            generator_name_to_is_subscribed[generator_name] = True
                pending_delete = []
                for generator_name in generator_name_to_is_subscribed:
                    # If we know about generator, but it is not available anymore, we need to send a message about it
                    # and remove generator from subscribed generators
                    if generator_name not in available_generator_names:
                        if generator_name_to_is_available[generator_name]:
                            generator_name_to_is_available[generator_name] = False
                            pending_delete.append(generator_name)
                            alias = get_alias_from_generator_settings(generator_name_to_settings[generator_name])
                            print('Generator become unavailable ' + generator_name, flush=True)
                            bm.send_user_message(addon, alias,
                                                 'Generator ' + generator_name + ' ' + alias + ' become unavailable')
                for generator_name in pending_delete:
                    del generator_name_to_settings[generator_name]
                    clear_signals_for_generator(generator_name)
                    pending_delete.clear()


def broadcasting_handler(_addon, generator_name, event):
    """
    This function is called when the event from the generator is received
    :param _addon:
    :param generator_name:
    :param event:
    :return:
    """
    global generator_name_to_settings, alias_to_signal_state, generator_name_to_buy_signal, \
        generator_name_to_sell_signal, generator_name_to_is_connected
    alias = get_alias_from_generator_settings(generator_name_to_settings[generator_name])
    # Successfully connected to generator
    if generator_name in generator_name_to_is_subscribed:
        generator_name_to_is_connected[generator_name] = True

    if not does_generator_need_to_be_processed(generator_name):
        return

    threshold = float(generator_name_to_settings[generator_name]['params']['threshold'])
    # For different generators we have different fields for buy and sell signals
    if 'Pressure' in generator_name:
        buy_estimate = float(event['buyEstimate'])
        sell_estimate = float(event['sellEstimate'])
    else:
        buy_estimate, sell_estimate = float(event['estimate']), -float(event['estimate'])

    # Check if the estimate is above the threshold
    if buy_estimate >= threshold:
        generator_name_to_buy_signal[generator_name] = True
    elif sell_estimate >= threshold:
        generator_name_to_sell_signal[generator_name] = True
    else:
        generator_name_to_buy_signal[generator_name] = False
        generator_name_to_sell_signal[generator_name] = False

    # Check if all generators signal to buy at this moment
    all_buy_signals = len(generator_name_to_buy_signal) > 0
    for generator_name in generator_name_to_buy_signal:
        all_buy_signals &= generator_name_to_buy_signal[generator_name]
        if not all_buy_signals:
            break

    # Check if all generators signal to sell at this moment
    all_sell_signals = len(generator_name_to_sell_signal) > 0
    for generator_name in generator_name_to_sell_signal:
        all_sell_signals &= generator_name_to_sell_signal[generator_name]
        if not all_sell_signals:
            break

    number_of_generators = get_actual_generators_number()
    number_of_buy_signals = len(generator_name_to_buy_signal)
    number_of_sell_signals = len(generator_name_to_sell_signal)
    # Check if we have signals from all connected generators
    if number_of_generators == number_of_buy_signals or number_of_generators == number_of_sell_signals:
        # all_buy_signals - is a local event that all thresholds are crossed
        # all_sell_signals - is a local event that all thresholds are crossed
        # alias_to_signal_state - is a global state that all indicators signalises for buy or sell action
        if all_buy_signals:
            # If all generators signal to buy, and the global state is not buy, we need to send a message to Bookmap
            if alias_to_signal_state[alias] != 1:
                alias_to_signal_state[alias] = 1
                bm.send_user_message(addon, alias, "BUY " + alias)
        elif all_sell_signals:
            # If all generators signal to sell, and the global state is not sell, we need to send a message to Bookmap
            if alias_to_signal_state[alias] != 0:
                alias_to_signal_state[alias] = 0
                bm.send_user_message(addon, alias, 'SELL ' + alias)
        else:
            # If a global state is not undefined, we set it to undefined
            if alias_to_signal_state[alias] != 2:
                print('No signal', flush=True)
            alias_to_signal_state[alias] = 2


def get_alias_from_generator_settings(settings):
    # Generators has different settings structure, so we need to check all possible places where alias can be
    if 'aliases' in settings:
        return settings['aliases'][0]
    elif 'instrument' in settings['params']:
        return settings['params']['instrument']
    elif 'options' in settings['params']['multiInstrument']:
        return json.loads(settings['params']['multiInstrument'])['options'][0]
    return None


def does_generator_need_to_be_processed(generator_name):
    # If we want to subscribe to the generator, and it is connected then we process an event from it
    return generator_name in generator_name_to_is_subscribed and generator_name_to_is_subscribed[generator_name] and \
        generator_name in generator_name_to_is_connected and generator_name_to_is_connected[generator_name]


def get_actual_generators_number():
    # Count only those generators that we want to subscribe to and they are available
    count = 0
    for generator_name in generator_name_to_is_subscribed.keys():
        if generator_name in generator_name_to_is_available:
            if generator_name_to_is_subscribed[generator_name] and generator_name_to_is_available[generator_name]:
                count += 1
    return count


def broadcasting_settings_handler(_addon, generator_name, settings):
    """
    This function is called when the settings of the generator are changed
    :param _addon:
    :param generator_name:
    :param settings:
    :return:
    """
    global generator_name_to_settings
    print("Broadcasting settings: " + generator_name + " " + str(settings), flush=True)
    generator_name_to_settings[generator_name] = settings


if __name__ == "__main__":
    addon = bm.create_addon()
    # start addon, requires 3 arguments - addon itself, handler for subscribe event
    # and handler for unsubscribe event
    bm.start_addon(addon, handle_subscribe_instrument,
                   handle_unsubscribe_instrument)

    bm.add_broadcasting_provider_status_handler(addon, broadcasting_status_handler)
    bm.add_on_setting_change_handler(addon, setting_change_handler)
    bm.add_broadcasting_handler(addon, broadcasting_handler)
    bm.add_broadcasting_settings_handler(addon, broadcasting_settings_handler)
    # block python execution giving control over the script to Bookmap only, so you
    # do not risk, that your script will be turned off earlier that you decide
    bm.wait_until_addon_is_turned_off(addon)
