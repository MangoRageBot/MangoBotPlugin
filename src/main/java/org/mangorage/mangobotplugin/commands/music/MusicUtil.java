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
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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
import org.mangorage.mangobotcore.MangoBotCore;
import org.mangorage.mangobotplugin.pagedlist.PagedListManager;
import org.mangorage.mangobotplugin.pagedlist.PagedListWithAction;

public class MusicUtil {
    public static void connectToAudioChannel(VoiceChannel channel) {
        try {
            Guild guild = channel.getGuild();
            AudioManager audioManager = guild.getAudioManager();
            MusicPlayer musicPlayer = MusicPlayer.getInstance(guild.getId());

            audioManager.setSendingHandler(musicPlayer);
            audioManager.setSelfDeafened(true);
            audioManager.setSelfMuted(false);
            audioManager.setAutoReconnect(true);
            audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE);
            audioManager.setConnectTimeout(30_000);
            musicPlayer.setVolume(5); // Default volume so nobody gets there ears torn out by sound.

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
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());


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

    public static String getResultTrack(PagedList.Page<AudioTrack> page, int currentPage, int totalPage) {
        return """
                Song: (%s/%s)
                %s
                """.formatted(currentPage, totalPage, MarkdownUtil.maskedLink(page.getEntries()[0].getInfo().title, page.getEntries()[0].getInfo().uri));
    }

    public static void sendSongsQueue(MessageChannel channel, PagedListManager listManager, MusicPlayer manager, AudioTrack[] songs) {

        channel.sendMessage(
                """
                Getting List... 
                """
        ).queue(m -> {
            var list = new PagedListWithAction<AudioTrack>((pageList, button,id, total) -> {
                switch (id) {
                    case "next" -> pageList.next();
                    case "prev" -> pageList.previous();
                    case "add" -> {
                        manager.add(
                                pageList.current().getEntries()[0].makeClone()
                        );

                        button.deferReply(true).setContent("Added Song to queue!").queue();

                        return;
                    }
                }

                m.editMessage(getResultTrack(pageList.current(), pageList.getPage(), total)).queue();
            });

            list.get().rebuild(songs, 1);

            Button prev = Button.primary("prev", "Previous");
            Button next = Button.primary("next", "Next");
            Button add = Button.primary("add", "Add to Queue");

            m.editMessage(getResultTrack(list.get().current(), list.get().getPage(), list.get().totalPages())).setActionRow(prev, next, add).queue();

            listManager.putList(m.getId(), list);
        });
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
            var list = new PagedListWithAction<String>((pageList, button,id, total) -> {

                switch (id) {
                    case "next" -> pageList.next();
                    case "prev" -> pageList.previous();
                }

                m.editMessage(getResult(pageList.current(), pageList.getPage(), total)).queue();
            });

            list.get().rebuild(songs, 1);

            Button prev = Button.primary("prev", "Previous");
            Button next = Button.primary("next", "Next");

            m.editMessage(getResult(list.get().current(), list.get().getPage(), list.get().totalPages())).setActionRow(prev, next).queue();

            listManager.putList(m.getId(), list);
        });
    }
}
