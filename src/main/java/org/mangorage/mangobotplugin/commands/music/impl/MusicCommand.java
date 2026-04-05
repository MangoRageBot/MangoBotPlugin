package org.mangorage.mangobotplugin.commands.music.impl;

import dev.arbjerg.lavalink.client.LavalinkClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandType;
import org.mangorage.mangobotplugin.commands.music.AudioLoader;
import org.mangorage.mangobotplugin.commands.music.GuildMusicManager;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;

import java.util.HashMap;
import java.util.Map;

public final class MusicCommand extends AbstractJDACommand implements IMusicManager {
    private final Map<Long, GuildMusicManager> musicManagerMap = new HashMap<>();
    private final LavalinkClient client;

    public MusicCommand(String name, LavalinkClient client) {
        super(name, "Music Commands!");
        this.client = client;
        addSubCommand(new JoinCommand(this));
        addSubCommand(new PlayCommand(this));
        addSubCommand(new PauseCommand(this));
        addSubCommand(new StopCommand(this));
    }

    @Override
    public JDACommandType getCommandType() {
        return JDACommandType.GUILD;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        return JDACommandResult.PASS;
    }


    @Override
    public GuildMusicManager getOrCreate(long guildId) {
        return musicManagerMap.computeIfAbsent(
                guildId,
                id -> new GuildMusicManager(guildId, client)
        );
    }
}
