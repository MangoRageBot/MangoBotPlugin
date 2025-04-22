package org.mangorage.mangobotplugin;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.mangorage.mangobotcore.jda.event.DiscordButtonInteractEvent;
import org.mangorage.mangobotcore.jda.event.DiscordMessageReceivedEvent;
import org.mangorage.mangobotcore.jda.event.DiscordModalInteractionEvent;
import org.mangorage.mangobotplugin.entrypoint.MangoBot;

public final class BotEventListener {
    private final MangoBot mangoBot;

    public BotEventListener(MangoBot mangoBot) {
        this.mangoBot = mangoBot;
    }


    @SubscribeEvent
    public void onModalInteract(ModalInteractionEvent event) {
        DiscordModalInteractionEvent.BUS.post(new DiscordModalInteractionEvent(event));
    }

    @SubscribeEvent
    public void onModalInteract(ButtonInteractionEvent event) {
        MangoBot.ACTION_REGISTRY.post(event);
        DiscordButtonInteractEvent.BUS.post(new DiscordButtonInteractEvent(event));
    }

    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        DiscordMessageReceivedEvent.BUS.post(new DiscordMessageReceivedEvent(event));
        mangoBot.getCommandManager().handle(event.getMessage());
    }
}
