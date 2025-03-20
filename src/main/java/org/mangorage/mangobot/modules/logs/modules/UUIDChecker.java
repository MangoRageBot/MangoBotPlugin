package org.mangorage.mangobot.modules.logs.modules;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobot.modules.logs.LogAnalyserModule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UUIDChecker implements LogAnalyserModule {
    private static final Pattern pattern = Pattern.compile("--uuid,\\s*([a-fA-F0-9-]+)");

    public static String extractUUID(String input) {
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    @Override
    public void analyse(String str, Message message) {
        var lines = str.split("\n");
        for (String line : lines) {
            if (line.contains("--username") && line.contains("--uuid")) {
                var id = extractUUID(line);
                if (id != null) {
                    message.reply(
                            "[Found Profile](https://namemc.com/search/%s)".formatted(id)
                    ).setSuppressEmbeds(true).mentionRepliedUser(true).queue();
                    break;
                }
            }
        }
    }
}
