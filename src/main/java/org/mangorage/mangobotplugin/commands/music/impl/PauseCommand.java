package org.mangorage.mangobotplugin.commands.music.impl;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandType;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;
import reactor.core.CoreSubscriber;

public final class PauseCommand extends AbstractJDACommand {

    private final IMusicManager manager;

    public PauseCommand(IMusicManager manager) {
        super("pause", "Pause the music!");
        this.manager = manager;
    }

    @Override
    public JDACommandType getCommandType() {
        return JDACommandType.GUILD;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var context = commandContext.getContextObject();
        final var guild = commandContext.getContextObject().getGuild();

        final var guildManager = manager.getOrCreate(guild.getIdLong());

        guildManager.getPlayer()
                .ifPresent(player -> {
                    player.setPaused(true).subscribe(plr -> {
                        context.reply("Paused Music!").queue();
                    });
                });

        return JDACommandResult.PASS;
    }
}
