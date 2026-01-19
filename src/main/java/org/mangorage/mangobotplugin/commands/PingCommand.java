package org.mangorage.mangobotplugin.commands;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.util.misc.Arguments;

public final class PingCommand extends AbstractJDACommand {

    public PingCommand(String name) {
        super(name);
    }

    @Override
    public JDACommandResult run(Message message, Arguments arguments) {
        message.reply("Pong!").queue();
        return JDACommandResult.PASS;
    }
}
