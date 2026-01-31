package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.argument.OptionalArg;
import org.mangorage.mangobotcore.api.command.v1.argument.OptionalFlagArg;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.EnumArgumentType;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.commands.trick.TrickType;

import java.util.List;

public final class TrickModifyCommand extends AbstractJDACommand {
    private final TrickManager trickManager;
    private final RequiredArg<String> trickArg = registerRequiredArgument("trick", "The trick name", StringArgumentType.single());
    private final OptionalArg<TrickType> trickTypeArg = registerOptionalArgument("type", "The trick type", EnumArgumentType.of(TrickType.class), TrickType.NORMAL);
    private final OptionalFlagArg trickSuppressArg = registerFlagArgument("--suppress", "Whether to suppress output");
    private final RequiredArg<String> trickDataArg = registerRequiredArgument("data", "The trick data", StringArgumentType.quote());

    public TrickModifyCommand(String name, TrickManager trickManager) {
        super(name, "Modify a trick!");
        this.trickManager = trickManager;
    }

    @Override
    public List<String> aliases() {
        return List.of("m");
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var message = commandContext.getContextObject();
        final var trickName = commandContext.getArgument(trickArg);
        final var trickType = commandContext.getArgumentOrElse(trickTypeArg);
        final var trickSuppress = commandContext.getArgument(trickSuppressArg);
        final var trickData = commandContext.getArgument(trickDataArg);

        final var guildId = message.getGuildIdLong();
        final var trick = trickManager.getTrickForGuildByName(guildId, trickName);

        if (trick == null) {
            message.reply("A trick with that name doesn't exist!").queue();
            return JDACommandResult.PASS;
        }

        if (trick.isLocked()) {
            message.reply("That trick is already locked!").queue();
            return JDACommandResult.PASS;
        }

        switch (trickType) {
            case NORMAL -> {
                trick.setType(TrickType.NORMAL);
                trick.setContent(trickData);
            }
            case ALIAS -> {
                trick.setType(TrickType.ALIAS);
                trick.setContent(trickData);
            }
            case SCRIPT -> {
                trick.setType(TrickType.SCRIPT);
                trick.setContent(trickData);
            }
        }

        trick.setSuppress(trickSuppress);
        trick.setLastUserEdited(message.getAuthor().getIdLong());
        trick.setLastEdited(System.currentTimeMillis());

        trickManager.saveTrick(trick);

        message.reply("Modified Trick '" + trickName + "' of type '" + trickType + "'!").queue();

        return JDACommandResult.PASS;
    }
}