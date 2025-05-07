package org.mangorage.mangobotplugin;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.mangorage.commonutils.misc.PagedList;
import org.mangorage.mangobotcore.jda.event.DiscordButtonInteractEvent;

import java.util.HashMap;
import java.util.Map;

public final class PagedListManager {
    // Key must be a messageId
    private final Map<String, PagedListWithAction> pages = new HashMap<>();

    public PagedListManager() {
        DiscordButtonInteractEvent.BUS.addListener(this::onButton);
    }

    public void putList(String id, PagedListWithAction pagedListWithAction) {
        pages.put(id, pagedListWithAction);
    }

    public void removeList(String id) {
        pages.remove(id);
    }

    public PagedList.Page<String> next(String id) {
        return pages.get(id).get().next();
    }

    public PagedList.Page<String> previous(String id) {
        return pages.get(id).get().previous();
    }

    private void update(PagedListWithAction action, String buttonID) {
        var list = action.get();

        switch (buttonID) {
            case "next" -> list.next();
            case "prev" -> list.previous();
        }

        action.consume();
    }


    public void onButton(DiscordButtonInteractEvent event) {
        var interaction = event.getDiscordEvent();

        Message message = interaction.getMessage();
        String ID = message.getId();

        if (pages.containsKey(ID)) {
            update(pages.get(ID), interaction.getButton().getId());
            interaction.getInteraction().deferEdit().queue();
        }
    }
}
