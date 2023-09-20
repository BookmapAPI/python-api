package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.income.converters.AddPointToIndicatorEventConverter;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class AddPointToIndicatorEventConverterModule {

	@Binds
	@IntoMap
	@EventTypeMapKey(Type.ADD_POINT_TO_INDICATOR)
	abstract EventConverter<String, ? extends AbstractEvent> converter(AddPointToIndicatorEventConverter converter);
}
