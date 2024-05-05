package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public interface LogAnalyserModule {

	public void analyse(String str, Message messaje);
	
}
