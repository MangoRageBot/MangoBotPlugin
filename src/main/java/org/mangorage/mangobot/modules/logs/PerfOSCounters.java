/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class PerfOSCounters  implements LogAnalyserModule{

	public void analyse(String str, Message messaje) {

		if (str.contains("Invalid registry value type detected for PerfOS counters")&&str.contains("com.modrinth.theseus")) {
				messaje.reply("This is a common issue on Modrinth Theseus. Do not use Modrinth or their launcher, it is not good, especially on Forge. If you need to download a Modrinth format modpack you can use Prism Launcher, GDLauncher, ATLauncher, SKLauncher, or others which are far more reliable.").setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}
	}

}
