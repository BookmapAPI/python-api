package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.income.converters.SubscribeToGeneratorConverter;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.utils.Type;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class SubscribeToGeneratorConverterModule {

    @Binds
    @IntoMap
    @EventTypeMapKey(Type.REGISTER_BROADCASTING_PROVIDER)
    abstract EventConverter<String, ? extends AbstractEvent> converter(SubscribeToGeneratorConverter converter);
}
