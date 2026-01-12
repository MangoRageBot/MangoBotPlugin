package org.mangorage.mangobotplugin.commands;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.jda.command.v1.CommandResult;
import org.mangorage.mangobotcore.api.jda.command.v1.ICommand;
import org.mangorage.mangobotcore.api.util.misc.Arguments;

import java.util.List;

public class PingCommand implements ICommand {
    @Override
    public String id() {
        return "ping";
    }

    @Override
    public List<String> commands() {
        return List.of("ping");
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public CommandResult execute(Message message, Arguments arguments) {
        message.reply("Pong!").queue();;
        return CommandResult.PASS;
    }
}
