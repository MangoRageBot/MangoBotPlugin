/*
 * This file is written by Asbestosstar. It is not copyrighted, and a Ruby version will be included in the FeatureCreep Moderation Bot which is also not copyrighted. 
 * Feel free to use this in your own software. Free as in Speech, Free as in Beer, No warranties, No Export restrictions.
 * */

package org.mangorage.mangobot.modules.logs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import org.mangorage.mangobot.core.Util;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobot.modules.logs.modules.BrokenDrivers;
import org.mangorage.mangobot.modules.logs.modules.EarlyWindow;
import org.mangorage.mangobot.modules.logs.modules.Java22;
import org.mangorage.mangobot.modules.logs.modules.MissingDeps;

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
			public void analyse(String str, Message message) {
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


	// Be sure to include slashes, Paste.ee and other sites without separate or
	// only raw URLs will not work, ones with fancy raw URLs like OpenSUSE paste
	// will also not be included at this time.
	public String[] supported_paste_sites = new String[] {
			"paste.mikumikudance.jp/",
			"paste.centos.org/",
			"pastebin.com/", "mclo.gs/"
	};

	public void scanMessage(Message message) {
		String content = message.getContentStripped();
		for (String uri : getLogURLs(content)) {
			InputStream log = Util.getFileInputStream(uri);
			if (log != null) {
				try {
					String str = Util.getStringFromInputStream(log);
					readLog(message, str);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}			
	}

	public ArrayList<String> getLogURLs(String message) {
		ArrayList<String> list = new ArrayList<>();
		for (String word : message.split(" ")) {

			for (String paste : supported_paste_sites) {
				if (word.contains(paste)) {

					if (paste.equals("mclo.gs/")) {
						String[] url_arr = word.split("/");
						String slug = url_arr[url_arr.length - 1];
						list.add("https://api.mclo.gs/1/raw/" + slug);
					} else if (paste.equals("pastebin.com/")) {
						String[] url_arr = word.split("/");
						String slug = url_arr[url_arr.length - 1];
						list.add("https://pastebin.com/raw/" + slug);
					} else { // Add more else ifs for other sites
						if (word.contains("/view/raw/")) {
							list.add(word);
						} else if (word.contains("/view/")) {
							list.add(word.replace("/view/", "/view/raw/"));
						}

					}

				}

			}

		}

		return list;
	}

	public void readLog(Message message, String log) {
		for(LogAnalyserModule mod : mods) {
			mod.analyse(log, message);
		}
	}

}
