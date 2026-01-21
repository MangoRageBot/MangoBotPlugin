package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.commands.trick.TrickType;

public final class TrickShowSubCommand extends AbstractJDACommand {
    private final RequiredArg<String> trickNameArg;
    private final TrickManager trickManager;

    public TrickShowSubCommand(String name, TrickManager trickManager) {
        super(name);
        this.trickNameArg = registerRequiredArgument(
            "trickName",
            "The name of the trick to show",
            StringArgumentType.single()
        );
        this.trickManager = trickManager;
    }

    @Override
    public JDACommandResult run(Message context, CommandContext commandContext, CommandParseResult commandParseResult) throws Throwable {
        final var trickName = commandContext.getArgument(trickNameArg, commandParseResult);
        var trick = trickManager.getTrickForGuildByName(
                context.getGuild().getIdLong(),
                trickName
        );

        if (trick == null) {
            context.reply("Trick with name '" + trickName + "' not found.");
            return JDACommandResult.PASS;
        } else {
            if (trick.getType() == TrickType.ALIAS) {
                trick = trickManager.getTrickForGuildByName(
                        context.getGuild().getIdLong(),
                        trick.getAliasTarget()
                );
            }
            switch (trick.getType()) {
                case NORMAL, ALIAS -> context.reply(trick.getContent()).queue();
                case SCRIPT -> context.reply("Trick '" + trickName + "' is a script trick. (Will add back)").queue();
            }
        }

        return JDACommandResult.PASS;
    }
}
