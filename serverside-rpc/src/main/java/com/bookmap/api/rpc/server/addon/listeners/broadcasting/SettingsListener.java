package com.bookmap.api.rpc.server.addon.listeners.broadcasting;

import com.bookmap.addons.broadcasting.api.view.listeners.UpdateSettingsListener;
import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.BroadcastingSettingsEvent;
import com.bookmap.api.rpc.server.utils.JsonUtil;

public class SettingsListener implements UpdateSettingsListener {

    private final EventLoop eventLoop;
    private final String generatorName;

    public SettingsListener(EventLoop eventLoop, String generatorName) {
        this.eventLoop = eventLoop;
        this.generatorName = generatorName;
    }

    @Override
    public void reactToSettingsUpdate(Object o) {
        if (o != null) {
            String event;
            try {
                event = JsonUtil.convertObjectToJsonString(o);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException("Error during parsing event object", e);
            }
            eventLoop.pushEvent(new BroadcastingSettingsEvent(generatorName, event));
        }
    }
}
