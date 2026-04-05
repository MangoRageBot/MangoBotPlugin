package org.mangorage.mangobotplugin.commands.music;


import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;

import java.util.List;

public final class AudioLoader extends AbstractAudioLoadResultHandler {
    private final GuildMusicManager mngr;

    public AudioLoader(GuildMusicManager mngr) {
        this.mngr = mngr;
    }

    @Override
    public void ontrackLoaded(TrackLoaded result) {
        final Track track = result.getTrack();

        var userData = new MyUserData(0l);

        track.setUserData(userData);

        this.mngr.scheduler.enqueue(track);

        final var trackTitle = track.getInfo().getTitle();

//        event.getHook().sendMessage("Added to queue: " + trackTitle + "\nRequested by: <@" + userData.requester() + '>').queue();
    }

    @Override
    public void onPlaylistLoaded(PlaylistLoaded result) {
        final int trackCount = result.getTracks().size();
//        event.getHook()
//                .sendMessage("Added " + trackCount + " tracks to the queue from " + result.getInfo().getName() + "!")
//                .queue();

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

        //event.getHook().sendMessage("Adding to queue: " + firstTrack.getInfo().getTitle()).queue();

        this.mngr.scheduler.enqueue(firstTrack);
    }

    @Override
    public void noMatches() {
        //event.getHook().sendMessage("No matches found for your input!").queue();
    }

    @Override
    public void loadFailed(LoadFailed result) {
        //.getHook().sendMessage("Failed to load track! " + result.getException().getMessage()).queue();
    }
}
