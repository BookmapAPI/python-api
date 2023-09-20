package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.SendOrderEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import velox.api.layer1.data.OrderDuration;
import velox.api.layer1.data.SimpleOrderSendParameters;
import velox.api.layer1.data.SimpleOrderSendParametersBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SendOrderConverter implements EventConverter<String, SendOrderEvent> {

    private final JsonParser parser = new JsonParser();;

    @Inject
    SendOrderConverter(){}

    @Override
    public SendOrderEvent convert(String entity) {
        String[] tokens = entity.split(FIELDS_DELIMITER);
        String json = tokens[1];

        JsonObject object = parser.parse(json).getAsJsonObject();
        // Mandatory parameters that should always be in received json
        String alias = object.get("alias").getAsString();
        boolean isBuy = object.get("isBuy").getAsBoolean();
        int size = object.get("size").getAsInt();

        SimpleOrderSendParametersBuilder builder = new SimpleOrderSendParametersBuilder(alias, isBuy, size);

        if (object.has("limitPrice")) {
            builder.setLimitPrice(object.get("limitPrice").getAsDouble());
        }
        if (object.has("stopPrice")) {
            builder.setStopPrice(object.get("stopPrice").getAsDouble());
        }
        if (object.has("duration")) {
            builder.setDuration(OrderDuration.valueOf(object.get("duration").getAsString()));
        }
        if (object.has("clientId")) {
            builder.setClientId(object.get("clientId").getAsString());
        }
        if (object.has("takeProfitOffset")) {
            builder.setTakeProfitOffset(object.get("takeProfitOffset").getAsInt());
        }
        if (object.has("stopLossOffset")) {
            builder.setStopLossOffset(object.get("stopLossOffset").getAsInt());
        }
        if (object.has("stopLossTrailingStep")) {
            builder.setStopLossTrailingStep(object.get("stopLossTrailingStep").getAsInt());
        }
        if (object.has("takeProfitClientId")) {
            builder.setTakeProfitClientId(object.get("takeProfitClientId").getAsString());
        }
        if (object.has("stopLossClientId")) {
            builder.setStopLossClientId(object.get("stopLossClientId").getAsString());
        }

        SimpleOrderSendParameters orderSendParameters = builder.build();

        return new SendOrderEvent(
                orderSendParameters
        );
    }
}
