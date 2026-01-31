package org.mangorage.mangobotplugin.commands.trick.impl;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordButtonInteractEvent;
import org.mangorage.mangobotcore.api.util.misc.PagedList;
import org.mangorage.mangobotcore.api.util.misc.RunnableTask;
import org.mangorage.mangobotcore.api.util.misc.TaskScheduler;
import org.mangorage.mangobotplugin.commands.trick.Trick;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class TrickListSubCommand extends AbstractJDACommand {
    private final Map<String, PagedList<String>> PAGES = new ConcurrentHashMap<>();
    private final TrickManager trickManager;

    public TrickListSubCommand(String name, TrickManager trickManager) {
        super(name, "List of tricks");
        this.trickManager = trickManager;
        DiscordButtonInteractEvent.BUS.addListener(this::onButton);
    }

    @Override
    public List<String> aliases() {
        return List.of("l");
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        final var message = commandContext.getContextObject();
        int length = 5;

        MessageChannelUnion channel = message.getChannel();
        if (!trickManager.getTricksForGuild(message.getGuildIdLong()).isEmpty()) {

            final long guildID = message.getGuildIdLong();

            PagedList<String> tricks = createTricks(guildID, length);

            channel.sendMessage("""
                       Getting Tricks List... 
                        """)
                    .queue(m -> {
                        PAGES.put(m.getId(), tricks);
                        TaskScheduler.getExecutor().schedule(new RunnableTask<>(m, (d) -> removeTricksList(d.get())), 10, TimeUnit.MINUTES);
                        updateTrickListMessage(tricks, m, true);
                    }
            );
        }

        return JDACommandResult.PASS;
    }

    private void updateTrickListMessage(PagedList<String> tricks, Message message, boolean addButtons, String buttonID) {
        switch (buttonID) {
            case "next" -> tricks.next();
            case "prev" -> tricks.previous();
        }

        String result = createTricksString(tricks);

        if (addButtons) {
            // Add buttons!
            Button prev = Button.primary("prev".formatted(message.getId()), "previous");
            Button next = Button.primary("next".formatted(message.getId()), "next");

            message.editMessage(result).setActionRow(prev, next).queue();
        } else {
            message.editMessage(result).queue();
        }
    }

    private void updateTrickListMessage(PagedList<String> tricks, Message message, boolean addButtons) {
        updateTrickListMessage(tricks, message, addButtons, "");
    }

    private String createTricksString(PagedList<String> tricks) {
        String result = "List of Tricks (%s / %s) \r".formatted(tricks.getPage(), tricks.totalPages());

        PagedList.Page<String> entries = tricks.current();

        int i = 0;
        for (String entry : entries.getEntries()) {
            i++;
            result = result + "%s: %s \r".formatted(i, entry);
        }

        return result;
    }

    private void removeTricksList(Message message) {
        if (PAGES.containsKey(message.getId())) {
            message.editMessage(createTricksString(PAGES.get(message.getId()))).setComponents().queue();
            PAGES.remove(message.getId());
        }
    }

    private PagedList<String> createTricks(long guildID, int entries) {
        PagedList<String> tricks = new PagedList<>();

        Object[] LIST = trickManager.getTricksForGuild(guildID).stream().map(Trick::getTrickID).toArray();
        tricks.rebuild(Arrays.copyOf(LIST, LIST.length, String[].class), entries);

        return tricks;
    }

    public void onButton(DiscordButtonInteractEvent event) {
        var interaction = event.getDiscordEvent();

        Message message = interaction.getMessage();
        String ID = message.getId();

        if (PAGES.containsKey(ID)) {
            updateTrickListMessage(PAGES.get(ID), message, false, interaction.getButton().getId());
            interaction.getInteraction().deferEdit().queue();
        }
    }
}
