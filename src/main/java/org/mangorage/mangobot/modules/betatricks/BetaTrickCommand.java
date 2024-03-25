package org.mangorage.mangobot.modules.betatricks;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BetaTrickCommand implements IBasicCommand {


    private final CorePlugin plugin;
    private final DataHandler<BetaTrick> TRICK_DATA_HANDLER;
    private final Map<String, Map<String, BetaTrick>> TRICKS = new HashMap<>();


    public BetaTrickCommand(CorePlugin plugin) {
        this.plugin = plugin;
        this.TRICK_DATA_HANDLER = DataHandler.create(
                (data) -> {
                    if (data.isGuild()) // If its a guild based trick, then goes here...
                        TRICKS.computeIfAbsent(data.getId(), (k) -> new HashMap<>()).put(data.getTrickID(), data);
                },
                BetaTrick.class,
                "plugins/%s/data/btricks".formatted(plugin.getId()),
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


    private void delete(BetaTrick trick) {
        TRICK_DATA_HANDLER.deleteFile("%s.json".formatted(trick.getTrickID()), (trick.isGuild() ? "g-" : "u-") + trick.getId());
    }

    private void save(BetaTrick trick) {
        TRICK_DATA_HANDLER.save("%s.json".formatted(trick.getTrickID()), trick, (trick.isGuild() ? "g-" : "u-") + trick.getId());
    }

    private boolean exists(String trickID, String id, boolean isGuild) {
        if (isGuild) {
            var map = TRICKS.get(id);
            if (map != null)
                return map.containsKey(trickID);
        } else {

        }

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
            if (exists(trickID, guildID, true)) {
                dMessage.apply(message.reply("Trick Already Exists!")).queue();
                return CommandResult.PASS;
            }

            var trick = new BetaTrick(trickID, guildID, true);
            if (args.hasArg("-content")) {
                trick.setType(TrickType.NORMAL);
                trick.setSuppress(suppress);
                trick.setContent(args.getFrom(args.getArgIndex("-content") + 1));
            } else if (args.hasArg("-script")) {
                trick.setType(TrickType.SCRIPT);
                // Update for CodeBlocks ->
                trick.setScript(args.getFrom(args.getArgIndex("-script") + 1));
            } else if (args.hasArg("-alias")) {
                // Check if Target is Normal/Scriptable too! Can be added later...

                trick.setType(TrickType.ALIAS);
                // Update for CodeBlocks ->
                trick.setAliasTarget(args.getFrom(args.getArgIndex("-alias") + 1));
            }

            trick.setOwnerID(member.getIdLong());
            trick.setLastUserEdited(member.getIdLong());
            trick.setLastEdited(System.currentTimeMillis());

            TRICKS.computeIfAbsent(guildID, (k) -> new HashMap<>()).put(trickID, trick);
            save(trick);

            dMessage.apply(message.reply("Added New Trick %s!".formatted(trickID))).queue();

        } else if (type == TrickCMDType.MODIFY) {

        } else if (type == TrickCMDType.REMOVE) {

        } else if (type == TrickCMDType.INFO) {
            if (!exists(trickID, guildID, true)) {
                dMessage.apply(message.reply("Trick %s Doesn't Exist!".formatted(trickID))).queue();
                return CommandResult.PASS;
            }

            var trick = TRICKS.get(guildID).get(trickID);

            String details = """
                    Details for Trick %s
                    
                    Type -> %s
                    Owner -> <@%s>
                    LastUserEdited -> <@%s>
                    LastEdited -> %s
                    isGuild -> %s
                    isLocked -> %s
                    isSuppressed -> %s
                    Times Used -> %s
                  
                    """
                    .formatted(
                            trick.getTrickID(),
                            trick.getType(),
                            trick.getOwnerID(),
                            trick.getLastUserEdited(),
                            convertMillisecondsToDate(trick.getLastEdited()),
                            trick.isGuild(),
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
            if (!exists(trickID, guildID, true)) {
                dMessage.apply(message.reply("Trick %s Doesn't Exist!".formatted(trickID))).queue();
                return CommandResult.PASS;
            }

            var trick = TRICKS.get(guildID).get(trickID);
            var trickType = trick.getType();
            if (trickType == TrickType.NORMAL) {
                MessageChannelUnion channel = message.getChannel();

                channel.sendMessage(trick.getContent()).setSuppressedNotifications(trick.isSuppressed()).queue();
                trick.use();
                save(trick);

            } else if (trickType == TrickType.ALIAS) {

            } else if (trickType == TrickType.SCRIPT) {
                // WIP
                dMessage.apply(message.reply("Scriptable Tricks are not yet added!")).queue();
            }
        }

        /*
        !trick -s trickID
        !trick -r trickID
        !trick -i trickID

        !trick -e trickID -suppress -content Hello There!
        !trick -e trickID -script msg.reply(''Hello!');
        !trick -e trickID -alias targetID

        !trick -a trickID -suppress -content Hello There!
        !trick -a trickID -alias targetID
        !trick -a trickID -script msg.reply(''Hello!');
         */


        return CommandResult.PASS;
    }

    public String convertMillisecondsToDate(long milliseconds) {
        // Create a Date object using milliseconds
        Date date = new Date(milliseconds);

        // Create a SimpleDateFormat object to format the date
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        // Format the date
        return sdf.format(date);
    }

    @Override
    public String commandId() {
        return "betaTrick";
    }
}
