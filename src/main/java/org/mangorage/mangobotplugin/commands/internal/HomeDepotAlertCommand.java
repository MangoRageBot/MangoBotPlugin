package org.mangorage.mangobotplugin.commands.internal;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.commonutils.misc.Arguments;
import org.mangorage.mangobotcore.jda.command.api.CommandResult;
import org.mangorage.mangobotcore.jda.command.api.ICommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class HomeDepotAlertCommand implements ICommand {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public String id() {
        return "homedepot";
    }

    @Override
    public List<String> commands() {
        return List.of(id());
    }

    @Override
    public String usage() {
        return "!homedepot <locationID>";
    }

    @Override
    public CommandResult execute(Message message, Arguments arguments) {
        if (message.getAuthor().getIdLong() != 194596094200643584L) return CommandResult.DEVELOPERS_ONLY;
        if (message.isFromGuild()) return CommandResult.PASS;
        executor.submit(() -> {
            message.reply(
                    createAssociateTask(
                            arguments.getOrDefault(0, "")
                    )
            ).queue();
        });
        return CommandResult.PASS;
    }

    /**
     * Sends a POST request to Home Depot API to create an associate help task.
     * @param taskId The identifier for the task (e.g., "TRA128")
     * @return The server response as a String
     */
    public static String createAssociateTask(String taskId) {
        String targetUrl = "https://apionline.homedepot.com/v1/callForAssociateHelp/createTask/" + taskId;

        try {
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Setup Request
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json, text/plain, */*");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setRequestProperty("Origin", "https://www.homedepot.com");
            conn.setRequestProperty("Referer", "https://www.homedepot.com/");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");

            conn.setFixedLengthStreamingMode(0);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();

            // Read Response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    (responseCode < 400) ? conn.getInputStream() : conn.getErrorStream()))) {
                return br.lines().collect(Collectors.joining("\n"));
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
