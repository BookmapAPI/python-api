package com.bookmap.api.rpc.server.data.outcome.converters.modules;

import com.bookmap.api.rpc.server.data.outcome.converters.OrderExecutionConverter;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import com.bookmap.api.rpc.server.data.utils.Type;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class OrderExecutionConverterModule {

	@Binds
	@IntoMap
	@EventTypeMapKey(Type.EXECUTE_ORDER)
	abstract EventConverter<? extends AbstractEvent, String> converter(OrderExecutionConverter converter);
}
