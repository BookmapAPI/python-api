package com.bookmap.api.rpc.server.data.utils;

/**
 * Converts one entity to another.
 *
 * @param <SRC> type of the source entity
 * @param <DST> type of the destination entity
 */
public interface EventConverter<SRC, DST> {

	String FIELDS_DELIMITER = "\uE000";

	DST convert(SRC entity);
}
