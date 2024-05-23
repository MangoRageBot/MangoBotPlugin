package org.mangorage.mangobot.modules.tricks.lua.helpers;

import java.util.Objects;

public class LuaObjectHelper {
    public Integer getInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {

        }
        return null;
    }

    public boolean isInteger(String value) {
        return getInteger(value) != null;
    }

    public boolean isSameNumber(Number number, Number number2) {
        return Objects.equals(number, number2);
    }
}
