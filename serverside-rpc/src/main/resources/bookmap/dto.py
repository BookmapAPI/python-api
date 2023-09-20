import json


class OrderSendParameters:
    alias: str
    is_buy: bool
    size: int
    limit_price: int
    stop_price: int
    take_profit_offset: int
    stop_loss_offset: int
    stop_loss_trailing_step: int
    take_profit_client_id: str
    stop_loss_client_id: str
    duration: str
    client_id: str

    def __init__(self, alias, is_buy, size):
        self.alias = alias
        self.is_buy = is_buy
        self.size = size
        self.limit_price = None
        self.stop_price = None
        self.take_profit_offset = None
        self.stop_loss_offset = None
        self.stop_loss_trailing_step = None
        self.take_profit_client_id = None
        self.stop_loss_client_id = None
        self.duration = None
        self.client_id = None

    def to_json(self):
        # Create a dictionary to hold the non-None fields
        data = {
            "alias": self.alias,
            "isBuy": self.is_buy,
            "size": self.size,
        }

        # Add optional fields if they are not None
        if self.limit_price is not None:
            data["limitPrice"] = self.limit_price

        if self.stop_price is not None:
            data["stopPrice"] = self.stop_price

        if self.take_profit_offset is not None:
            data["takeProfitOffset"] = self.take_profit_offset

        if self.stop_loss_offset is not None:
            data["stopLossOffset"] = self.stop_loss_offset

        if self.stop_loss_trailing_step is not None:
            data["stopLossTrailingStep"] = self.stop_loss_trailing_step

        if self.take_profit_client_id is not None:
            data["takeProfitClientId"] = self.take_profit_client_id

        if self.stop_loss_client_id is not None:
            data["stopLossClientId"] = self.stop_loss_client_id

        if self.duration is not None:
            data["duration"] = self.duration

        if self.client_id is not None:
            data["clientId"] = self.client_id

        # Convert the dictionary to JSON and return it
        return json.dumps(data, indent=None)
