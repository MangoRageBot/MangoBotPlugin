package org.mangorage.mangobotplugin.pagedlist;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.jda.event.DiscordButtonInteractEvent;

import java.util.HashMap;
import java.util.Map;

public final class PagedListManager {
    // Key must be a messageId
    private final Map<String, PagedListWithAction<?>> pages = new HashMap<>();

    public PagedListManager() {
        DiscordButtonInteractEvent.BUS.addListener(this::onButton);
    }

    public void putList(String id, PagedListWithAction<?> pagedListWithAction) {
        pages.put(id, pagedListWithAction);
    }

    public void removeList(String id) {
        pages.remove(id);
    }

    public void onButton(DiscordButtonInteractEvent event) {
        var interaction = event.getDiscordEvent();

        Message message = interaction.getMessage();
        String ID = message.getId();


        if (pages.containsKey(ID)) {
            pages.get(ID).consume(interaction.getInteraction(), interaction.getButton().getId());
            if (interaction.isAcknowledged()) return;
            interaction.deferEdit().queue();
        }
    }
}
