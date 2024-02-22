package com.bookmap.api.rpc.server.addon.listeners.broadcasting;

import com.bookmap.addons.broadcasting.api.view.listeners.LiveEventListener;
import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.BroadcastingEvent;
import com.bookmap.api.rpc.server.utils.JsonUtil;

/**
 * The listener that is passed to BrAPI.
 * The add-on provider will broadcast its live events to this listener.
 */
public class EventListener implements LiveEventListener {

    private final EventLoop eventLoop;
    private final String generatorName;
    private final FilterListener filterListener;

    public EventListener(EventLoop eventLoop, String generatorName, FilterListener filterListener) {
        this.eventLoop = eventLoop;
        this.generatorName = generatorName;
        this.filterListener = filterListener;
    }

    @Override
    public void giveEvent(Object o) {
        if (o != null) {
            if (filterListener.toFilter(o) == null) {
                return;
            }
            String event;
            try {
                event = JsonUtil.convertObjectToJsonString(o);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException("Error during parsing event object", e);
            }
            eventLoop.pushEvent(new BroadcastingEvent(generatorName, event));
        }
    }
}
