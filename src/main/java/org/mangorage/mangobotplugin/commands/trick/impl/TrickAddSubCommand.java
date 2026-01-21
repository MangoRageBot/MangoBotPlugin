package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.BooleanArgumentType;
import org.mangorage.mangobotcore.api.command.v1.argument.types.EnumArgumentType;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.Trick;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.commands.trick.TrickType;

public final class TrickAddSubCommand extends AbstractJDACommand {
    private final TrickManager trickManager;
    private final RequiredArg<String> trickArg = registerRequiredArgument("trick", "The trick to add", StringArgumentType.single());
    private final RequiredArg<TrickType> trickTypeArg = registerRequiredArgument("type", "The trick type", EnumArgumentType.of(TrickType.class));
    private final RequiredArg<Boolean> trickSuppressArg = registerRequiredArgument("suppress", "Whether to suppress output", BooleanArgumentType.INSTANCE);
    private final RequiredArg<String> trickDataArg = registerRequiredArgument("data", "The trick data", StringArgumentType.quote());

    public TrickAddSubCommand(String name, TrickManager trickManager) {
        super(name);
        this.trickManager = trickManager;
    }

    @Override
    public JDACommandResult run(Message context, String[] arguments, CommandParseResult commandParseResult) throws Throwable {
        final var trickName = trickArg.get(arguments, commandParseResult);
        final var trickType = trickTypeArg.get(arguments, commandParseResult);
        final var trickSuppress = trickSuppressArg.get(arguments, commandParseResult);
        final var trickData = trickDataArg.get(arguments, commandParseResult);

        final var trick = new Trick(trickName, context.getGuildIdLong());

        if (trickManager.getTrickForGuildByName(context.getGuildIdLong(), trickName) != null) {
            context.reply("A trick with that name already exists!").queue();
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
        trick.setLastUserEdited(context.getAuthor().getIdLong());
        trick.setOwnerID(context.getAuthor().getIdLong());

        trickManager.addTrick(trick);
        context.reply("Added Trick '" + trickName + "' of type '" + trickType + "'!").queue();

        return JDACommandResult.PASS;
    }
}
