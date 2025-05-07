/*
 * Copyright (c) 2023. MangoRage
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.mangorage.mangobotplugin.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.mangorage.commonutils.log.LogHelper;

public class MusicUtil {
    public static void connectToAudioChannel(VoiceChannel channel) {
        try {
            Guild guild = channel.getGuild();
            AudioManager audioManager = guild.getAudioManager();

            audioManager.setSendingHandler(MusicPlayer.getInstance(guild.getId()));

            audioManager.setSelfDeafened(true);
            audioManager.setSelfMuted(false);
            audioManager.setAutoReconnect(true);
            audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE);
            audioManager.setConnectTimeout(30_000);

            MusicPlayer.getInstance(guild.getId()).setVolume(5); // Default volume so nobody gets there ears torn out by sound.
            audioManager.openAudioConnection(channel);
        } catch (Exception e) {
            LogHelper.error("Failed to connect to voice channel: " + e.getMessage());
        }
    }

    public static void connectToAudioChannelNoMusic(VoiceChannel channel) {

        try {
            Guild guild = channel.getGuild();
            AudioManager audioManager = guild.getAudioManager();

            audioManager.setSelfDeafened(true);
            audioManager.setSelfMuted(false);
            audioManager.setAutoReconnect(true);
            audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE);
            audioManager.setConnectTimeout(30_000);

            audioManager.openAudioConnection(channel);
        } catch (Exception e) {
            LogHelper.error("Failed to connect to voice channel: " + e.getMessage());
        }
    }

    public static void leaveVoiceChannel(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
    }

    public static void registerRemoteSources(AudioPlayerManager playerManager) {
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
    }
}
