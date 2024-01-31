package com.bookmap.api.rpc.server.data.outcome.converters.modules;

import com.bookmap.api.rpc.server.data.outcome.converters.PositionUpdateConverter;
import com.bookmap.api.rpc.server.data.outcome.converters.ProviderStatusConverter;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.utils.Type;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ProviderStatusConverterModule {

	@Binds
	@IntoMap
	@EventTypeMapKey(Type.PROVIDERS_STATUS)
	abstract EventConverter<? extends AbstractEvent, String> converter(ProviderStatusConverter converter);
}
