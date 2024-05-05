/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class WeRequireAtLeastJava17  implements LogAnalyserModule{

	
	


	
	public void analyse(String str, Message messaje) {

		if (str.contains("Current Java is") && str.contains("but we require at least")) {
			messaje.reply("You are using old Java version. Use Java 17 for 1.17-1.20.4 or Java 21 for 1.20.5+. [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft).").setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}else if (str.contains("Error: could not open") && str.contains("user_jvm_args.txt")) {
			messaje.reply("You are using old Java version. Use Java 17 for 1.17-1.20.4 or Java 21 for 1.20.5+. [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft).").setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}

	}
	
	
	
	
	
}
