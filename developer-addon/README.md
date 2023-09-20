# General information about Python API

Python API consists from 3 different projects: developer-addon, serverside-rpc
and client-rpc (last 2 are in the same gitlab repository that called l1-api-rpc)

### developer-addon

This project is UI interface for Python API. It includes:
- Python code editor
- Examples of existing addons in resource folder
- Mechanism for building `${your-addon-name}.jar`

  (the actual `python-api.jar` is build in this project)

### serverside-rpc

This project is server part of rpc communication between `${your-addon-name}.jar` and
python in runtime. It includes:
- Income event handler
- Outcome event handler
- Mechanism to communicate with Bookmap core and with python in runtime

  (to build and test your changes you should build `serverside-rpc.jar` in appropriate project and put
it into resources folder in developer-addon project and build the last one into `python-api-${version}.jar`.
If you want to build unobfuscated .jar then you should specify filename in `DefaultBuildService#build()`)


## How to develop Python API addon

To develop Python API addon you should clone [l1-api-rpc](https://gitlab.dev.bookmap.com/connectivity/l1-api-rpc)
and [developer-addon](https://gitlab.dev.bookmap.com/connectivity/developer-addon) to your local machine.
Notice that to you shouldn't open full l1-api-rpc like a project in IntelliJ IDEA rather you should open
folder serverside-rpc as a project (IDEA can't open such projects that consists of 2 parts properly)


## Project structure

Python Api based on rpc. Workflow has next core events:

1. Create addon using python
2. Build addon into `.jar`
3. Run `.jar` and python code simultaneously

The `.jar` is some kind of connector between python code and core. It handles all calls from python
and sends it to core by using Bookmap API as well it handles all events from core and sends it to python.
