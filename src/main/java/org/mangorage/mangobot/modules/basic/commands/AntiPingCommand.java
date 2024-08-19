package org.mangorage.mangobot.modules.basic.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.IBasicCommand;
import org.mangorage.mangobotapi.core.data.DataHandler;
import org.mangorage.mangobotapi.core.data.IEmptyFileNameResolver;
import org.mangorage.mangobotapi.core.events.DiscordEvent;
import org.mangorage.mangobotapi.core.events.LoadEvent;
import org.mangorage.mangobotapi.core.events.SaveEvent;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiPingCommand  implements IBasicCommand {

    record Key(long serverID, long userID) {}
    record SaveData(Map<Key, Boolean> data) implements IEmptyFileNameResolver {}

    private static final Map<Key, Boolean> FLAGS = new HashMap<>();

    private static final DataHandler<SaveData> HANDLER = DataHandler.create()
            .path("data/pingsData")
            .file()
            .build(SaveData.class);

    public AntiPingCommand(CorePlugin plugin) {
        plugin.getPluginBus().addGenericListener(0, MessageReceivedEvent.class, DiscordEvent.class, this::onMessage2);
        plugin.getPluginBus().addListener(0, LoadEvent.class, loadEvent -> {
            var data = HANDLER.loadFile(plugin.getPluginDirectory());
            data.ifPresent(save -> {
                var result = save.data();
                FLAGS.clear();
                FLAGS.putAll(result);
            });
        });
        plugin.getPluginBus().addListener(0, SaveEvent.class, save -> {
            HANDLER.save(plugin.getPluginDirectory(), new SaveData(Map.copyOf(FLAGS)));
        });
    }

    public void onMessage2(DiscordEvent<MessageReceivedEvent> event) {
        var dEvent = event.getInstance();
        var msg = dEvent.getMessage();
        if (msg.getAuthor().isBot()) return;
        var msgReference = msg.getReferencedMessage();

        if (msgReference != null)  {
            var authorPinged = msgReference.getAuthor();
            if (authorPinged.isBot()) return;
            var guildID = dEvent.isFromGuild() ? dEvent.getGuild().getIdLong() : -1;
            if (guildID == -1) return;
            var whoPinged = msg.getAuthor();

            if (!msg.getMentions().isMentioned(authorPinged)) return;

            if (FLAGS.containsKey(new Key(guildID, authorPinged.getIdLong()))) {
                whoPinged.openPrivateChannel().queue(pc -> {
                    pc.sendMessageEmbeds(PingCommand.EMBED).setContent("").setContent("Please do not ping this person -> %s".formatted(msgReference.getJumpUrl())).queue();
                });
            }
        }
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments args) {
        var userID = message.getAuthor().getIdLong();
        var serverID = message.isFromGuild() ? message.getGuild().getIdLong() : -1;
        if (serverID == -1) return CommandResult.PASS;
        var key = new Key(serverID, userID);
        FLAGS.compute(key, (k, value) -> {
            var returnValue = value == null || !value;
            message.reply("Set Anti Ping Reponse " + (!returnValue ? "Off" : "On")).queue();
            return returnValue;
        });
        return CommandResult.PASS;
    }


    @Override
    public String commandId() {
        return "antiping";
    }


    @Override
    public String usage() {
        return "!antiping";
    }

    @Override
    public List<String> commandAliases() {
        return List.of();
    }

    @Override
    public String description() {
        return """
                Makes it so bot asks people nicely to not ping you if you are pinged.
                
                Currently only works when reply-pinged. Will add message pings later.
                """;
    }
}
