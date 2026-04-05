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

public final class JoinCommand extends AbstractJDACommand {

    private final IMusicManager musicManager;

    public JoinCommand(IMusicManager musicManager) {
        super("join", "Join the Voicechat!");
        this.musicManager = musicManager;
    }

    @Override
    public JDACommandType getCommandType() {
        return JDACommandType.GUILD;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var context = commandContext.getContextObject();
        final var member = context.getMember();

        if(joinHelper(member, commandContext.getContextObject().getJDA())) {
            context.reply("Joined Voice Channel!").queue();
        } else {
            context.reply("Please move to a Voice Channel!").queue();
        }

        return JDACommandResult.PASS;
    }

    // Makes sure that the bot is in a voice channel!
    private boolean joinHelper(Member member, JDA jda) {
        musicManager.getOrCreate(member.getGuild().getIdLong());

        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState.inAudioChannel()) {
            jda.getDirectAudioController().connect(memberVoiceState.getChannel());
            return true;
        }

        return false;
    }
}
