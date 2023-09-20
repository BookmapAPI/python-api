package com.bookmap.api.rpc.server.data.utils;

import dagger.MapKey;

@MapKey
public @interface EventTypeMapKey {
	Type value();
}
