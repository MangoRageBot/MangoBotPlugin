package org.mangorage.mangobot.modules.tricks;

public enum TrickCMDType {
    ADD,
    REMOVE,
    MODIFY,
    SHOW,
    INFO,
    NONE;

    public static TrickCMDType getType(String s) {
        if (s.contains("-a"))
            return ADD;
        if (s.contains("-r"))
            return REMOVE;
        if (s.contains("-e"))
            return MODIFY;
        if (s.contains("-s"))
            return SHOW;
        if (s.contains("-i"))
            return INFO;

        return NONE;
    }
}
