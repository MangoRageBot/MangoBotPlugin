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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.mangorage.commonutils.log.LogHelper;
import org.mangorage.commonutils.misc.Arguments;
import org.mangorage.mangobotcore.jda.command.api.CommandResult;
import org.mangorage.mangobotcore.jda.command.api.ICommand;
import org.mangorage.mangobotplugin.commands.music.MusicPlayer;
import org.mangorage.mangobotplugin.commands.music.MusicUtil;

import java.util.List;

public class PlayCommand implements ICommand {
    @Override
    public String id() {
        return "play";
    }

    @Override
    public List<String> commands() {
        return List.of("play");
    }

    @Override
    public String usage() {
        return "Play Usage: N/A";
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments arg) {
        String[] args = arg.getArgs();

        if (!message.isFromGuild()) return CommandResult.GUILD_ONLY;

        String URL = args[0];
        MessageChannelUnion channel = message.getChannel();
        Guild guild = message.getGuild();
        Member member = message.getMember();


        if (member == null) return CommandResult.FAIL;

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null) return CommandResult.FAIL;
        MusicPlayer player = MusicPlayer.getInstance(guild.getId());

        if (voiceState.inAudioChannel()) {
            if (!URL.isEmpty()) {
                if (!player.isPlaying()) {
                    try {
                        player.load(String.join(" ", args), e -> {
                            switch (e.getReason()) {
                                case SUCCESS -> {
                                    MusicUtil.connectToAudioChannel(voiceState.getChannel().asVoiceChannel());
                                    player.add(e.getTrack());
                                    player.play();
                                    MessageEmbed embed = new EmbedBuilder()
                                            .setTitle(e.getTrack().getInfo().title, e.getTrack().getInfo().uri)
                                            .build();
                                    channel.sendMessage("Playing: ").addEmbeds(embed).queue();
                                }
                                case FAILED -> {
                                    channel.sendMessage("Failed").queue();
                                }
                                case NO_MATCHES -> {
                                    channel.sendMessage("No matches was found!").queue();
                                }
                            }
                        });
                    } catch (Exception e) {
                        LogHelper.error(e.getMessage());
                    }
                } else
                    channel.sendMessage("Already playing!").queue();
            } else {
                if (player.isPlaying()) {
                    player.resume();
                    AudioTrack track = player.getPlaying();
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle(track.getInfo().title, track.getInfo().uri)
                            .build();
                    channel.sendMessage("Resumed playing: ").addEmbeds(embed).queue();
                } else {
                    if (!player.isQueueEmpty()) {
                        MusicUtil.connectToAudioChannel(voiceState.getChannel().asVoiceChannel());
                        player.play();
                        AudioTrack track = player.getPlaying();
                        MessageEmbed embed = new EmbedBuilder()
                                .setTitle(track.getInfo().title, track.getInfo().uri)
                                .build();
                        channel.sendMessage("Started playing: ").addEmbeds(embed).queue();
                    } else
                        channel.sendMessage("Nothing is currently playing.").queue();
                }
            }
        } else
            channel.sendMessage("Must be in a voice channel!").queue();

        return CommandResult.PASS;
    }
}
