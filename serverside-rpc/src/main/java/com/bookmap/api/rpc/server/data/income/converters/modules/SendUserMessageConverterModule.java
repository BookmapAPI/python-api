package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.income.converters.SendUserMessageConverter;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.utils.Type;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class SendUserMessageConverterModule {

    @Binds
    @IntoMap
    @EventTypeMapKey(Type.SEND_USER_MESSAGE)
    abstract EventConverter<String, ? extends AbstractEvent> converter(SendUserMessageConverter converter);
}
