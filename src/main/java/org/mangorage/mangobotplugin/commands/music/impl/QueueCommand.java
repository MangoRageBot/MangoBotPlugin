package org.mangorage.mangobotplugin.commands.music.impl;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.argument.OptionalArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandType;
import org.mangorage.mangobotplugin.commands.music.AudioLoader;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;

import java.util.concurrent.TimeUnit;

public final class QueueCommand extends AbstractJDACommand {

    private final OptionalArg<String> identifierArg =
            new OptionalArg<>("identifier", "Identifier", StringArgumentType.single(), null);

    private final IMusicManager manager;

    public QueueCommand(IMusicManager manager) {
        super("queue", "Queue a song!");
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
        final var identifier = commandContext.getArgumentOrElse(identifierArg);

        if (identifier == null) {
            context.reply("You need to provide something to queue.").queue();
            return JDACommandResult.PASS;
        }

        if (!member.getVoiceState().inAudioChannel()) {
            context.reply("Must be in a voice chat!").queue();
            return JDACommandResult.PASS;
        }

        final var guildManager = manager.getOrCreate(guild.getIdLong());

        // Ensure bot joins
        joinHelper(member, member.getJDA());

        context.reply("Processing...").queueAfter(2, TimeUnit.SECONDS, m -> {
            guildManager.getLink().ifPresent(link -> {
                link.loadItem(identifier).subscribe(new AudioLoader(guildManager, context));
            });
        });



        return JDACommandResult.PASS;
    }

    // same helper as your play command
    private boolean joinHelper(Member member, JDA jda) {
        manager.getOrCreate(member.getGuild().getIdLong());

        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState.inAudioChannel()) {
            jda.getDirectAudioController().connect(memberVoiceState.getChannel());
            return true;
        }

        return false;
    }
}