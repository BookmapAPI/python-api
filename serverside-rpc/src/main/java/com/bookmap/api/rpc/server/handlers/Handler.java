package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;

/**
 * Handler is an entity responsible for event handling. This is crucial interface because actual processing
 * of each event is handled by handler. Each handler decides on its own how to actually handle events, but
 * it is expected that each handler uses advantages of multithreading and handles as much as possible asynchronously.
 * Nothing guarantees that handler will be initially called in concurrent environment, hence if {@link this#handle(AbstractEvent)}
 * blocks main thread, new event won't be passed to processing.
 * <p>
 * Technically implementation of event handler can call another handler, however if it is required to generate a new event,
 * it is better to pass this event to {@link com.bookmap.api.rpc.server.EventLoop#pushEvent(AbstractEvent)} method. It might
 * be important from perspective of event synchronization, since {@link com.bookmap.api.rpc.server.EventLoop} guarantees
 * ordering of pushed events, so any synchronization work which might be required is limited to a handler entity to do.
 *
 * @param <T> type of the event
 */
public interface Handler<T extends AbstractEvent> {

	void handle(T event);
}
