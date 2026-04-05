package org.mangorage.mangobotplugin.commands.music.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandType;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;

public final class StatusCommand extends AbstractJDACommand {

    private final IMusicManager manager;

    public StatusCommand(IMusicManager manager) {
        super("status", "Check music status!");
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

        var player = playerOpt.get();

        String status;

        if (player.getPaused()) {
            status = "Paused ⏸";
        } else {
            status = "Playing ▶";
        }

        // Try to get track info if available (depends on your player implementation)
        String trackInfo = "Unknown track";

        try {
            var track = player.getTrack(); // adjust if your API differs
            if (track != null && track.getInfo() != null) {
                trackInfo = track.getInfo().getTitle();
            }
        } catch (Exception ignored) {
            // If your API doesn’t support it, just ignore
        }

        context.reply("**Status:** " + status + "\n**Track:** " + trackInfo).queue();

        return JDACommandResult.PASS;
    }
}