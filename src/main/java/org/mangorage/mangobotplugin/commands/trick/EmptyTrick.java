package org.mangorage.mangobotplugin.commands.trick;


import org.mangorage.mangobotplugin.commands.trick.lua.MemoryBank;

import java.util.HashMap;

public final class EmptyTrick extends Trick {
    public static final EmptyTrick INSTANCE = new EmptyTrick();

    private EmptyTrick() {
        super(null, 0);
    }

    @Override
    public MemoryBank getMemoryBank() {
        return new MemoryBank(new HashMap<>());
    }
}
