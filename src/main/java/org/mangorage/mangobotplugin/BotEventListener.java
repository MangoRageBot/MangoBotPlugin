package org.mangorage.mangobotplugin;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.command.v1.ICommandDispatcher;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.event.v1.CommandEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordButtonInteractEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordMessageReactionAddEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordMessageReceivedEvent;
import org.mangorage.mangobotcore.api.jda.event.v1.DiscordModalInteractionEvent;
import org.mangorage.mangobotcore.api.plugin.MangoBotCore;
import org.mangorage.mangobotcore.api.util.jda.slash.command.watcher.WatcherManager;
import org.mangorage.mangobotcore.api.util.misc.Arguments;
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

        final var message = event.getMessage();
        final var rawMessage = message.getContentRaw();
        final var cmdPrefix = MangoBotCore.isDevMode() ? "dev!" : "!";
        final var silentPrefix = "s" + cmdPrefix;
        final var isSilent = rawMessage.startsWith(silentPrefix);
        final var dispatcher = mangoBot.getCommandDispatcher();

        if (isCommand(rawMessage, cmdPrefix, silentPrefix)) {
            executeCommand(event, message, rawMessage, cmdPrefix, silentPrefix, isSilent, dispatcher);
        }
    }

    private boolean isCommand(String rawMessage, String cmdPrefix, String silentPrefix) {
        return rawMessage.startsWith(cmdPrefix) || rawMessage.startsWith(silentPrefix);
    }

    private void executeCommand(MessageReceivedEvent event, Message message, String rawMessage, String cmdPrefix, String silentPrefix, boolean isSilent, ICommandDispatcher<Message, JDACommandResult> dispatcher) {
        final long cmdStart = System.currentTimeMillis();
        final var cmdParseResult = new CommandParseResult();

        event.getChannel().sendTyping().queue();  // Indicate command processing

        // Clean the command prefix and execute
        final String commandText = rawMessage.replaceFirst(isSilent ? silentPrefix : cmdPrefix, "");
        final var result = dispatcher.execute(commandText, message, cmdParseResult);

        handleCommandResult(message, result, isSilent, cmdParseResult, cmdStart);
    }

    private void handleCommandResult(Message message, JDACommandResult result, boolean isSilent, CommandParseResult cmdParseResult, long cmdStart) {
        if (result != JDACommandResult.INVALID_COMMAND) {
            handleValidCommand(message, result, isSilent);
        } else {
            handleUnknownCommand(message);
        }

        sendCmdExecutionStats(message, cmdStart);
        sendParseErrorsIfAny(message, cmdParseResult);
    }

    private void handleValidCommand(Message message, JDACommandResult result, boolean isSilent) {
        if (result.getMessage() != null) {
            message.reply(result.getMessage()).queue();
        }

        if (isSilent) {
            message.delete().queueAfter(250, TimeUnit.MILLISECONDS);  // Delete silent command after 250ms
        }
    }

    private void handleUnknownCommand(Message message) {
        String[] commandParts = message.getContentRaw().split(" ");
        Arguments arguments = Arguments.of(Arrays.copyOfRange(commandParts, 1, commandParts.length));

        var cmd = message.getContentRaw().replaceFirst("!", "").split(" ");
        final var cmdEvent = CommandEvent.BUS.fire(new CommandEvent(message, cmd[0], arguments));

        if (cmdEvent.isHandled()) {
            var msg = cmdEvent.getResult().getMessage();
            if (msg != null) {
                message.reply(msg).queue();
            }
        }
    }

    private void sendCmdExecutionStats(Message message, long cmdStart) {
        if (MangoBotCore.isDevMode()) {
            long cmdEnd = System.currentTimeMillis() - cmdStart;
            message.reply("Took: %sms to process command request!".formatted(cmdEnd))
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }

    private void sendParseErrorsIfAny(Message message, CommandParseResult cmdParseResult) {
        if (!cmdParseResult.getMessages().isEmpty()) {
            message.reply(String.join("\n", cmdParseResult.getMessages()))
                    .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }

    @SubscribeEvent
    public void onReactionAdd(MessageReactionAddEvent event) {
        DiscordMessageReactionAddEvent.BUS.post(new DiscordMessageReactionAddEvent(event));
    }
    
}
