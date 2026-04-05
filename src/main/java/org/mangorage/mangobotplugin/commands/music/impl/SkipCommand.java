package org.mangorage.mangobotplugin.commands.music.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandType;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;

public final class SkipCommand extends AbstractJDACommand {

    private final IMusicManager manager;

    public SkipCommand(IMusicManager manager) {
        super("skip", "Skip the current track!");
        this.manager = manager;
    }

    @Override
    public JDACommandType getCommandType() {
        return JDACommandType.GUILD;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var context = commandContext.getContextObject();
        final var guild = context.getGuild();

        final var guildManager = manager.getOrCreate(guild.getIdLong());

        var playerOpt = guildManager.getPlayer();

        if (playerOpt.isEmpty()) {
            context.reply("Nothing is playing.").queue();
            return JDACommandResult.PASS;
        }


        guildManager.next();

        context.reply("Skipped track! ⏭").queue();

        return JDACommandResult.PASS;
    }
}
