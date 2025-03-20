package org.mangorage.mangobot.modules.logs.modules;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobot.modules.logs.LogAnalyserModule;

public final class UUIDChecker implements LogAnalyserModule {

    @Override
    public void analyse(String str, Message message) {
        System.out.println("Check");
    }
}
