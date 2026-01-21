package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;

public final class TrickRemoveSubCommand extends AbstractJDACommand {


    private final TrickManager trickManager;
    private final RequiredArg<String> trickArg;

    public TrickRemoveSubCommand(String name, TrickManager trickManager) {
        super(name);
        this.trickManager = trickManager;
        this.trickArg = registerRequiredArgument(
                "trick_id",
                "The Trick ID",
                StringArgumentType.single()
        );
    }

    @Override
    public JDACommandResult run(Message context, CommandContext commandContext, CommandParseResult commandParseResult) throws Throwable {
        final var trickName = commandContext.getArgument(trickArg, commandParseResult);
        if (trickManager.removeTrick(trickName, context.getGuildIdLong())) {
            context.reply("Successfully removed trick: " + trickName).queue();
        } else {
            context.reply("No trick found with ID: " + trickName).queue();
        }
        return JDACommandResult.PASS;
    }
}
