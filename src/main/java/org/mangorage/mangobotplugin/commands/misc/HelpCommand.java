package org.mangorage.mangobotplugin.commands.misc;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.ICommandDispatcher;
import org.mangorage.mangobotcore.api.command.v1.argument.OptionalFlagArg;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;

public class HelpCommand extends AbstractJDACommand {
    private final ICommandDispatcher<Message, JDACommandResult> commandDispatcher;
    private final OptionalFlagArg advancedFlag = registerFlagArgument("--advanced", "Shows advanced information");
    private final OptionalFlagArg extraInfoFlag = registerFlagArgument("--extrainfo", "Shows extra information");
    private final RequiredArg<String> commandArg = registerRequiredArgument("command", "The command to get help for", StringArgumentType.single());

    public HelpCommand(String name, ICommandDispatcher<Message, JDACommandResult> commandDispatcher) {
        super(name);
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    public JDACommandResult run(Message message, CommandContext commandContext, CommandParseResult commandParseResult) throws Throwable {
        final boolean advanced = commandContext.getArgument(advancedFlag, commandParseResult);
        final boolean extraInfo = commandContext.getArgument(extraInfoFlag, commandParseResult);
        final String commandName = commandContext.getArgument(commandArg, commandParseResult);

        var command = commandDispatcher.getCommand(commandName);

        if (command == null) {
            message.reply("Command `" + commandName + "` not found.").queue();
        } else {
            final var cmdInfo = command.buildUsage(advanced || extraInfo);
            // Usage Info
            message.reply("""
                    Usages:
                    %s
                    """.formatted(
                            String.join("\n", cmdInfo.usages())
                    )
            ).queue();
            // Extra Info
            if (extraInfo) {
                message.reply("""
                                ExtraInfo:
                                %s
                                """.formatted(
                                String.join("\n", cmdInfo.extraInfo().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toList())
                        )
                ).queue();
            }
        }
        return JDACommandResult.PASS;
    }
}
