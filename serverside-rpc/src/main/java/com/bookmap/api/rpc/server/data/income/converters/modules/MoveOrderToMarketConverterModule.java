package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.income.converters.MoveOrderToMarketConverter;
import com.bookmap.api.rpc.server.data.income.converters.SendOrderConverter;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.utils.Type;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class MoveOrderToMarketConverterModule {

    @Binds
    @IntoMap
    @EventTypeMapKey(Type.MOVE_ORDER_TO_MARKET)
    abstract EventConverter<String, ? extends AbstractEvent> converter(MoveOrderToMarketConverter converter);
}
