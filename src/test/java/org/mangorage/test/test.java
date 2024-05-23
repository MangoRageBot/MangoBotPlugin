package org.mangorage.test;

import org.mangorage.eventbus.EventBus;
import org.mangorage.mangobotapi.core.events.SaveEvent;

public class test {
    public static void main(String[] args) {
        var bus = EventBus.create();

        bus.addListener(10, SaveEvent.class, test::onSave);
        bus.post(new SaveEvent());
    }

    public static void onSave(SaveEvent e) {
        System.out.println("LOL");
    }
}
