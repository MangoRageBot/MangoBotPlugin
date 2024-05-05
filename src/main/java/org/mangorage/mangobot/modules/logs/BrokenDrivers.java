/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class BrokenDrivers {

	public static void analyse(String str, Message messaje) {

		if (str.contains("EXCEPTION_ACCESS_VIOLATION") && str.contains("atio6axx.dll")) {
			messaje.reply(
					"You have broken AMD or ATI Graphics Drivers, updating from Device Manager wont fix it. Read this guide to fix them: https://forums.minecraftforge.net/topic/125488-rules-and-frequently-asked-questions-faq/#:~:text=How%20do%20I%20update%20my%20drivers%3F")
					.setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		} else if (str.contains("EXCEPTION_ACCESS_VIOLATION") && str.contains("nouveau")) { // FUCKK I forgot the name
																							// of the full file that
																							// sometimes causes crashes,
																							// was it like nouveau.so ?
			messaje.reply(
					"Some older versions sometimes have a few issues with some Nouveau Graphics on early loading screen")
					.setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		} else {
			String last = null;

			String nl = System.getProperty("line.separator");

			for (String line : str.split(nl)) {
				last = line;
			}

			if (last != null) {

				if (last.contains("Backend library: LWJGL") || last.contains("Trying GL version")
						|| last.contains("you probably have a driver issue")) {
					messaje.reply(
							"You have an issue with your Graphics Drivers. If you have an AMD/ATI GPU or APU update your AMD graphics drivers. If you have an NVIDIA graphics card make sure to mark the game and all instances of javaw.exe to use the dedicated graphics card. Read this guide: https://forums.minecraftforge.net/topic/125488-rules-and-frequently-asked-questions-faq/#:~:text=How%20do%20I%20update%20my%20drivers%3F")
							.setSuppressEmbeds(true).mentionRepliedUser(true).queue();
				}

			}

		}

	}

}
