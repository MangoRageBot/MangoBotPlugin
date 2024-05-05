/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class SSLError  implements LogAnalyserModule{

	public void analyse(String str, Message messaje) {

		if (str.contains("net.minecraftforge.installertools")&&str.contains("sun.security.validator.PKIXValidator")) {
				messaje.reply("This issue is in most cases caused by an outdated version of Java with issues with Let's Encrypt SSL. Please Update to a newer build of Java [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft). It can also be caused by networking issues.").setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}
	}

}
