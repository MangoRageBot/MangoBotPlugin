package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public interface LogAnalyserModule {

	void analyse(String str, Message message); 
}
