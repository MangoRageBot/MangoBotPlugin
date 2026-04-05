package org.mangorage.mangobotplugin.commands.music;

public interface IMusicManager {
    GuildMusicManager getOrCreate(long guildId);
}
