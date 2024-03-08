package com.bookmap.api.rpc.server.addon.listeners.broadcasting;

import com.bookmap.addons.broadcasting.api.view.listeners.UpdateFilterListener;
import com.bookmap.api.rpc.server.log.RpcLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FilterListener implements UpdateFilterListener {

    private Object filter;
    private Method toFilter;
    private final boolean doesRequireFiltering;

    public FilterListener(boolean doesRequireFiltering) {
        this.doesRequireFiltering = doesRequireFiltering;
    }

    @Override
    public void reactToFilterUpdates(Object o) {
        System.out.println("Filter updated " + o);
        if (o != null) {
            System.out.println("Filter is not null");
            filter = o;
            try {
                toFilter = filter.getClass().getDeclaredMethod("toFilter", Object.class);
            } catch (NoSuchMethodException e) {
                RpcLogger.error("Error filter updating", e);
                throw new RuntimeException(e);
            }
        } else {
            RpcLogger.error("Passed filter is null");
        }
    }

    public Object toFilter(Object event) {
        if (filter != null && doesRequireFiltering) {
            try {
                return toFilter.invoke(filter, event);
            } catch (InvocationTargetException | IllegalAccessException e) {
                RpcLogger.error("Error during filtering", e);
                throw new RuntimeException(e);
            }
        }
        return event;
    }
}
