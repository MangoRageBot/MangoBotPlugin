package org.mangorage.mangobot.modules.tricks;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.mangorage.basicutils.LogHelper;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.IBasicCommand;
import org.mangorage.mangobotapi.core.data.DataHandler;
import org.mangorage.mangobotapi.core.events.BasicCommandEvent;
import org.mangorage.mangobotapi.core.events.LoadEvent;
import org.mangorage.mangobotapi.core.events.SaveEvent;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;
import org.mangorage.mangobotapi.core.util.MessageSettings;
import java.util.HashMap;
import java.util.Map;

public class TrickCommand implements IBasicCommand {
    private final CorePlugin plugin;
    private final DataHandler<Trick> TRICK_DATA_HANDLER;
    private final Map<String, Map<String, Trick>> TRICKS = new HashMap<>();


    public TrickCommand(CorePlugin plugin) {
        this.plugin = plugin;
        this.TRICK_DATA_HANDLER = DataHandler.create(
                (data) -> {
                    TRICKS.computeIfAbsent(data.getGuildID(), (k) -> new HashMap<>()).put(data.getTrickID(), data);
                },
                Trick.class,
                "plugins/%s/data/tricksV2".formatted(plugin.getId()),
                DataHandler.Properties.create()
                        .setFileNamePredicate(e -> true)
        );

        plugin.getPluginBus().addListener(LoadEvent.class, this::onLoadEvent);
        plugin.getPluginBus().addListener(SaveEvent.class, this::onSaveEvent);
        plugin.getPluginBus().addListener(BasicCommandEvent.class, this::onCommandEvent);
    }


    public void onLoadEvent(LoadEvent event) {
        LogHelper.info("Loading Tricks Data!");
        TRICK_DATA_HANDLER.loadAll();
        LogHelper.info("Finished loading Tricks Data!");
    }

    public void onSaveEvent(SaveEvent event) {
        LogHelper.info("Saving Tricks Data!");

        TRICKS.forEach((k, v) -> {
            v.forEach((k2, v2) -> save(v2));
        });
    }

    public void onCommandEvent(BasicCommandEvent event) {
        if (!event.isHandled()) {
            Message message = event.getMessage();
            if (!message.isFromGuild()) return;
            String guildID = message.getGuild().getId();
            String command = event.getCommand().toLowerCase();
            String args = event.getArguments().getFrom(0);

            // We have found something that works, make sure we do this so that "Invalid Command" doesn't occur
            // event.setHandled() insures that the command has been handled!
            if (TRICKS.containsKey(guildID) && TRICKS.get(guildID).containsKey(command))
                event.setHandled(execute(message, Arguments.of("-s", command, args)));
        }
    }


    private void delete(Trick trick) {
        TRICK_DATA_HANDLER.deleteFile("%s.json".formatted(trick.getTrickID()), trick.getGuildID());
    }

    private void save(Trick trick) {
        TRICK_DATA_HANDLER.save("%s.json".formatted(trick.getTrickID()), trick, trick.getGuildID());
    }

    private boolean exists(String trickID, String guildID) {
        var map = TRICKS.get(guildID);
        if (map != null)
            return map.containsKey(trickID);
        return false;
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments args) {
        MessageSettings dMessage = plugin.getMessageSettings();
        Member member = message.getMember();
        if (member == null)
            return CommandResult.PASS;
        String guildID = message.getGuild().getId();
        String typeString = args.get(0);
        TrickCMDType type = typeString == null ? TrickCMDType.NONE : TrickCMDType.getType(typeString);
        String trickID = args.get(1);
        if (trickID != null)
            trickID = trickID.toLowerCase();
        boolean suppress = args.hasArg("-suppress");


        // By Default Tricks are for Guilds.
        // Will update for Users...
        if (type == TrickCMDType.ADD) {
            if (exists(trickID, guildID)) {
                dMessage.apply(message.reply("Trick Already Exists!")).queue();
                return CommandResult.PASS;
            }

            var trick = new Trick(trickID, guildID);
            if (args.hasArg("-content")) {
                trick.setType(TrickType.NORMAL);
                trick.setSuppress(suppress);
                var content = args.getFrom(args.getArgIndex("-content") + 1);
                if (content == null || content.isEmpty()) {
                    dMessage.apply(message.reply("Content Cannot be null/empty!")).queue();
                    return CommandResult.PASS;
                }
                trick.setContent(content);
            } else if (args.hasArg("-script")) {
                trick.setType(TrickType.SCRIPT);
                // Update for CodeBlocks ->
                var script = args.getFrom(args.getArgIndex("-script") + 1);
                if (script == null || script.isEmpty()) {
                    dMessage.apply(message.reply("Script Cannot be null/empty!")).queue();
                    return CommandResult.PASS;
                }
                trick.setScript(script);
            } else if (args.hasArg("-alias")) {
                // Check if Target is Normal/Scriptable too! Can be added later...

                trick.setType(TrickType.ALIAS);
                // Update for CodeBlocks ->
                var alias = args.getFrom(args.getArgIndex("-alias") + 1);
                if (alias == null || alias.isEmpty()) {
                    dMessage.apply(message.reply("Trick Cannot be null/empty!")).queue();
                    return CommandResult.PASS;
                }

                trick.setAliasTarget(alias);
            }

            trick.setOwnerID(member.getIdLong());
            trick.setLastUserEdited(member.getIdLong());
            trick.setLastEdited(System.currentTimeMillis());

            TRICKS.computeIfAbsent(guildID, (k) -> new HashMap<>()).put(trickID, trick);
            save(trick);

            dMessage.apply(message.reply("Added New Trick %s!".formatted(trickID))).queue();

        } else if (type == TrickCMDType.MODIFY) {
            if (exists(trickID, guildID)) {
                var trick = TRICKS.get(guildID).get(trickID);
                if (args.hasArg("-content")) {
                    trick.setType(TrickType.NORMAL);
                    trick.setSuppress(suppress);
                    var content = args.getFrom(args.getArgIndex("-content") + 1);
                    if (content == null || content.isEmpty()) {
                        dMessage.apply(message.reply("Content Cannot be null/empty!")).queue();
                        return CommandResult.PASS;
                    }
                    trick.setContent(content);
                } else if (args.hasArg("-script")) {
                    trick.setType(TrickType.SCRIPT);
                    // Update for CodeBlocks ->
                    var script = args.getFrom(args.getArgIndex("-script") + 1);
                    if (script == null || script.isEmpty()) {
                        dMessage.apply(message.reply("Script Cannot be null/empty!")).queue();
                        return CommandResult.PASS;
                    }
                    trick.setScript(script);
                } else if (args.hasArg("-alias")) {
                    // Check if Target is Normal/Scriptable too! Can be added later...

                    trick.setType(TrickType.ALIAS);
                    // Update for CodeBlocks ->
                    var alias = args.getFrom(args.getArgIndex("-alias") + 1);
                    if (alias == null || alias.isEmpty()) {
                        dMessage.apply(message.reply("Trick Cannot be null/empty!")).queue();
                        return CommandResult.PASS;
                    }

                    trick.setAliasTarget(alias);
                }

                trick.setLastUserEdited(member.getIdLong());
                trick.setLastEdited(System.currentTimeMillis());

                save(trick);

                dMessage.apply(message.reply("Modified Trick %s!".formatted(trickID))).queue();
                return CommandResult.PASS;
            } else {
                dMessage.apply(message.reply("Trick %s does not exist!".formatted(trickID))).queue();
            }
        } else if (type == TrickCMDType.REMOVE) {
            if (exists(trickID, guildID)) {
                var trick = TRICKS.get(guildID).get(trickID);
                delete(trick);
                TRICKS.get(guildID).remove(trickID);
                dMessage.apply(message.reply("Removed Trick %s.".formatted(trickID))).queue();
            }
        } else if (type == TrickCMDType.INFO) {
            if (!exists(trickID, guildID)) {
                dMessage.apply(message.reply("Trick %s Doesn't Exist!".formatted(trickID))).queue();
                return CommandResult.PASS;
            }

            var trick = TRICKS.get(guildID).get(trickID);

            String details = """
                    Details for Trick %s
                    
                    Type -> %s
                    Owner -> <@%s>
                    Created -> <t:%s:d> <t:%s:T>
                    LastUserEdited -> <@%s>
                    LastEdited -> <t:%s:d> <t:%s:T>
                    isLocked -> %s
                    isSuppressed -> %s
                    Times Used -> %s
                  
                    """
                    .formatted(
                            trick.getTrickID(),
                            trick.getType(),
                            trick.getOwnerID(),
                            trick.getCreated(),
                            trick.getCreated(),
                            trick.getLastUserEdited(),
                            trick.getLastEdited(),
                            trick.getLastEdited(),
                            trick.isLocked(),
                            trick.isSuppressed(),
                            trick.getTimesUsed()
                    );


            if (trick.getType() == TrickType.NORMAL) {
                details = details + "Content: \n" + trick.getContent();
            } else if (trick.getType() == TrickType.ALIAS) {
                details = details + "Alias -> " + trick.getAliasTarget();
            } else if (trick.getType() == TrickType.SCRIPT) {
                details = details + "Script: \n" + trick.getScript();
            }

            dMessage.apply(message.reply(details)).setSuppressedNotifications(true).queue();

        } else if (type == TrickCMDType.SHOW) {
            if (!exists(trickID, guildID)) {
                dMessage.apply(message.reply("Trick %s Doesn't Exist!".formatted(trickID))).queue();
                return CommandResult.PASS;
            }

            var trick = TRICKS.get(guildID).get(trickID);
            useTrick(trick, message, message.getChannel(), guildID);
        }

        /*
        !trick -s trickID
        !trick -r trickID
        !trick -i trickID

        !trick -e trickID -suppress -content Hello There!
        !trick -e trickID -script msg.reply(''Hello!');
        !trick -e trickID -alias targetID
`
        !trick -a trickID -suppress -content Hello There!
        !trick -a trickID -alias targetID
        !trick -a trickID -script msg.reply(''Hello!');
         */


        return CommandResult.PASS;
    }

    private void useTrick(Trick trick, Message message, MessageChannel channel, String guildID) {
        MessageSettings dMessage = plugin.getMessageSettings();
        var type = trick.getType();
        if (type == TrickType.NORMAL) {
            dMessage.apply(channel.sendMessage(trick.getContent())).setSuppressEmbeds(trick.isSuppressed()).queue();
            trick.use();
            save(trick);
        } else if (type == TrickType.ALIAS) {
            if (exists(trick.getAliasTarget(), guildID)) {
                var alias = TRICKS.get(guildID).get(trick.getAliasTarget());
                useTrick(alias, message, channel, guildID);
            }
        } else if (type == TrickType.SCRIPT) {
            dMessage.apply(message.reply("Scriptable Tricks currently disabled...")).queue();
        }
    }

    @Override
    public String commandId() {
        return "trick";
    }
}
