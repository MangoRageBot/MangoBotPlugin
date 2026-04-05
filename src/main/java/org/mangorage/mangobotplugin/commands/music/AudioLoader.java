package org.mangorage.mangobotplugin.commands.music;


import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public final class AudioLoader extends AbstractAudioLoadResultHandler {
    private final GuildMusicManager mngr;
    private final Message message;

    public AudioLoader(GuildMusicManager mngr, Message message) {
        this.mngr = mngr;
        this.message = message;
    }

    @Override
    public void ontrackLoaded(TrackLoaded result) {
        final Track track = result.getTrack();

        var userData = new MyUserData(message.getAuthor().getIdLong());

        track.setUserData(userData);

        this.mngr.scheduler.enqueue(track);

        final var trackTitle = track.getInfo().getTitle();

        message.reply("Added to queue: " + trackTitle + "\nRequested by: <@" + userData.requester() + '>').queue();
    }

    @Override
    public void onPlaylistLoaded(PlaylistLoaded result) {
        final int trackCount = result.getTracks().size();
        message.reply("Added " + trackCount + " tracks to the queue from " + result.getInfo().getName() + "!")
                .queue();

        this.mngr.scheduler.enqueuePlaylist(result.getTracks());
    }

    @Override
    public void onSearchResultLoaded(SearchResult result) {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty()) {
            //event.getHook().sendMessage("No tracks found!").queue();
            return;
        }

        final Track firstTrack = tracks.get(0);

        message.reply("Adding to queue: " + firstTrack.getInfo().getTitle()).queue();

        this.mngr.scheduler.enqueue(firstTrack);
    }

    @Override
    public void noMatches() {
        message.reply("No matches found for your input!").queue();
    }

    @Override
    public void loadFailed(LoadFailed result) {
        message.reply("Failed to load track! " + result.getException().getMessage()).queue();
    }
}
