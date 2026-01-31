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
        super(name, "Remove Trick");
        this.trickManager = trickManager;
        this.trickArg = registerRequiredArgument(
                "trick",
                "The trick name",
                StringArgumentType.single()
        );
    }

    @Override
    public List<String> aliases() {
        return List.of("r");
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var message = commandContext.getContextObject();
        final var trickName = commandContext.getArgument(trickArg);
        final var trick = trickManager.getTrickForGuildByName(message.getGuildIdLong(), trickName);

        if (trick == null) {
            message.reply("No trick found with ID: " + trickName).queue();
            return JDACommandResult.PASS;
        } else if (trick.isLocked()) {
            message.reply("Trick is locked!").queue();
            return JDACommandResult.PASS;
        } else {
            trickManager.removeTrick(trickName, trick.getGuildID());
            message.reply("Removed Trick " + trickName).queue();
            return JDACommandResult.PASS;
        }
    }
}
