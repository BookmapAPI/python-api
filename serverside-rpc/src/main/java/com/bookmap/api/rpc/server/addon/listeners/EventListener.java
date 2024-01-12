package com.bookmap.api.rpc.server.addon.listeners;

import com.bookmap.addons.broadcasting.api.view.listeners.LiveEventListener;
import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.BroadcastingEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The listener that is passed to BrAPI.
 * The add-on provider will broadcast its live events to this listener.
 */
public class EventListener implements LiveEventListener {

    private final EventLoop eventLoop;
    private final String alias;
    private final Map<Class<?>, Field[]> classToFields = new ConcurrentHashMap<>();

    public EventListener(EventLoop eventLoop, String alias) {
        this.eventLoop = eventLoop;
        this.alias = alias;
    }

    @Override
    public void giveEvent(Object o) {
        if (o != null) {
            String event;
            try {
                event = convertEventToJSON(o);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException("Error during parsing event object", e);
            }
            eventLoop.pushEvent(new BroadcastingEvent(Type.BROADCASTING, alias, event));
        }
    }

    private String convertEventToJSON(Object o) throws IllegalAccessException {
        JsonObject jsonObject = new JsonObject();
        Class<?> clazz = o.getClass();
        if (classToFields.containsKey(clazz)) {
            Field[] fields = classToFields.get(clazz);
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(o);
                jsonObject.addProperty(fieldName, fieldValue.toString());
            }
        } else {
            Field[] fields = clazz.getDeclaredFields();
            List<Field> fieldNames = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);

                String fieldName = field.getName();
                if (fieldName.equals("serialVersionUID")) {
                    continue;
                }
                fieldNames.add(field);
                Object fieldValue = field.get(o);
                jsonObject.addProperty(fieldName, fieldValue.toString());
            }
            classToFields.put(clazz, fieldNames.toArray(new Field[0]));
        }

        return jsonObject.toString();
    }
}