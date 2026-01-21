package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.event.v1.CommandEvent;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.entrypoint.MangoBot;

public final class TrickCommand extends AbstractJDACommand {
    private final TrickManager trickManager;

    public TrickCommand(String name, MangoBot plugin) {
        super(name);
        this.trickManager = plugin.getTrickManager();


        addSubCommand(new TrickAddSubCommand("add", plugin.getTrickManager()));
        addSubCommand(new TrickRemoveSubCommand("remove", plugin.getTrickManager()));

        addSubCommand(new TrickShowSubCommand("show", plugin.getTrickManager()));
        addSubCommand(new TrickListSubCommand("list", plugin.getTrickManager()));

        CommandEvent.BUS.addListener(this::onCommandEvent);
    }

    public void onCommandEvent(CommandEvent event) {
        if (!event.isHandled()) {
            Message message = event.getMessage();
            if (!message.isFromGuild()) return;
            long guildID = message.getGuild().getIdLong();
            String command = event.getCommand().toLowerCase();
            String args = event.getArguments().getFrom(0);

            // TODO: Finish!
            final var trick = trickManager.getTrickForGuildByName(guildID, command);
            if (trick != null) {
                execute(
                        message,
                        new String[]{
                                "show",
                                trick.getTrickID()
                        },
                        new CommandParseResult()
                );
                event.setHandled(JDACommandResult.PASS);
            }
        }
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        return JDACommandResult.PASS;
    }
}
