package org.mangorage.mangobotplugin.pagedlist;

import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.mangorage.commonutils.misc.PagedList;

public final class PagedListWithAction<T> {
    private final PagedList<T> pagedList = new PagedList<>();
    private final PagedListAction<T> action;

    public PagedListWithAction(PagedListAction<T> action) {
        this.action = action;
    }

    public void consume(ButtonInteraction interaction, String id) {
        action.consume(get(), interaction, id, pagedList.totalPages());
    }

    public PagedList<T> get() {
        return pagedList;
    }
}
