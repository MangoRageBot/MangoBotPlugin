/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs.modules;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobot.modules.logs.LogAnalyserModule;

public class EarlyWindow  implements LogAnalyserModule {

	public void analyse(String str, StringBuilder message) {
		var lines = LogAnalyserModule.split(str);
		if (lines.length > 0) {
			String last = lines[lines.length - 1];
			if (last.contains("Loading ImmediateWindowProvider fmlearlywindow")) {
				message.append("\n").append("Your FML Early Window is failing. "
								+ "To Change this go to (.)minecraft/config/fml.toml"
								+ "Edit earlyWindowProvider to be earlyWindowProvider=\"none\""
								+ "If you are on an M1 Mac you should also make sure you are using an ARM version of Java not an Intel x64 one."
								+ "This is also a common issue if you have outdated Drivers. Please check this guide if on windows and turning of this does not work. https://forums.minecraftforge.net/topic/125488-rules-and-frequently-asked-questions-faq/#:~:text=How%20do%20I%20update%20my%20drivers%3F"
				);
			}
		}
	}

}
