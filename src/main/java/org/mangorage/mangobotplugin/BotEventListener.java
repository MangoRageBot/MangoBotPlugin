package org.mangorage.mangobotplugin;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.mangorage.commonutils.jda.slash.command.watcher.WatcherManager;
import org.mangorage.mangobotcore.jda.event.DiscordButtonInteractEvent;
import org.mangorage.mangobotcore.jda.event.DiscordMessageReactionAddEvent;
import org.mangorage.mangobotcore.jda.event.DiscordMessageReceivedEvent;
import org.mangorage.mangobotcore.jda.event.DiscordModalInteractionEvent;
import org.mangorage.mangobotplugin.entrypoint.MangoBot;

public final class BotEventListener {
    private final MangoBot mangoBot;

    public BotEventListener(MangoBot mangoBot) {
        this.mangoBot = mangoBot;
    }

    @SubscribeEvent
    public void onInteraction(SlashCommandInteractionEvent event) {
        WatcherManager.onCommandEvent(event);
    }

    @SubscribeEvent
    public void onSlashAuto(CommandAutoCompleteInteractionEvent event) {
        WatcherManager.onCommandAutoCompleteEvent(event);
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

    @SubscribeEvent
    public void onReactionAdd(MessageReactionAddEvent event) {
        DiscordMessageReactionAddEvent.BUS.post(new DiscordMessageReactionAddEvent(event));
    }
    
}
