/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs;

import net.dv8tion.jda.api.entities.Message;

public class MissingScheme  implements LogAnalyserModule{

	public void analyse(String str, Message messaje) {

		if (str.contains("Caused by: java.lang.IllegalArgumentException: Missing scheme")
				&& str.contains("org.jboss.modules")) {
			messaje.reply("Update FeatureCreep").setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}

	}

}
