package com.bookmap.api.rpc.server.data.outcome.converters.modules;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.outcome.converters.ServerOffConverter;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ServerOffConverterModule {

	@Binds
	@IntoMap
	@EventTypeMapKey(Type.SERVER_OFF)
	abstract EventConverter<? extends AbstractEvent, String> converter(ServerOffConverter serverOffConverter);
}
