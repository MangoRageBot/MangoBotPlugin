package org.mangorage.mangobotplugin.commands;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.ICommandDispatcher;
import org.mangorage.mangobotcore.api.command.v1.ICommandNode;
import org.mangorage.mangobotcore.api.jda.command.v1.CommandResult;
import org.mangorage.mangobotcore.api.jda.command.v1.ICommand;
import org.mangorage.mangobotcore.api.util.misc.Arguments;

import java.util.List;

public final class PingCommand {

    public static void register(String id, ICommandDispatcher dispatcher) {
        dispatcher.register(
                ICommandNode.create(id)
                        .requires(ctx -> ctx.hasType(Message.class))
                        .usage("Checks if the discord bot is running!")
                        .executes((ctx, args) -> {
                            ctx.get(Message.class).reply("Pong!").queue();
                            return CommandResult.PASS;
                        })
                        .build()
        );
    }
}
