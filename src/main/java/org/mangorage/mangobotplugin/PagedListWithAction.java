package org.mangorage.mangobotplugin;

import org.apache.logging.log4j.util.TriConsumer;
import org.mangorage.commonutils.misc.PagedList;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PagedListWithAction {
    private final PagedList<String> pagedList = new PagedList<>();
    private final TriConsumer<PagedList.Page<String>, Integer, Integer> consumer;

    public PagedListWithAction(TriConsumer<PagedList.Page<String>, Integer, Integer> consumer) {
        this.consumer = consumer;
    }

    public void consume() {
        consumer.accept(pagedList.current(), pagedList.getPage(), pagedList.totalPages());
    }

    public PagedList<String> get() {
        return pagedList;
    }
}
