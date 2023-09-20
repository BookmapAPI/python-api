package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.income.converters.MoveOrderConverter;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.utils.Type;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class MoveOrderConverterModule {

    @Binds
    @IntoMap
    @EventTypeMapKey(Type.MOVE_ORDER)
    abstract EventConverter<String, ? extends AbstractEvent> converter(MoveOrderConverter converter);
}
