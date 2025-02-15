package org.mangorage.mangobot.website.impl;

import java.util.HashMap;

public final class ObjectMap {
    private final HashMap<Object, Object> map = new HashMap<>();

    public <T> T get(Object object, Class<T> tClass) {
        return (T) map.get(object);
    }

    public void put(Object id, Object o) {
        map.put(id, o);
    }

    public <T> T putAndReturn(Object id, T object) {
        put(id, object);
        return object;
    }
}
