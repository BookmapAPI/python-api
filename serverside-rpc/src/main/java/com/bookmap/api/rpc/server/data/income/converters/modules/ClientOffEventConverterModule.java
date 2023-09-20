package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.income.converters.ClientOffEventConverter;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ClientOffEventConverterModule {

	@Binds
	@IntoMap
	@EventTypeMapKey(Type.CLIENT_OFF)
	abstract EventConverter<String, ? extends AbstractEvent> converter(ClientOffEventConverter converter);
}
