package com.bookmap.api.rpc.server.data.income.converters.modules;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.income.converters.AddFieldEventConverter;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.utils.EventTypeMapKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class AddFieldEventConverterModule {
	@Binds
	@IntoMap
	@EventTypeMapKey(Type.ADD_SETTING_FIELD)
	abstract EventConverter<String, ? extends AbstractEvent> converter(AddFieldEventConverter addFieldEventConverter);
}