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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.mangorage.commonutils.misc.Arguments;
import org.mangorage.mangobotcore.jda.command.api.CommandResult;
import org.mangorage.mangobotcore.jda.command.api.ICommand;
import org.mangorage.mangobotplugin.commands.music.MusicPlayer;

import java.util.List;

public class QueueCommand implements ICommand {
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
        return "Queue Usage: N/A";
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments arg) {
        String[] args = arg.getArgs();
        String URL = args[0];
        MessageChannelUnion channel = message.getChannel();
        Guild guild = message.getGuild();
        MusicPlayer player = MusicPlayer.getInstance(guild.getId());

        if (URL != null) {
            player.load(URL, e -> {
                /**
                 MessageEmbed embed = new EmbedBuilder()
                 .setTitle(track.getInfo().title, track.getInfo().uri)
                 .build();
                 channel.sendMessage("Playing: ").addEmbeds(embed).queue();


                 channel.sendMessage("Queued: " + track.getInfo().title).queue();
                 **/
                switch (e.getReason()) {
                    case SUCCESS -> {
                        player.add(e.getTrack());
                        MessageEmbed embed = new EmbedBuilder()
                                .setTitle(e.getTrack().getInfo().title, e.getTrack().getInfo().uri)
                                .build();
                        channel.sendMessage("Added to Queue: ").addEmbeds(embed).queue();

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
