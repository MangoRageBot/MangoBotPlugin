package org.mangorage.mangobotplugin;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

    static String formatMessage(SelfUser bot, String message) {
        return message.replaceFirst("<@" + bot.getId() + ">", "").trim();
    }


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
        final var message = event.getMessage();
        if (message.getContentRaw().isEmpty()) return; // No reason to send event, its blank...

        final long cmdStart = System.currentTimeMillis();

        DiscordMessageReceivedEvent.BUS.post(new DiscordMessageReceivedEvent(event));

        if (event.getAuthor().isBot()) return;

        final var rawMessage = formatMessage(mangoBot.getJDA().getSelfUser(), message.getContentRaw());
        final var processed = rawMessage.split("!");

        final var isSilent = processed.length > 1 && processed[0].contains("s");
        final var isDev = processed.length > 1 && processed[0].contains("dev");

        if (!isDev && MangoBotCore.isDevMode()) return;
        final var commandText = processed.length > 1 ? processed[1] : processed[0];

        event.getChannel().sendTyping().queue();

        final var dispatcher = mangoBot.getCommandDispatcher();
        final var cmdParseResult = new CommandParseResult();
        final var result = dispatcher.execute(commandText, message, cmdParseResult);

        handleCommandResult(message, commandText, result, isSilent, cmdParseResult, cmdStart);
    }

    private void handleCommandResult(Message message, String commandText, JDACommandResult result, boolean isSilent, CommandParseResult cmdParseResult, long cmdStart) {
        if (result != JDACommandResult.INVALID_COMMAND) {
            handleValidCommand(message, result, isSilent);
        } else {
            handleUnknownCommand(message, commandText, isSilent);
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

    private void handleUnknownCommand(Message message, String commandText, boolean isSilent) {
        String[] commandParts = commandText.split(" ");
        Arguments arguments = Arguments.of(Arrays.copyOfRange(commandParts, 1, commandParts.length));

        final var cmdEvent = CommandEvent.BUS.fire(new CommandEvent(message, commandText, arguments));

        if (cmdEvent.isHandled()) {
            var msg = cmdEvent.getResult().getMessage();
            if (msg != null) {
                message.reply(msg).queue();
            }
            handleValidCommand(message, cmdEvent.getResult(), isSilent);
        } else {
            if (!isSilent) {
                message.reply("Unknown command: `%s`".formatted(commandText)).queue();
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
