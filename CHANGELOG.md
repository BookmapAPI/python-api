## Version 0.2.0
- Updated BM max version to 7.9.

## Version 0.1.9
- Fixed BMP-8740 - 'Liquidity Tracker' add-on crashes when turned on/off frequently.

## Version 0.1.8
- Fixed BMP-8612 - A BM crashed when closing
- Fixed BMP-8638 - PyAdd-on crashes when a instrument is removed.
- Fixed BMP-8613 - (Performance) | Excessive CPU load

## Version 0.1.7
- Fixed BMP-8563 - The add-on and BM crash when the add-on is launched for two instruments

## Version 0.1.5
- Added Broadcasting API lib v0.57.
- Fixed BMP-8259 - NullPointerException in HandlerManager.

## Version 0.1.1
- make ctrl+f search
- make ctrl+b build
- make ctrl+h replace
- add dictionary for supported features in `handle_subscribe_instrument`

## Version 0.1.0
- rename library from `pyl1api` to `bookmap`
- add trading functionality, for more details see [README](https://github.com/BookmapAPI/python-api/blob/master/README.md)
- interval handler added with `add_on_interval_handler` now also receives instrument 
`alias` (previously only `addon` was received)
Before: `on_interval(addon)`
Now: `on_interval(addon, alias)`
