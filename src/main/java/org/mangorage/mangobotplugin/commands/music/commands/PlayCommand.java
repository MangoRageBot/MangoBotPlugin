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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.mangorage.mangobotcore.api.jda.command.v1.CommandResult;
import org.mangorage.mangobotcore.api.jda.command.v1.ICommand;
import org.mangorage.mangobotcore.api.util.misc.Arguments;
import org.mangorage.mangobotplugin.commands.music.MusicPlayer;
import org.mangorage.mangobotplugin.commands.music.MusicUtil;

import java.util.List;

public final class PlayCommand implements ICommand {
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
        if (!message.isFromGuild()) return CommandResult.GUILD_ONLY;

        MessageChannelUnion channel = message.getChannel();
        Guild guild = message.getGuild();
        Member member = message.getMember();


        if (member == null) return CommandResult.FAIL;

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null) return CommandResult.FAIL;
        MusicPlayer player = MusicPlayer.getInstance(guild.getId());


        if (!voiceState.inAudioChannel()) {
            message.reply("Must be in a Voice Channel!").queue();
            return CommandResult.PASS;
        }

        if (player.isPlaying() && !player.isPaused()) return CommandResult.PASS;

        MusicUtil.connectToAudioChannel(voiceState.getChannel().asVoiceChannel());

        player.resume();

        AudioTrack track = player.getPlaying();
        channel.sendMessage(
                """
                Playing:
                %s/%s
                %s
                """.formatted(MusicUtil.formatDuration(track.getPosition()), MusicUtil.formatDuration(track.getDuration()),  MarkdownUtil.maskedLink(track.getInfo().title, track.getInfo().uri))).queue();

        return CommandResult.PASS;
    }
}
