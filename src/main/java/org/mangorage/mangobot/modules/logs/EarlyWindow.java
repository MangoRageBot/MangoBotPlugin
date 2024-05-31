/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class EarlyWindow  implements LogAnalyserModule{

	public void analyse(String str, Message message) {
		String nl = System.getProperty("line.separator");

		var lines = str.split(nl);
		if (lines.length > 0) {
			String last = lines[lines.length - 1];
			if (last.contains("Loading ImmediateWindowProvider fmlearlywindow")) {
				message.reply("Your FML Early Window is failing. "
						+ "This is in many cases caused by broken drivers. If you have a graphics card from NVIDIA, make sure you set MinecraftLauncher and all instances of Minecraft's javaw.exe files. If you do not have NVIDIA and have only AMD, you can update your drivers. [Guide](https://forums.minecraftforge.net/topic/125488-rules-and-frequently-asked-questions-faq/#:~:text=How%20do%20I%20update%20my%20drivers%3F)"+nl
						+ "If you are on an M1 Mac you should also make sure you are using an ARM version of Java not an Intel x64 one."+nl
						+ "If it still does not work after all these steps you can try to turn off the earlyWindowProver, though this can cause some issues with some mods."+nl		
						+ "To do this go to (.)minecraft/config/fml.toml"+nl
						+ "Edit earlyWindowProvider to be earlyWindowProvider=\"none\"")
						.setSuppressEmbeds(true).mentionRepliedUser(true).queue();
			}
		}
	}

}
