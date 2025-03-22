/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs.modules;

import org.mangorage.mangobot.modules.logs.LogAnalyserModule;

public class BrokenDrivers implements LogAnalyserModule {

	public void analyse(String str, StringBuilder message) {
		if (str.contains("EXCEPTION_ACCESS_VIOLATION") && str.contains("atio6axx.dll")) {
			message.append("\n").append(
					"Updating your drivers might help. Please bear in mind that checking for updates the usual ways won't find any updates when drivers are in a broken state, so it's important you follow the linked guide. Important: If you have Nvidia graphics, also make sure you've set anything Minecraft-related (such as Java and launchers) to prioritise high performance graphics in both the Windows settings and Nvidia control panel. Read this guide to fix them: https://forums.minecraftforge.net/topic/125488-rules-and-frequently-asked-questions-faq/#:~:text=How%20do%20I%20update%20my%20drivers%3F"
			);
		} else if (str.contains("EXCEPTION_ACCESS_VIOLATION") && str.contains("nouveau")) {
			message.append("\n").append(
					"Some older versions sometimes have a few issues with some Nouveau Graphics on early loading screen"
			);
		} else {
			String last = null;

			for (String line : LogAnalyserModule.split(str)) {
				last = line;
			}

			if (last != null) {
				if (last.contains("Backend library: LWJGL") || last.contains("Trying GL version") || last.contains("you probably have a driver issue")) {
					message.append("\n").append(
							"You have an issue with your Graphics Drivers. If you have an AMD/ATI GPU or APU update your AMD graphics drivers. If you have an NVIDIA graphics card make sure to mark the game and all instances of javaw.exe to use the dedicated graphics card. Read this guide: https://forums.minecraftforge.net/topic/125488-rules-and-frequently-asked-questions-faq/#:~:text=How%20do%20I%20update%20my%20drivers%3F"
					);
				}
			}
		}
	}

}
