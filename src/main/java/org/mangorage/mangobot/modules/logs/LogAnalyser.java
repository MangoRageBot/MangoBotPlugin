/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */

package org.mangorage.mangobot.modules.logs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public final class LogAnalyser {
	public static LogAnalyser of(LogAnalyserModule... modules) {
		return new LogAnalyser(List.of(modules));
	}

	public static LogAnalyserModule createModule(LogAnalyserModule module, List<String> strings) {
		return createModule(module, String::contains, strings);
	}

	public static LogAnalyserModule createModule(LogAnalyserModule module, BiPredicate<String, String> comparePredicate, List<String> strings) {
		return new LogAnalyserModule() {
			private final List<String> stringsList = strings;
			@Override
			public void analyse(String str, StringBuilder message) {
				boolean foundAll = true;
				for (String string : stringsList) {
					if (!comparePredicate.test(str, string)) {
						foundAll = false;
						break;
					}
				}
				if (foundAll)
					module.analyse(str, message);
			}
		};
	}


	private final Set<LogAnalyserModule> mods = new HashSet<>();
	
	private LogAnalyser(List<LogAnalyserModule> mods) {
		this.mods.addAll(mods);
	}

	public void add(LogAnalyserModule module) {
		this.mods.add(module);
	}

	public void addAll(LogAnalyserModule... modules) {
		this.mods.addAll(List.of(modules));
	}

	public void readLog(StringBuilder message, String log) {
		log = log.replaceAll("\\r?\\n", "\\n");
		for(LogAnalyserModule mod : mods) {
			mod.analyse(log, message);
		}
	}

}
