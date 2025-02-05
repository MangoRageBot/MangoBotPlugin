package org.mangorage.mangobot.website.impl;

import java.util.HashMap;

public final class ObjectMap {
    private final HashMap<String, Object> map = new HashMap<>();

    public <T> T get(String id, Class<T> tClass) {
        return (T) map.get(id);
    }

    public void put(String id, Object o) {
        map.put(id, o);
    }

    public <T> T putAndReturn(String id, T object) {
        put(id, object);
        return object;
    }
}
