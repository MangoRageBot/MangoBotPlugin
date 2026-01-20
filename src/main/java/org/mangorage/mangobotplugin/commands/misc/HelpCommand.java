package org.mangorage.mangobotplugin.commands.misc;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.ICommandDispatcher;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;

public class HelpCommand extends AbstractJDACommand {
    private final ICommandDispatcher<Message, JDACommandResult> commandDispatcher;
    private final RequiredArg<String> commandArg;

    public HelpCommand(String name, ICommandDispatcher<Message, JDACommandResult> commandDispatcher) {
        super(name);
        this.commandDispatcher = commandDispatcher;
        this.commandArg = registerRequiredArgument(
                "command",
                "The command to get help for",
                StringArgumentType.single()
        );
    }

    @Override
    public JDACommandResult run(Message message, String[] arguments, CommandParseResult commandParseResult) throws Throwable {
        final String commandName = commandArg.get(arguments, commandParseResult);
        var command = commandDispatcher.getCommand(commandName);
        if (command == null) {
            message.reply("Command `" + commandName + "` not found.").queue();
        } else {
            message.reply(
                    String.join("\n", command.buildUsage())
            ).queue();
        }
        return JDACommandResult.PASS;
    }
}
