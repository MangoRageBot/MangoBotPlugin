package org.mangorage.mangobotplugin.commands.music.impl;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandType;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;

public final class PlayCommand extends AbstractJDACommand {

    private final IMusicManager manager;

    public PlayCommand(IMusicManager manager) {
        super("play", "Resume the current track!");
        this.manager = manager;
    }

    @Override
    public JDACommandType getCommandType() {
        return JDACommandType.GUILD;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var context = commandContext.getContextObject();
        final var guild = context.getGuild();
        final var member = context.getMember();

        if (!member.getVoiceState().inAudioChannel()) {
            context.reply("You need to be in a voice channel.").queue();
            return JDACommandResult.PASS;
        }

        final var guildManager = manager.getOrCreate(guild.getIdLong());

        // Make sure bot is in the same VC
        joinHelper(member, member.getJDA());

        guildManager.getPlayer().ifPresentOrElse(player ->
                        player.setPaused(false).subscribe(plr ->
                                context.reply("Resumed music!").queue()
                        ),
                () -> context.reply("Nothing is playing.").queue()
        );

        return JDACommandResult.PASS;
    }

    private boolean joinHelper(Member member, JDA jda) {
        final GuildVoiceState voiceState = member.getVoiceState();

        if (voiceState.inAudioChannel()) {
            jda.getDirectAudioController().connect(voiceState.getChannel());
            return true;
        }

        return false;
    }
}