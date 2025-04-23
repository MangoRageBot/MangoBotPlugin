package org.mangorage.mangobotplugin.commands.internal;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.mangorage.commonutils.misc.Arguments;
import org.mangorage.mangobotcore.jda.command.api.CommandResult;
import org.mangorage.mangobotcore.jda.command.api.ICommand;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class EmojiCommand implements ICommand {
    @Override
    public String id() {
        return "emoji";
    }

    @Override
    public List<String> commands() {
        return List.of("emoji");
    }

    @Override
    public String usage() {
        return """
                Emoji Usage:
                
                !emoji create <name/id> <emoji>
                !emoji create <name/id> <emojiName> <emojiId>
                """;
    }

    @Override
    public CommandResult execute(Message message, Arguments arguments) {
        var sub_cmd = arguments.get(0);
        if (sub_cmd.contains("create")) {
            var requester = message.getMember();
            if (!requester.hasPermission(Permission.ADMINISTRATOR)) {
                return CommandResult.NO_PERMISSION;
            } else {
                if (arguments.getArgs().length == 4) {
                    var ce = Emoji.fromFormatted("<:" + arguments.get(2) + ":" + arguments.get(3) + ">").asCustom();
                    try {
                        message.getGuild().createEmoji(
                                arguments.get(1),
                                Icon.from(ce.getImage().download().get())
                        ).queue(s -> {
                            message.reply("Successfully added new Emoji!").queue();
                        });
                    } catch (Throwable e) {
                        message.reply("Failed").queue();
                    }
                } else if (arguments.getArgs().length == 3) {
                    var ce = Emoji.fromFormatted(arguments.get(2)).asCustom();
                    try {
                        message.getGuild().createEmoji(
                                arguments.get(1),
                                Icon.from(ce.getImage().download().get())
                        ).queue(s -> {
                            message.reply("Successfully added new Emoji!").queue();
                        });
                    } catch (Throwable e) {
                        message.reply("Failed").queue();
                    }
                } else {
                    message.reply("Improper Usage").queue();
                }

                return CommandResult.PASS;
            }
        }
        return CommandResult.PASS;
    }
}
