package org.mangorage.mangobot.modules.tricks.lua;

public class LuaPrimitiveStringArray {
    private final String[] array;

    public LuaPrimitiveStringArray(String... array) {
        this.array = array;
    }

    public String get(int index) {
        return array[index];
    }

    public int getSize() {
        return array.length;
    }
}
