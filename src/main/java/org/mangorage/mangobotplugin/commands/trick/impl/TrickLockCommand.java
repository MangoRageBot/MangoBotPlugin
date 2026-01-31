package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;

import java.util.List;

public final class TrickLockCommand extends AbstractJDACommand {

    private final TrickManager trickManager;
    private final boolean lock;
    private final RequiredArg<String> trickArg = registerRequiredArgument("trick", "The trick name", StringArgumentType.single());

    public TrickLockCommand(String name, String description, TrickManager trickManager, boolean lock) {
        super(name, description);
        this.trickManager = trickManager;
        this.lock = lock;
    }

    @Override
    public List<String> aliases() {
        return lock ? List.of("l") : List.of("u");
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var message = commandContext.getContextObject();
        final var trickName = commandContext.getArgument(trickArg);
        final var guildId = message.getGuildIdLong();

        final var trick = trickManager.getTrickForGuildByName(guildId, trickName);

        if (trick == null) {
            message.reply("A trick with that name doesn't exist!").queue();
            return JDACommandResult.PASS;
        }

        if (trick.isLocked() == lock) {
            if (lock) {
                message.reply("Trick is already locked!").queue();
            } else {
                message.reply("Trick is already unlocked!").queue();
            }
            return JDACommandResult.PASS;
        }

        if (trick.getOwnerID() != message.getAuthor().getIdLong()) {
            if (lock) {
                message.reply("Only trick owner can lock trick!").queue();
            } else {
                message.reply("Only trick owner can unlock trick!").queue();
            }
            return JDACommandResult.PASS;
        }

        trick.setLock(lock);
        trick.setLastEdited(System.currentTimeMillis());

        trickManager.saveTrick(trick);

        if (lock) {
            message.reply("🔒 Locked Trick '" + trickName + "'. It can no longer be modified.").queue();
        } else {
            message.reply("🔒 Unlocked Trick '" + trickName + "'. It can now be modified.").queue();
        }

        return JDACommandResult.PASS;
    }
}