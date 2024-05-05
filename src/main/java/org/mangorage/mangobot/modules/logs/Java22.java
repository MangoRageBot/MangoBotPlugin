/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class Java22  implements LogAnalyserModule{

	public void analyse(String str, Message messaje) {

		String out ="Please use Java 17 for 1.17-1.20.4 and Java 21 for Anything newer, Java 8 for anything older. [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft). If you still have issues it could be because some mod has too new or old of files.";
		if (str.contains("java.lang.IllegalArgumentException: Unsupported class file major version")) {
			if (str.contains("--fml.forgeVersion, 4") || str.contains("--fml.forgeVersion, 3")) {
				if (str.contains("java version 2") && !str.contains("java version 20") && !str.contains("java version 21")) {
				out = "Java 22 and above do not work on Minecraft Versions bellow 1.20.5 for most modloaders due to ASM being outdated." + out ;
				}
			}
			messaje.reply(out).setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}
	}

}
