package org.mangorage.mangobotplugin;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.event.v1.CommandEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordButtonInteractEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordMessageReactionAddEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordMessageReceivedEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordModalInteractionEvent;
import org.mangorage.mangobotcore.api.plugin.MangoBotCore;
import org.mangorage.mangobotcore.api.util.jda.slash.command.watcher.WatcherManager;
import org.mangorage.mangobotcore.api.util.misc.Arguments;
import org.mangorage.mangobotcore.api.util.misc.TaskScheduler;
import org.mangorage.mangobotplugin.entrypoint.MangoBot;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

        // Command Logic
        final var message = event.getMessage();
        final var rawMessage = message.getContentRaw();
        final var cmdPrefix = MangoBotCore.isDevMode() ? "dev!" : "!";
        final var silentPrefix = "s" + cmdPrefix;
        final var isSilent = rawMessage.startsWith(silentPrefix);


        if (isSilent || rawMessage.startsWith(cmdPrefix)) {
            final var cmdParseResult = new CommandParseResult(); // TODO: Do something with this when we convert to new cmd system!
            final var dispatcher = mangoBot.getCommandDispatcher();
            final var result = dispatcher.execute(
                    isSilent ? rawMessage.replaceFirst(silentPrefix, "") : rawMessage.replaceFirst(cmdPrefix, ""),
                    message,
                    cmdParseResult
            );

            if (result != JDACommandResult.INVALID_COMMAND) {
                if (result.getMessage() != null)
                    event.getMessage().reply(result.getMessage()).queue();

                if (isSilent)
                    TaskScheduler.getExecutor().schedule(
                            () -> {
                                message.delete().queue();
                                },
                            250,
                            TimeUnit.MILLISECONDS
                    );
            } else {
                String[] command_pre = rawMessage.split(" ");
                Arguments arguments = Arguments.of(Arrays.copyOfRange(command_pre, 1, command_pre.length));

                var cmd = rawMessage.replaceFirst(cmdPrefix, "").split(" ");

                final var cmdEvent = CommandEvent.BUS.fire(new CommandEvent(message, cmd[0], arguments));
                if (cmdEvent.isHandled()) {
                    final var msg = cmdEvent.getResult().getMessage();
                    if (msg != null)
                        message.reply(msg).queue();
                }
            }

            if (!cmdParseResult.getMessages().isEmpty()) {
                message.reply(
                        String.join("\n", cmdParseResult.getMessages())
                ).queue();
            }
        }
    }

    @SubscribeEvent
    public void onReactionAdd(MessageReactionAddEvent event) {
        DiscordMessageReactionAddEvent.BUS.post(new DiscordMessageReactionAddEvent(event));
    }
    
}
