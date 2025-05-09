package org.mangorage.mangobotplugin.pagedlist;

import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.mangorage.commonutils.misc.PagedList;

public interface PagedListAction<T> {
    void consume(PagedList<T> pagedList, ButtonInteraction interaction, String buttonId, Integer totalPages);
}
