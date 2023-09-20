package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.income.converters.SendOrderConverter;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class SendOrderConverterModule {

    @Binds
    @IntoMap
    @EventTypeMapKey(Type.SEND_ORDER)
    abstract EventConverter<String, ? extends AbstractEvent> converter(SendOrderConverter converter);
}
