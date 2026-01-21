package org.mangorage.mangobotplugin.commands.misc;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;

public final class PingCommand extends AbstractJDACommand {

    public PingCommand(String name) {
        super(name);
    }

    @Override
    public JDACommandResult run(Message message, CommandContext commandContext, CommandParseResult commandParseResult) {
        message.reply("Pong!").queue();
        return JDACommandResult.PASS;
    }
}
