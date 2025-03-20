/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs.modules;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobot.modules.logs.LogAnalyserModule;

public class MissingDeps  implements LogAnalyserModule {

	public void analyse(String str, Message message) {
		if (str.contains("Missing or unsupported mandatory dependencies:")) {
			String nl = System.getProperty("line.separator");
			
			String out = "Missing or unsupported mandatory dependencies:" + nl;

			for(String line: str.split(nl)) {
					if (line.contains("Mod ID") && line.contains("Requested by") && line.contains("Expected range")) {
						out = out + line + nl;
					}
			}
			message.reply(out).setSuppressEmbeds(true).mentionRepliedUser(true).queue();
		}
	}
}
