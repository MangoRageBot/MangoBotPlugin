package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.argument.OptionalFlagArg;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.EnumArgumentType;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.Trick;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.commands.trick.TrickType;

public final class TrickAddSubCommand extends AbstractJDACommand {
    private final TrickManager trickManager;
    private final RequiredArg<String> trickArg = registerRequiredArgument("trick", "The trick name", StringArgumentType.single());
    private final RequiredArg<TrickType> trickTypeArg = registerRequiredArgument("type", "The trick type", EnumArgumentType.of(TrickType.class));
    private final OptionalFlagArg trickSuppressArg = registerFlagArgument("--suppress", "Whether to suppress output");
    private final RequiredArg<String> trickDataArg = registerRequiredArgument("data", "The trick data", StringArgumentType.quote());

    public TrickAddSubCommand(String name, TrickManager trickManager) {
        super(name);
        this.trickManager = trickManager;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var message = commandContext.getContextObject();
        final var trickName = commandContext.getArgument(trickArg);
        final var trickType = commandContext.getArgument(trickTypeArg);
        final var trickSuppress = commandContext.getArgument(trickSuppressArg);
        final var trickData = commandContext.getArgument(trickDataArg);

        final var guildId = message.getGuildIdLong();
        final var trick = new Trick(trickName, guildId);

        if (trickManager.getTrickForGuildByName(guildId, trickName) != null) {
            message.reply("A trick with that name already exists!").queue();
            return JDACommandResult.PASS;
        }

        switch (trickType) {
            case NORMAL -> {
                trick.setType(TrickType.NORMAL);
                trick.setContent(trickData);
            }
            case ALIAS -> {
                trick.setType(TrickType.ALIAS);
                trick.setAliasTarget(trickData);
            }
            case SCRIPT -> {
                trick.setType(TrickType.SCRIPT);
                trick.setScript(trickData);
            }
        }

        trick.setSuppress(trickSuppress);
        trick.setLastUserEdited(message.getAuthor().getIdLong());
        trick.setOwnerID(message.getAuthor().getIdLong());

        trickManager.addTrick(trick);
        message.reply("Added Trick '" + trickName + "' of type '" + trickType + "'!").queue();

        return JDACommandResult.PASS;
    }
}
