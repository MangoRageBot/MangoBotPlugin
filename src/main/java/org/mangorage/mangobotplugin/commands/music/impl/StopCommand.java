package org.mangorage.mangobotplugin.commands.music.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotplugin.commands.music.AudioLoader;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;

public final class StopCommand extends AbstractJDACommand {
    private final IMusicManager manager;

    public StopCommand(IMusicManager manager) {
        super("stop", "Stops the music from playing!");
        this.manager = manager;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var guild = commandContext.getContextObject().getGuild();

        final var guildManager = manager.getOrCreate(guild.getIdLong());
        final var player = guildManager.getPlayer();

        player.ifPresent(lp -> {
            lp.stopTrack().subscribe();
        });

        return JDACommandResult.PASS;
    }
}
