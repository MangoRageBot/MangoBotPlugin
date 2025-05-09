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

package org.mangorage.mangobotplugin.commands.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.mangorage.commonutils.misc.Arguments;
import org.mangorage.mangobotcore.jda.command.api.CommandResult;
import org.mangorage.mangobotcore.jda.command.api.ICommand;
import org.mangorage.mangobotplugin.pagedlist.PagedListManager;
import org.mangorage.mangobotplugin.commands.music.MusicPlayer;
import org.mangorage.mangobotplugin.commands.music.MusicUtil;

import java.util.List;

public final class QueueCommand implements ICommand {
    private final PagedListManager pagedListManager;

    public QueueCommand(PagedListManager pagedListManager) {
        this.pagedListManager = pagedListManager;
    }

    @Override
    public String id() {
        return "queue";
    }

    @Override
    public List<String> commands() {
        return List.of("queue");
    }

    @Override
    public String usage() {
        return """
                Queue Usage:
                
                !queue
                !queue url
                """;
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments args) {
        MessageChannelUnion channel = message.getChannel();
        Guild guild = message.getGuild();
        MusicPlayer player = MusicPlayer.getInstance(guild.getId());

        if (args.getArgs().length == 0) {
            MusicUtil.sendSongs(channel, pagedListManager, player);
            return CommandResult.PASS;
        }

        String query = args.getFrom(0);

        if (query != null) {

            if (!query.contains("search") && !query.startsWith("https://")) {
                query = YoutubeAudioSourceManager.SEARCH_PREFIX + " " + query;
            }

            final String queryFinal = query;

            player.load(query, e -> {
                switch (e.getReason()) {
                    case SUCCESS -> {
                        if (queryFinal.startsWith("https://")) {

                            e.getTracks().forEach(player::add);

                            channel.sendMessage(
                                    """
                                            send '!queue' to see list of songs in queue!
                                            send '!play' to have bot join and start playing next song!
                                            
                                            
                                            Added to Queue:
                                            %s
                                            """.formatted(MarkdownUtil.maskedLink(e.getTrack().getInfo().title, e.getTrack().getInfo().uri))).queue();
                        } else {
                            // Searching for something instead
                            MusicUtil.sendSongsQueue(channel, pagedListManager, player, e.getTracks().toArray(AudioTrack[]::new));
                        }
                    }
                    case FAILED -> {
                        channel.sendMessage("Failed").queue();
                    }
                    case NO_MATCHES -> {
                        channel.sendMessage("No matches was found!").queue();
                    }
                }

            });
        }
        return CommandResult.PASS;
    }
}
