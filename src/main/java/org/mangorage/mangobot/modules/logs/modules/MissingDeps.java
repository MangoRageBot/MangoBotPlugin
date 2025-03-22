/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */
package org.mangorage.mangobot.modules.logs.modules;

import org.mangorage.mangobot.modules.logs.LogAnalyserModule;

public class MissingDeps  implements LogAnalyserModule {

	public void analyse(String str, StringBuilder message) {
		if (str.contains("Missing or unsupported mandatory dependencies:")) {

			StringBuilder out = new StringBuilder("Missing or unsupported mandatory dependencies: \n");

			for(String line: LogAnalyserModule.split(str)) {
					if (line.contains("Mod ID") && line.contains("Requested by") && line.contains("Expected range")) {
						out.append(line).append("\n");
					}
			}
			message.append("\n").append(out);
		}
	}
}
