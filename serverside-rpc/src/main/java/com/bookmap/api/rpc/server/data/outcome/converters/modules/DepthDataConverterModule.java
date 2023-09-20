package com.bookmap.api.rpc.server.data.outcome.converters.modules;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.outcome.converters.DepthDataConverter;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class DepthDataConverterModule {

	@Binds
	@IntoMap
	@EventTypeMapKey(Type.DEPTH)
	abstract EventConverter<? extends AbstractEvent, String> converter(DepthDataConverter converter);
}
