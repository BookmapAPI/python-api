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
