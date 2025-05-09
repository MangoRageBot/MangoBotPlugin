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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.mangorage.commonutils.misc.Arguments;
import org.mangorage.mangobotcore.jda.command.api.CommandResult;
import org.mangorage.mangobotcore.jda.command.api.ICommand;
import org.mangorage.mangobotplugin.commands.music.MusicPlayer;
import org.mangorage.mangobotplugin.commands.music.MusicUtil;

import java.util.List;


public final class PauseCommand implements ICommand {
    @Override
    public String id() {
        return "pause";
    }

    @Override
    public List<String> commands() {
        return List.of("pause");
    }

    @Override
    public String usage() {
        return "Pause Usage: N/A";
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments args) {
        MessageChannelUnion channel = message.getChannel();
        Guild guild = message.getGuild();

        if (MusicPlayer.getInstance(guild.getId()).isPlaying()) {
            MusicPlayer.getInstance(guild.getId()).pause();
            AudioTrack track = MusicPlayer.getInstance(guild.getId()).getPlaying();

            channel.sendMessage(
                    """
                    Paused:
                    %s/%s
                    %s
                    """.formatted(MusicUtil.formatDuration(track.getPosition()), MusicUtil.formatDuration(track.getDuration()),  MarkdownUtil.maskedLink(track.getInfo().title, track.getInfo().uri))).queue();


        } else
            channel.sendMessage("Nothing is playing").queue();
        return CommandResult.PASS;
    }

}
