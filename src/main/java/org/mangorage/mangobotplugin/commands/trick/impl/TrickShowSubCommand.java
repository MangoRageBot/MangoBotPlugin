package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.commands.trick.TrickType;

import java.util.List;

public final class TrickShowSubCommand extends AbstractJDACommand {
    private final RequiredArg<String> trickNameArg;
    private final TrickManager trickManager;

    public TrickShowSubCommand(String name, TrickManager trickManager) {
        super(name);
        this.trickNameArg = registerRequiredArgument(
            "trick",
            "The trick name",
            StringArgumentType.single()
        );
        this.trickManager = trickManager;
    }

    @Override
    public List<String> getCommandNotes() {
        return List.of(
                "Description:",
                "Trick Show Command"
        );
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var message = commandContext.getContextObject();
        final var trickName = commandContext.getArgument(trickNameArg);
        var trick = trickManager.getTrickForGuildByName(
                message.getGuild().getIdLong(),
                trickName
        );

        if (trick == null) {
            message.reply("Trick with name '" + trickName + "' not found.").queue();
            return JDACommandResult.PASS;
        } else {
            if (trick.getType() == TrickType.ALIAS) {
                trick = trickManager.getTrickForGuildByName(
                        message.getGuild().getIdLong(),
                        trick.getAliasTarget()
                );
            }
            switch (trick.getType()) {
                case NORMAL, ALIAS -> message.reply(trick.getContent()).queue();
                case SCRIPT -> message.reply("Trick '" + trickName + "' is a script trick. (Will add back)").queue();
            }
            trick.use();
            trickManager.saveTrick(trick);
        }

        return JDACommandResult.PASS;
    }
}
