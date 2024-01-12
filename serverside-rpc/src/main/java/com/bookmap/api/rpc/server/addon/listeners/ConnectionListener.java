package com.bookmap.api.rpc.server.addon.listeners;

import com.bookmap.addons.broadcasting.api.view.listeners.ConnectionStatusListener;

/**
 * A listener that will be notified by BrAPI when the state of the connection to the provider changes.
 */
public class ConnectionListener implements ConnectionStatusListener {

    private boolean connectionStatus = false;


    @Override
    public void reactToStatusChanges(boolean status) {
        this.connectionStatus = status;
    }

    public boolean isConnected(){
        return connectionStatus;
    }
}
