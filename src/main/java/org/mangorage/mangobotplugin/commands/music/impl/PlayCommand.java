package org.mangorage.mangobotplugin.commands.music.impl;

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
        final var guild = commandContext.getContextObject().getGuild();
        final var identifier = commandContext.getArgumentOrElse(identifierArg);

        final var guildManager = manager.getOrCreate(guild.getIdLong());
        final var player = guildManager.getPlayer();

        if (identifier == null) {
            if (player.isPresent())
                player.get()
                        .setPaused(false)
                        .subscribe();
        } else {
            guildManager
                    .getLink()
                    .ifPresent(link -> {
                        link.loadItem(identifier).subscribe(new AudioLoader(guildManager));
                    });
        }

        return JDACommandResult.PASS;
    }
}
