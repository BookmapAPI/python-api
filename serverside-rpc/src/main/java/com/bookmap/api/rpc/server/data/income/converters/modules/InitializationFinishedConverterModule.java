package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.income.converters.InitializationFinishedConverter;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class InitializationFinishedConverterModule {

	@Binds
	@IntoMap
	@EventTypeMapKey(Type.INITIALIZATION_FINISHED)
	abstract EventConverter<String, ? extends AbstractEvent> converter(InitializationFinishedConverter converter);
}
