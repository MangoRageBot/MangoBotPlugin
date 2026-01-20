package org.mangorage.mangobotplugin.commands.internal.homedepot;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class HomeDepotSendAlertSubCommand extends AbstractJDACommand {
    private final RequiredArg<String> taskIdArg;

    public HomeDepotSendAlertSubCommand() {
        super("alert");
        this.taskIdArg = registerRequiredArgument(
                "taskId",
                "The Task ID to send the alert for (e.g., TRA128)",
                StringArgumentType.single()
        );
    }

    @Override
    public JDACommandResult run(Message message, String[] argument, CommandParseResult commandParseResult) throws Throwable {
        final var taskId = taskIdArg.get(argument, commandParseResult);
        message.reply(
                createAssociateTask(taskId)
        ).queue();
        return JDACommandResult.PASS;
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
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "keep-alive");

            conn.setDoOutput(true);
            conn.getOutputStream().write(new byte[0]);

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
