package com.bookmap.api.rpc.server.addon.listeners;

import com.bookmap.addons.broadcasting.api.view.listeners.LiveConnectionStatusListener;

/**
 * The listener that will be notified by BrAPI when the provider's live data subscription state changes.
 */
public class LiveConnectionListener implements LiveConnectionStatusListener {
    private boolean liveConnectionStatus = false;

    @Override
    public void reactToStatusChanges(boolean status) {
        liveConnectionStatus = status;
    }

    public boolean isConnect() {
        return liveConnectionStatus;
    }
}
