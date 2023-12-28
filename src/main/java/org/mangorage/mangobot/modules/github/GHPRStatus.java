package org.mangorage.mangobot.modules.github;

import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mangorage.mangobot.MangoBotPlugin;
import org.mangorage.mangobotapi.core.plugin.PluginManager;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

public class GHPRStatus extends TimerTask {

    public static void save(int number, Path fileName) {
        try {
            File file = fileName.toFile();

            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write the number to the file
                writer.write(Integer.toString(number));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int get(Path fileName) {
        int result = 9743;
        File file = fileName.toFile();

        // Check if the file exists before reading
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Read the number from the file
                String line = reader.readLine();
                if (line != null) {
                    result = Integer.parseInt(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private final Path file;
    private final CorePlugin corePlugin;

    public GHPRStatus(CorePlugin corePlugin) {
        this.file = PluginManager.getPlugin("mangobot").getPluginDirectory().resolve("ghprstatus/data.txt");
        this.corePlugin = corePlugin;
        new Timer().scheduleAtFixedRate(this, 15 * 1000, 60 * 60 * 1000); // 60 minutes/1hr
    }

    @Override
    public void run() {
        int lastChecked = get(file);
        int number = lastChecked;

        try {
            String username = "RealMangoRage";
            String token = MangoBotPlugin.PASTE_TOKEN.get();

            GitHub github = GitHub.connect(username, token);

            GHRepository repository = github.getRepository("MinecraftForge/MinecraftForge");


            var PRS = repository.getPullRequests(GHIssueState.OPEN).stream().filter(pr -> {
                return pr.getNumber() > lastChecked;
            }).toList();

            if (PRS.isEmpty()) return;

            var channel = corePlugin.getJDA().getTextChannelById(1129095014094475285L);

            if (PRS.size() > 1) {

                channel.sendMessage("New Forge PRS: %s".formatted(PRS.size())).queue();

                for (GHPullRequest PR : PRS) {
                    System.out.println(PR.getNumber());
                    if (PR.getNumber() > number)
                        number = PR.getNumber();
                    channel.sendMessage(
                            "\u200E \u200E \u200E \u200E \u200E * PR: %s [%s](%s)"
                                    .formatted(
                                            PR.getTitle(),
                                            PR.getNumber(),
                                            PR.getHtmlUrl()
                                    )
                    ).setSuppressEmbeds(true).queue();
                }
                save(number, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
