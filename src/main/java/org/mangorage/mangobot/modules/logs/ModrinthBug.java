/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class ModrinthBug  implements LogAnalyserModule{

	public void analyse(String str, Message message) {
		String nl = System.getProperty("line.separator");

		var lines = str.split(nl);
		if (lines.length > 0) {
			String last = lines[lines.length - 1];
			if (last.contains("Invalid registry value type detected for PerfOS counters") && str.contains("com.modrinth.theseus")) {
				message.reply("This is a common issue on Modrinth Theseus. Modrinth's launcher has been known to be problematic in some cases with Forge. If you need to download a Modrinth format modpack you can use a launcher more reliable with Forge which supports it format.")
						.setSuppressEmbeds(true).mentionRepliedUser(true).queue();
			}
		}
	}

}
