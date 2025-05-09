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
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.yamusic.YandexMusicAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.mangorage.commonutils.log.LogHelper;
import org.mangorage.commonutils.misc.PagedList;
import org.mangorage.commonutils.misc.RunnableTask;
import org.mangorage.commonutils.misc.TaskScheduler;
import org.mangorage.entrypoint.MangoBotCore;
import org.mangorage.mangobotplugin.PagedListManager;
import org.mangorage.mangobotplugin.PagedListWithAction;

import java.util.concurrent.TimeUnit;

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
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());

        // Not Available in my area...
        //playerManager.registerSourceManager(new NicoAudioSourceManager());
        //playerManager.registerSourceManager(new YandexMusicAudioSourceManager());

        // Dead or Broken?
        // playerManager.registerSourceManager(new GetyarnAudioSourceManager());

        // Dead, was shutdown
        // playerManager.registerSourceManager(new BeamAudioSourceManager());


        if (MangoBotCore.isDevMode()) {
            // I dont want to expose files in any way in production, in dev mode its nice for testing...
            playerManager.registerSourceManager(new LocalAudioSourceManager());
        }

        // Goes last to avoid messing up any of the above...
        playerManager.registerSourceManager(new HttpAudioSourceManager());
    }

    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.length() == 0) sb.append(secs).append("s");

        return sb.toString().trim();
    }

    private static String getResult(PagedList.Page<String> page, int currentPage, int totalPage) {
        return """
                Song: (%s/%s)
                %s
                """.formatted(currentPage, totalPage, page.getEntries()[0]);
    }

    public static void sendSongs(MessageChannel channel, PagedListManager listManager, MusicPlayer player) {
        var songs = player
                .getTracks()
                .stream()
                .map(track -> {
                    return MarkdownUtil.maskedLink(track.getInfo().title, track.getInfo().uri);
                })
                .toArray(String[]::new);


        channel.sendMessage(
                        """
                        Getting List... 
                        """
        ).queue(m -> {
            var list = new PagedListWithAction((page, current, total) -> {
                m.editMessage(getResult(page, current, total)).queue();
            });

            list.get().rebuild(songs, 1);

            Button prev = Button.primary("prev".formatted(m.getId()), "previous");
            Button next = Button.primary("next".formatted(m.getId()), "next");

            m.editMessage(getResult(list.get().current(), list.get().getPage(), list.get().totalPages())).setActionRow(prev, next).queue();

            listManager.putList(m.getId(), list);
        });
    }
}
