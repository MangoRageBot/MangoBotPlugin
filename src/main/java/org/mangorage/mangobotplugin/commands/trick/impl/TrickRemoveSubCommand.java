package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;

public final class TrickRemoveSubCommand extends AbstractJDACommand {
    private final TrickManager trickManager;

    public TrickRemoveSubCommand(String name, TrickManager trickManager) {
        super(name);
        this.trickManager = trickManager;
    }

    @Override
    public JDACommandResult run(Message context, String[] arguments, CommandParseResult commandParseResult) throws Throwable {
        context.reply("Removing tricks is under maintenance!").queue();
        return JDACommandResult.PASS;
    }
}
