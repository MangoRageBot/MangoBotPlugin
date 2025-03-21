package org.mangorage.mangobot.modules.logs;

import java.util.List;

import org.mangorage.mangobot.modules.logs.modules.BrokenDrivers;
import org.mangorage.mangobot.modules.logs.modules.EarlyWindow;
import org.mangorage.mangobot.modules.logs.modules.Java22;
import org.mangorage.mangobot.modules.logs.modules.LastStack;
import org.mangorage.mangobot.modules.logs.modules.MissingDeps;
import org.mangorage.mangobot.modules.logs.modules.RenewableLogAnalyser;

public interface LogAnalyserModule {
	String LS = System.getProperty("line.separator");

	LogAnalyser MAIN = LogAnalyser.of(
			new BrokenDrivers(),
			new EarlyWindow(),
			new Java22(),
			new MissingDeps(),
			new RenewableLogAnalyser(LastStack::new),
			LogAnalyser.createModule(
					(s, m) -> {
						m.append("\n").append("This is a common issue on Modrinth Theseus. Modrinth's launcher has been known to be problematic in some cases with Forge. If you need to download a Modrinth format modpack you can use Prism Launcher, GDLauncher, ATLauncher, or others which are far more reliable.");
					},
					List.of(
							"Invalid registry value type detected for PerfOS counters",
							"com.modrinth.theseus"
					)
			),
			LogAnalyser.createModule(
					(s, m) -> {
						m.append("\n").append("This issue is in most cases caused by an outdated version of Java with issues with Let's Encrypt SSL. Please Update to a newer build of Java [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft). It can also be caused by networking issues.");
					},
					List.of(
							"net.minecraftforge.installertools",
							"sun.security.validator.PKIXValidator"
					)
			),
			LogAnalyser.createModule(
					(s, m) -> {
						m.append("\n").append("Use Java 8. [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft).");
					},
					List.of(
							"jdk.internal.loader.ClassLoaders$AppClassLoader cannot be cast to class java.net.URLClassLoader"
					)
			),
			LogAnalyser.createModule(
					(s, m) -> {
						m.append("\n").append("You are using old Java version. Use Java 17 for 1.17-1.20.4 or Java 21 for 1.20.5+. [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft).");
					},
					List.of(
							"Current Java is",
							"but we require at least"
					)
			),
			LogAnalyser.createModule(
					(s, m) -> {
						m.append("\n").append("You are using old Java version. Use Java 17 for 1.17-1.20.4 or Java 21 for 1.20.5+. [Guide](https://mikumikudance.jp/index.php?title=Installing_Java_For_Minecraft).");
					},
					List.of(
							"Error: could not open",
							"user_jvm_args.txt"
					)
			),
			LogAnalyser.createModule(
					(s, m) -> {
						m.append("\n").append("Update FeatureCreep");
					},
					List.of(
							"Caused by: java.lang.IllegalArgumentException: Missing scheme",
							"org.jboss.modules"
					)
			)
	);

	void analyse(String str, StringBuilder message);
}
