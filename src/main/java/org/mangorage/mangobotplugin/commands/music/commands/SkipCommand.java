package org.mangorage.mangobotplugin.commands.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.mangorage.commonutils.misc.Arguments;
import org.mangorage.mangobotcore.jda.command.api.CommandResult;
import org.mangorage.mangobotcore.jda.command.api.ICommand;
import org.mangorage.mangobotplugin.commands.music.MusicPlayer;
import org.mangorage.mangobotplugin.commands.music.MusicUtil;

import java.util.List;

public final class SkipCommand implements ICommand {
    @Override
    public String id() {
        return "skip";
    }

    @Override
    public List<String> commands() {
        return List.of("skip");
    }

    @Override
    public String usage() {
        return "Skip Usage: !skip";
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments args) {
        MessageChannelUnion channel = message.getChannel();
        Guild guild = message.getGuild();
        MusicPlayer musicPlayer = MusicPlayer.getInstance(guild.getId());


        if (musicPlayer.isPlaying()) {

            musicPlayer.stop();
            musicPlayer.playNext();

            AudioTrack track = musicPlayer.getPlaying();

            channel.sendMessage(
                    """
                    Playing:
                    %s/%s
                    %s
                    """.formatted(MusicUtil.formatDuration(track.getPosition()), MusicUtil.formatDuration(track.getDuration()),  MarkdownUtil.maskedLink(track.getInfo().title, track.getInfo().uri))).queue();

        }

        return CommandResult.PASS;
    }


}
