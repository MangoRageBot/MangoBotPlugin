/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class URLClassLoaderIssue  implements LogAnalyserModule{

	
	public void analyse(String str, Message messaje) {

		if (str.contains("jdk.internal.loader.ClassLoaders$AppClassLoader cannot be cast to class java.net.URLClassLoader")) {
			messaje.reply("Use Java 8. [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft).").setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}

	}
	
	
	
	
	
}
