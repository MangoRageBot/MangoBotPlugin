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

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.mangorage.mangobotcore.api.util.log.LogHelper;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public final class MusicPlayer extends AudioEventAdapter implements AudioSendHandler {
    private static final HashMap<String, MusicPlayer> MUSIC_PLAYERS = new HashMap<>();

    public static MusicPlayer getInstance(String guildID) {
        if (!MUSIC_PLAYERS.containsKey(guildID)) {
            MusicPlayer player = new MusicPlayer(guildID);
            MUSIC_PLAYERS.put(guildID, player);
            return player;
        }
        return MUSIC_PLAYERS.get(guildID);
    }

    private final DefaultAudioPlayerManager manager;
    private final DefaultAudioPlayer audioPlayer;
    private final Deque<AudioTrack> tracks = new ArrayDeque<>();
    private final String guildID;
    private AudioStatus status = AudioStatus.STOPPED;
    private AudioFrame lastFrame;


    private MusicPlayer(String guildID) {
        this.guildID = guildID;
        this.manager = new DefaultAudioPlayerManager();
        this.audioPlayer = new DefaultAudioPlayer(manager);


        manager.enableGcMonitoring();
        manager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        manager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);


        MusicUtil.registerRemoteSources(manager);

        audioPlayer.addListener(this);
    }

    public AudioTrack getPlaying() {
        return audioPlayer.getPlayingTrack();
    }

    public void setVolume(int volume) {
        audioPlayer.setVolume(volume);
    }

    public boolean isPlaying() {
        if (audioPlayer.getPlayingTrack() == null)
            return false;
        return audioPlayer.getPlayingTrack() != null;
    }

    public boolean isPaused() {
        return audioPlayer.isPaused();
    }

    public boolean isQueueEmpty() {
        return tracks.isEmpty();
    }

    public void load(String URL, Consumer<AudioTrackEvent> eventConsumer) {
        try {
            manager.loadItem(URL, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    eventConsumer.accept(new AudioTrackEvent(track, AudioTrackEvent.Info.SUCCESS));
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    eventConsumer.accept(new AudioTrackEvent(playlist.getTracks(), AudioTrackEvent.Info.SUCCESS));
                }

                @Override
                public void noMatches() {
                    eventConsumer.accept(new AudioTrackEvent(AudioTrackEvent.Info.NO_MATCHES));
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    eventConsumer.accept(new AudioTrackEvent(AudioTrackEvent.Info.FAILED));
                    LogHelper.info(exception.getMessage());
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public AudioStatus getStatus() {
        return this.status;
    }

    public List<AudioTrack> getTracks() {
        return tracks.stream().toList();
    }

    public void play() {
        AudioTrack track = tracks.poll();
        if (track != null)
            audioPlayer.playTrack(track);
    }

    public void playNext() {
        play();
    }

    public void add(AudioTrack track) {
        tracks.add(track);
    }

    public void pause() {
        audioPlayer.setPaused(true);
    }

    public void resume() {
        audioPlayer.setPaused(false);
        if (audioPlayer.getPlayingTrack() == null)
            playNext();
    }

    public void stop() {
        audioPlayer.stopTrack();
    }


    public void onPlayerPause(AudioPlayer player) {
        this.status = AudioStatus.PAUSED;
    }

    public void onPlayerResume(AudioPlayer player) {
        this.status = AudioStatus.PLAYING;
    }

    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.status = AudioStatus.PLAYING;
    }

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.status = AudioStatus.STOPPED;
        if (endReason.mayStartNext)
            playNext();
    }

    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        LogHelper.info(exception.getMessage());
    }


    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
