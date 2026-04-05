package org.mangorage.mangobotplugin.commands.music.impl;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.argument.OptionalArg;
import org.mangorage.mangobotcore.api.command.v1.argument.RequiredArg;
import org.mangorage.mangobotcore.api.command.v1.argument.types.StringArgumentType;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandType;
import org.mangorage.mangobotplugin.commands.music.AudioLoader;
import org.mangorage.mangobotplugin.commands.music.IMusicManager;

import java.util.concurrent.TimeUnit;

public final class PlayCommand extends AbstractJDACommand {

    private final OptionalArg<String> identifierArg = new OptionalArg<>("identifier", "Identifier", StringArgumentType.single(), null);
    private final IMusicManager manager;

    public PlayCommand(IMusicManager manager) {
        super("play", "Play a song!");
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
        final var identifier = commandContext.getArgumentOrElse(identifierArg);

        final var member = context.getMember();

        if (!member.getVoiceState().inAudioChannel()) {
            context.reply("Must be in a voice chat!").queue();
            return JDACommandResult.PASS;
        }

        final var guildManager = manager.getOrCreate(guild.getIdLong());
        final var player = guildManager.getPlayer();

        if (identifier == null) {
            if (player.isPresent())
                player.get()
                        .setPaused(false)
                        .subscribe(plr -> {
                            context.reply("Unpaused music!").queue();
                        });
        } else {
            joinHelper(member, member.getJDA());

            if (guildManager.getPlayer().isPresent()) {
                guildManager
                        .getLink()
                        .ifPresent(link -> {
                            context.reply("Playing Track Soon!").queue();
                            link.loadItem(identifier).subscribe(new AudioLoader(guildManager, context));
                        });
            } else {

                context.reply("Playing Track Soon!")
                        .queueAfter(2, TimeUnit.SECONDS, m -> {
                            guildManager
                                    .getLink()
                                    .ifPresent(link -> {
                                        link.loadItem(identifier).subscribe(new AudioLoader(guildManager, context));
                                    });
                        });
            }
        }

        return JDACommandResult.PASS;
    }

    // Makes sure that the bot is in a voice channel!
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
