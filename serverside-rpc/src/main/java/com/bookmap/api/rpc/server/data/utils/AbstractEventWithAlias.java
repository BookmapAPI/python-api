package com.bookmap.api.rpc.server.data.utils;

public class AbstractEventWithAlias extends AbstractEvent {

	public String alias;

	public AbstractEventWithAlias(Type type, String alias) {
		super(type);
		this.alias = alias;
	}
}
