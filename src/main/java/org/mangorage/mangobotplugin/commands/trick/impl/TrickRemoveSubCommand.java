package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;

import java.util.List;

public final class TrickRemoveSubCommand extends AbstractJDACommand {


    private final TrickManager trickManager;
    private final RequiredArg<String> trickArg;

    public TrickRemoveSubCommand(String name, TrickManager trickManager) {
        super(name);
        this.trickManager = trickManager;
        this.trickArg = registerRequiredArgument(
                "trick",
                "The trick name",
                StringArgumentType.single()
        );
    }

    @Override
    public List<String> getCommandNotes() {
        return List.of(
                "Description:",
                "Trick Remove Command"
        );
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var message = commandContext.getContextObject();
        final var trickName = commandContext.getArgument(trickArg);
        if (trickManager.removeTrick(trickName, message.getGuildIdLong())) {
            message.reply("Successfully removed trick: " + trickName).queue();
        } else {
            message.reply("No trick found with ID: " + trickName).queue();
        }
        return JDACommandResult.PASS;
    }
}
