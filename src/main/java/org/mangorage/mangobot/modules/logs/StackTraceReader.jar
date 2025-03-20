package org.mangorage.mangobot.modules.logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Message;

public class StackTraceReader implements LogAnalyserModule {

	// 正则表达式用于匹配Java异常堆栈跟踪
	private static final Pattern STACK_TRACE_PATTERN = Pattern.compile("(?m)(^\\S.*(?:\\r?\\n[ \\t]+at\\s+.*)+)");
	// 正则表达式用于匹配包含org.spongepowered.asm.mixin的异常并提取JSON文件名（不包括refmap）
	private static final Pattern JSON_FILE_PATTERN = Pattern.compile("(\\S+\\.json)(?=[: ])");
	// 正则表达式用于匹配{}内的内容
	private static final Pattern BRACE_CONTENT_PATTERN = Pattern.compile("\\{([^}]+)\\}");

	public static String nl = System.lineSeparator();

	
	
	
	List<String> sm_config = new ArrayList<>();
	Map<String, Boolean> jars = new LinkedHashMap<>();// FATAL
	Map<String, Boolean> modids = new LinkedHashMap<>();// FATAL
	Map<String, Boolean> packs = new LinkedHashMap<>();// FATAL

	List<String> braceContents = new LinkedList<>();

	List<String> fatal_missing_classes = new ArrayList<String>();
	
	
	
	
	
	//These only contain the content but not lvl
	List<String> bad_jar = new ArrayList<String>();
	List<String> bad_modid = new ArrayList<String>();
	List<String> bad_package = new ArrayList<String>();
	StringBuilder build = new StringBuilder();

	
	@Override
	public void analyse(String log, Message message) {



		int lvl = 0;
		for (String trace : getFatalTraces(log).reversed()) {// Las ultimas son las más importante
			lvl++;
			this.processTrace(trace, true, lvl);
		}

		for (String trace : getTraces(log).reversed()) {// Las ultimas son las más importante
			lvl++;
			this.processTrace(trace, false, lvl);
		}

		List<String> jar_names = new ArrayList<String>();
		if (!jars.isEmpty()) {
			build.append(
					"**Found potentially problematic JAR files (Prioritise FATAL then Higher lvl then lower ln):**")
					.append(nl);
			for (Map.Entry<String, Boolean> jar : jars.entrySet()) {
				String[] lvl_info_arr = jar.getKey().split(Pattern.quote(" **lvl ** "));
				String lvl_info="";
				if(lvl_info_arr.length>1) {
					lvl_info=" **lvl ** "+lvl_info_arr[1];
				}else {
					System.out.println(lvl_info_arr[0]);
				}
				String jar_name = jar.getKey().split(".jar")[0] + ".jar"+lvl_info;
				if (!jar_names.contains(jar_name)) {
					if (jar.getValue()) {
						build.append("**Possibly Fatal:** ");
					}
					build.append(jar_name).append(nl);
					jar_names.add(jar_name);
				}
			}
		}
		
		
		
		if (!modids.isEmpty()) {
			build.append(
					"**Found potentially problematic modids (Prioritise FATAL then Higher lvl then lower ln):**")
					.append(nl);
			for (Map.Entry<String, Boolean> modid : modids.entrySet()) {
					if (modid.getValue()) {
						build.append("**Possibly Fatal:** ");
					}
					build.append(modid.getKey()).append(nl);				
			}
		}
		
		if (!packs.isEmpty()) {
			build.append(
					"**Found potentially problematic packages (Prioritise FATAL then Higher lvl then lower ln):**")
					.append(nl);
			for (Map.Entry<String, Boolean> pack : packs.entrySet()) {
					if (pack.getValue()) {
						build.append("**Possibly Fatal:** ");
					}
					build.append(pack.getKey()).append(nl);				
			}
		}
		
		if (!packs.isEmpty()) {
			build.append(
					"**Found potentially fatal mising classes:**")
					.append(nl);
			for (String miss : fatal_missing_classes) {
					build.append(miss).append(nl);				
			}
		}
		
		
		List<String> injected_configs = new LinkedList<String>();
		for (String content : braceContents.reversed()) {
			for (String ind : removeDuplicates(content.split(","))) {
				String cleansed = ind.replace("pl:runtimedistcleaner:A", "").replace("re:classloading", "")
						.replace("pl:mixin:APP:", "").replace("re:computing_frames", "")
						.replace("pl:accesstransformer:B", "").replace("pl:mixin:A", "").replace("xf:fml", "")
						.replace("featurecreep", "").replace("re:mixin", "");
				if (!injected_configs.contains(cleansed) && !cleansed.isEmpty()) {
					injected_configs.add(cleansed);
				}
			}
		}

		if (!injected_configs.isEmpty()) {
			build.append("**Found contents in {} (Top is most important, only top 8 shown):**").append(nl);
			int siz = 0;
			for (String conf : injected_configs) {
				if (siz <= 8) {
					build.append(conf.split(".json")[0].replace(".mixin", "").replace("mixin.", "")).append(nl);
					siz++;
				}
			}
		}

		if (!build.toString().isEmpty()) {
			message.reply(build.toString()).setSuppressEmbeds(true).mentionRepliedUser(true).queue();
			;
		}

	}

	public void processTrace(String trace, boolean fatal, int lvl) {

		List<String> jsonFiles = findJsonFilesInMixinExceptions(trace);

		if (!jsonFiles.isEmpty()) {
			for (String jsonFile : jsonFiles) {
				if (!sm_config.contains(jsonFile) && !jsonFile.endsWith(".refmap.json")) {
					sm_config.add(jsonFile);
					build.append("**Potentially Problematic SpongeMixin Config:** " + jsonFile).append(nl);
				}
			}
		} else {

			String[] arr = trace.split(nl);
			int line_num = 0;


			for (String untrimmed : arr) {
				line_num++;
				String line = untrimmed.trim();
				if (line.contains("[")) {// There is not always a Jar
					extractJarNamesSquareBracket(line, fatal, lvl, line_num);
				} else if (line.contains("/")) {// Some dev enviornments like ForgeGradle or dev orientated launchers
												// like TLauncher display the modID and layer, this is helpful
												// especially when the Jar cannot be found
					String[] arr_modid = line.split("/");
					if(arr_modid.length>1) {
					String modid = arr_modid[1].split("@")[0];
					if (!bad_modid.contains(modid) && !line.split("/")[0].startsWith("java.")&&!isModIDDenylisted(modid)&&line.startsWith("at")) {
						bad_modid.add(modid);
						modids.put(
								modid + " **lvl ** " + String.valueOf(lvl) + "** ln** " + String.valueOf(line_num),
								fatal);

					}
					}

				} else if (line.startsWith("at")) {// Sometimes we just gotta use packages
					int line_len = 25;
					if (line.length() - 11 < 25) {
						line_len = line.length() - 11;// ~[?:?] {re:mixin}
					}
					String pack = line.substring(3, line_len);
					if(!bad_package.contains(pack) && !packIsDenyListed(pack)) {
					packs.put(pack + " **lvl ** " + Integer.toString(lvl) + "** ln** " + Integer.toString(line_num),
							fatal);
					bad_package.add(pack);
					}
				} else if (line.contains("ClassNotFoundException") && fatal) {
					fatal_missing_classes.add("**FATAL** Missing Class: " + line);
				}

				Matcher braceMatcher = BRACE_CONTENT_PATTERN.matcher(line);
				while (braceMatcher.find()) {
					String content = braceMatcher.group(1).trim();
					if (!braceContents.contains(content)) {//TODO, readd levels
						braceContents.add(content);
					}
				}

			}

//			if (trace_contain(arr, "[")) {
//				for (String untrimmed : arr) {
//					String line = untrimmed.trim();
//					extractJarNamesSquareBracket(line, jars, false);
//				}
//			}

		}
	}

//	public boolean trace_contain(String[] arr, String cont) {
//		// TODO Auto-generated method stub
//		if (arr.length == 1) {
//			return arr[0].contains(cont);
//		}
//
//		for (int i = 1; i < arr.length; i++) {// start at 1 to only get trace
//			if (arr[i].contains(cont)) {
//				return true;
//			}
//		}
//
//		return false;
//	}

	private boolean isModIDDenylisted(String modid) {
		// TODO Auto-generated method stub
		if(modid.isBlank()) {return true;}
		
		String[] ids = {
				"java",
				"minecraft",
				"minecraftforge",
				"eventbus",
				"cpw.",
				"coremods",
				"featurecreep",
				"mixin",
				"accesstransformer",
				"authlib",
				"jdk.",
				"java.",
				"fmlloader",
				"fmlcore",
				"org.spongepowered.mixin",
				"fmlearlydisplay"
		};
		
		for(String id:ids) {
			if(modid.startsWith(id)) {
				return true;
			}
		}
		
		
		return false;
	}

	private boolean packIsDenyListed(String pack) {
		// TODO Auto-generated method stub
		String[] prefixes = {
				"java.",
				"net.minecraft.",
				"net.minecraftforge.",
				"com.mojang.",
				"cpw.",
				"featurecreep.",
				"jdk.",
				"sun.",
				"com.sun.",
				"org.lwjgl.",
				"org.apache.",
				"io.netty",
				"org.prismlauncher",
				"io.github.zekerzhayard",
				"org.multimc",
				"org.polymc",
				"org.tlauncher",
				
		};
		
		for(String prefix:prefixes) {
			if(pack.startsWith(prefix)) {
				return true;
			}
		}
		
		
		return false;
	}

	public static String[] removeDuplicates(String[] inputArray) {
		// 使用LinkedHashSet来保持插入顺序的同时去除重复项
		Set<String> set = new HashSet<>(Arrays.asList(inputArray));

		// 将去重后的set转回数组
		String[] resultArray = set.toArray(new String[0]);

		return resultArray;
	}

	private List<String> getFatalTraces(String log) {
		// TODO Auto-generated method stub
		List<String> ret = new ArrayList<String>();
		String[] lines = log.split(nl);
		int len = lines.length;
		for (int i = 0; i < len; i++) {
			String line = lines[i];
			if (line.contains("/FATAL]")) {
				if (i + 2 > len) {
				} else {
					StringBuilder trace = new StringBuilder();
					trace.append(lines[i + 1]);
					addFatalTraces(trace, lines, i + 2);
					ret.add(trace.toString());
				}
			}
		}
		return ret;
	}

	private void addFatalTraces(StringBuilder trace, String[] lines, int index) {
		// TODO Auto-generated method stub
		int len = lines.length;
		for (int i = index; i < len; i++) {
			String line = lines[i];
			if (line.trim().startsWith("at ")) {
				trace.append(line);
			} else {
				return;
			}

		}

	}

	private void extractJarNamesSquareBracket(String line, boolean fatal,int lvl, int line_num) {
		int startIdx = line.indexOf('[');
		int endIdx = line.indexOf(']');

		while (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
			String candidate = line.substring(startIdx + 1, endIdx);
			// Check if the candidate string ends with ".jar" or contains ".jar%23"
			if (candidate.contains(".jar") && !isJarDenied(candidate)) {
				if (!bad_jar.contains(candidate)) {
					bad_jar.add(candidate);
					jars.put(candidate + " **lvl ** " + Integer.toString(lvl) + "** ln** " + Integer.toString(line_num),
							fatal);
				}
			}
			// Look for the next '[' and ']'
			startIdx = line.indexOf('[', endIdx);
			endIdx = line.indexOf(']', endIdx + 1);
		}
	}

	public static List<String> getTraces(String log) {
		List<String> stackTraces = new ArrayList<>();
		Matcher matcher = STACK_TRACE_PATTERN.matcher(log);
		while (matcher.find()) {
			stackTraces.add(matcher.group());
		}
		return stackTraces;
	}

	public List<String> findJsonFilesInMixinExceptions(String logContent) {
		List<String> jsonFiles = new ArrayList<>();

		String[] lines = logContent.split("\r?\n");
		for (String line : lines) {
			if (line.contains("org.spongepowered.asm.mixin")) {
				Matcher matcher = JSON_FILE_PATTERN.matcher(line.trim());
				while (matcher.find()) {
					// Group 1 captures the mixin JSON file name
					if (matcher.group(1) != null) {
						jsonFiles.add(matcher.group(1));
					}
				}
			}
		}

		return jsonFiles;
	}

	private boolean isJarDenied(String jarName) {
		if (jarName.startsWith("fml")) {
			return true;
		}
		if (jarName.startsWith("forge-")) {
			return true;
		}
		if (jarName.contains("fmlcore")) {
			return true;
		}
		if (jarName.startsWith("mixin")) {
			return true;
		}

		if (jarName.startsWith("gson-")) {
			return true;
		}
		if (jarName.startsWith("eventbus")) {
			return true;
		}
		if (jarName.startsWith("featurecreep-")) {
			return true;
		}
		if (jarName.startsWith("server-")) {
			return true;
		}
		if (jarName.startsWith("modlauncher")) {
			return true;
		}
		if (jarName.startsWith("javafmllanguage")) {
			return true;
		}
		if (jarName.startsWith("client-")) {
			return true;
		}
		if (jarName.startsWith("lwjgl-")) {
			return true;
		}
		if (jarName.startsWith("netty-")) {
			return true;
		}
		if (jarName.startsWith("bootstraplauncher")) {
			return true;
		}
		if (jarName.startsWith("securejarhandler")) {
			return true;
		}
		if (jarName.startsWith("core-")) {
			return true;
		}
		if (jarName.startsWith("loader-")) {
			return true;
		}
		if (jarName.startsWith("language-")) {
			return true;
		}
		if (jarName.startsWith("minecraft-") && jarName.contains("server")) {
			return true;
		}
		if (jarName.startsWith("minecraft-") && jarName.contains("client")) {
			return true;
		}
		return false;
	}
}
