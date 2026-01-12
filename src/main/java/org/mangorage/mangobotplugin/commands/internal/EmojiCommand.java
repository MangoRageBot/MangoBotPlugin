package org.mangorage.mangobotplugin.commands.internal;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.mangorage.mangobotcore.api.jda.command.v1.CommandResult;
import org.mangorage.mangobotcore.api.jda.command.v1.ICommand;
import org.mangorage.mangobotcore.api.util.jda.slash.command.Command;
import org.mangorage.mangobotcore.api.util.jda.slash.command.CommandOption;
import org.mangorage.mangobotcore.api.util.misc.Arguments;

import java.util.List;

public final class EmojiCommand implements ICommand {

    public static String getInfo(final CustomEmoji emoji) {
        return """
                URL: %s
                Id: %s
                Name: %s
                Created: %s
                Formatted: %s
                Reaction Code: %s
                Mention: %s
                """
                .formatted(
                        emoji.getImageUrl(),
                        emoji.getId(),
                        emoji.getName(),
                        emoji.getTimeCreated().toString(),
                        emoji.getFormatted(),
                        emoji.getAsReactionCode(),
                        emoji.getAsMention()
                );
    }

    public EmojiCommand() {
        Command.slash("emoji", "Useful emoji Comamnd")
                .addSubCommand("info", "Get info about Emoji")
                .addOption(
                        new CommandOption(OptionType.STRING, "emoji", "The Emoji", true)
                )
                .executes(e -> {
                    final var valueOption = e.getInteraction().getOption("emoji");
                    if (valueOption != null) {
                        final var emoji = Emoji.fromFormatted(valueOption.getAsString()).asCustom();
                        e.reply(getInfo(emoji)).queue();
                    }
                })
                .build()
                .addSubCommand("create", "Create an Emoji")
                .addOptions(
                        new CommandOption(OptionType.STRING, "name", "The Name for the emoji you are creating", true),
                        new CommandOption(OptionType.STRING, "emoji", "The Actual Emoji you are wanting", true)
                )
                .executes(e -> {
                    if (!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        e.reply("Insufficient Permissions!").setEphemeral(true).queue();
                    } else {
                        final var name = e.getInteraction().getOption("name");
                        final var emoji = e.getInteraction().getOption("emoji");
                        if (name == null || emoji == null) {
                            e.reply("Incomplete Command").setEphemeral(true).queue();
                        } else {
                            final var ce = Emoji.fromFormatted(emoji.getAsString()).asCustom();

                            try {
                                e.getGuild().createEmoji(
                                        name.getAsString(),
                                        Icon.from(ce.getImage().download().get())
                                ).queue();

                                e.reply("Created Emoji!").setEphemeral(true).queue();
                            } catch (Throwable ignored) {
                                e.reply("Failed to create Emoji!").setEphemeral(true).queue();
                            }

                        }
                    }
                })
                .build()
                .buildAndRegister();
    }


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
                
                !emoji send <emoji>
                !emoji send <emojiName> <emojiId>
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
        } else if (sub_cmd.contains("info")) {
            if (arguments.getArgs().length == 3) {
                var ce = Emoji.fromFormatted("<:" + arguments.get(1) + ":" + arguments.get(2) + ">");
                message.reply(getInfo(ce.asCustom())).queue();
            } else if (arguments.getArgs().length == 2) {
                var ce = Emoji.fromFormatted(arguments.get(1));
                message.reply(getInfo(ce.asCustom())).queue();
            } else {
                message.reply("Improper Usage").queue();
            }
        }
        return CommandResult.PASS;
    }
}
