package org.mangorage.mangobot.modules.tricks;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.mangorage.basicutils.LogHelper;
import org.mangorage.basicutils.TaskScheduler;
import org.mangorage.basicutils.misc.PagedList;
import org.mangorage.basicutils.misc.RunnableTask;
import org.mangorage.jdautils.command.Command;
import org.mangorage.jdautils.command.CommandOption;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandAlias;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.IBasicCommand;
import org.mangorage.mangobotapi.core.data.DataHandler;
import org.mangorage.mangobotapi.core.events.BasicCommandEvent;
import org.mangorage.mangobotapi.core.events.LoadEvent;
import org.mangorage.mangobotapi.core.events.SaveEvent;
import org.mangorage.mangobotapi.core.events.discord.DButtonInteractionEvent;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;
import org.mangorage.mangobotapi.core.util.MessageSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class TrickCommand implements IBasicCommand {
    private static final boolean ALLOW_SCRIPT_TRICKS = true;
    private final CorePlugin plugin;
    private final DataHandler<Trick> TRICK_DATA_HANDLER;
    private final Map<String, Map<String, Trick>> TRICKS = new HashMap<>();
    private final Map<String, PagedList<String>> PAGES = new ConcurrentHashMap<>();
    private TrickScriptable SCRIPT_RUNNER;

    public TrickCommand(CorePlugin plugin) {
        this.plugin = plugin;
        this.SCRIPT_RUNNER = new TrickScriptable(plugin);
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
        plugin.getPluginBus().addListener(DButtonInteractionEvent.class, this::onButton);

        Command.slash("trick", "Displays a Trick!")
                .addSubCommand("execute", "execute a trick")
                    .executes(e -> {
                        e.reply("Trick Executed! " + e.getInteraction().getOption("name")).queue();
                    })
                    .addOption(
                            new CommandOption(OptionType.STRING, "name", "desc", false, true)
                                    .onAutoComplete(e -> {
                                        var guild = e.getGuild();
                                        if (guild != null) {
                                            var id = guild.getId();
                                            var entries = TRICKS.get(id);
                                            if (entries != null) {
                                                e.replyChoiceStrings(
                                                        entries.entrySet().stream()
                                                                .filter(k -> k.getValue().getType() == TrickType.NORMAL)
                                                                .map(Map.Entry::getKey)
                                                                .limit(25)
                                                                .toList()
                                                ).queue();
                                            }
                                        }
                                    })
                    )
                    .build()
                .buildAndRegister();
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

    private boolean isOwnerAndUnlocked(Trick trick, Member member) {
        if (!trick.isLocked()) return true;
        return trick.getOwnerID() == member.getIdLong();
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

        if (type == TrickCMDType.NONE) {
            return CommandResult.PASS;
        }

        // By Default Tricks are for Guilds.
        // Will update for Users...
        if (type == TrickCMDType.ADD) {
            if (exists(trickID, guildID)) {
                dMessage.apply(message.reply("Trick '%s' Already Exists!".formatted(trickID))).queue();
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

            dMessage.apply(message.reply("Added New Trick '%s'!".formatted(trickID))).queue();

        } else if (type == TrickCMDType.MODIFY) {
            if (exists(trickID, guildID)) {
                var trick = TRICKS.get(guildID).get(trickID);

                if (!isOwnerAndUnlocked(trick, member)) {
                    dMessage.apply(message.reply("Cannot modify/remove Trick '%s' as your not the owner of this trick and its locked.".formatted(trickID))).queue();
                    return CommandResult.PASS;
                }

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
                dMessage.apply(message.reply("Trick '%s' does not exist!".formatted(trickID))).queue();
            }
        } else if (type == TrickCMDType.REMOVE) {
            if (exists(trickID, guildID)) {
                var trick = TRICKS.get(guildID).get(trickID);

                if (!isOwnerAndUnlocked(trick, member)) {
                    dMessage.apply(message.reply("Cannot modify/remove Trick '%s' as your not the owner of this trick and its locked.".formatted(trickID))).queue();
                    return CommandResult.PASS;
                }

                delete(trick);
                TRICKS.get(guildID).remove(trickID);
                dMessage.apply(message.reply("Removed Trick %s.".formatted(trickID))).queue();
            }
        } else if (type == TrickCMDType.INFO) {
            if (!exists(trickID, guildID)) {
                dMessage.apply(message.reply("Trick '%s' does not exist!".formatted(trickID))).queue();
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
                dMessage.apply(message.reply("Trick '%s' does not exist!".formatted(trickID))).queue();
                return CommandResult.PASS;
            }

            var trick = TRICKS.get(guildID).get(trickID);
            useTrick(trick, message, message.getChannel(), guildID, args);
        } else if (type == TrickCMDType.LIST) {
            int length;

            if (trickID != null) {
                try {
                    length = Integer.parseInt(trickID);
                } catch (NumberFormatException e) {
                    length = 5;
                }
            } else {
                length = 5;
            }

            MessageChannelUnion channel = message.getChannel();
            if (TRICKS.containsKey(guildID) && !TRICKS.get(guildID).isEmpty()) {

                PagedList<String> tricks = createTricks(guildID, length);

                channel.sendMessage("""
                        Getting Tricks List... 
                        """).queue((m -> {
                            PAGES.put(m.getId(), tricks);
                            TaskScheduler.getExecutor().schedule(new RunnableTask<>(m, (d) -> removeTricksList(d.get())), 10, TimeUnit.MINUTES);
                            updateTrickListMessage(tricks, m, true);
                        })
                );
            }
            return CommandResult.PASS;
        } else if (type == TrickCMDType.LOCK) {
            if (!exists(trickID, guildID)) {
                dMessage.apply(message.reply("Trick '%s' does not exist!".formatted(trickID)));
                return CommandResult.PASS;
            }

            var trick = TRICKS.get(guildID).get(trickID);
            if (trick.getOwnerID() == member.getIdLong()) {
                dMessage.apply(
                        message.reply((trick.isLocked() ? "Unlocked" : "Locked") + " Trick '%s'".formatted(trickID))
                ).queue();
                trick.setLock(!trick.isLocked());
                save(trick);
            } else {
                dMessage.apply(message.reply("Can only lock/unlock your own Tricks!")).queue();
            }
        } else if (type == TrickCMDType.TRANSFER) {
            // TODO: Add transfer ability...
        }

        /*
        !trick -s trickID
        !trick -r trickID
        !trick -i trickID
        !trick -l <10>

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

    private void useTrick(Trick trick, Message message, MessageChannel channel, String guildID, Arguments args) {
        MessageSettings dMessage = plugin.getMessageSettings();
        var type = trick.getType();
        if (type == TrickType.NORMAL) {
            dMessage.apply(channel.sendMessage(trick.getContent())).setSuppressEmbeds(trick.isSuppressed()).queue();
            trick.use();
            save(trick);
        } else if (type == TrickType.ALIAS) {
            if (exists(trick.getAliasTarget(), guildID)) {
                var alias = TRICKS.get(guildID).get(trick.getAliasTarget());
                useTrick(alias, message, channel, guildID, args);
            }
        } else if (type == TrickType.SCRIPT) {
            if (!ALLOW_SCRIPT_TRICKS) {
                dMessage.apply(message.reply("Scriptable Tricks currently disabled...")).queue();
            } else {
                // Execute code
                var script = MarkdownSanitizer.sanitize(trick.getScript());
                SCRIPT_RUNNER.execute(
                        script,
                        message,
                        channel,
                        args.getArgs().length > 1 ? args.getFrom(2).split(" ") : new String[]{}
                );
            }
        }
    }

    @Override
    public String commandId() {
        return "trick";
    }

    @Override
    public List<String> commandAliases() {
        return CommandAlias.create(this, "tricks", "tr");
    }

    @Override
    public String usage() {
        return """
                ## `!tricks`
                `-a` to add, `-e` to edit, `-s` to view source, `-r` to remove, `-l` to list.
                - To run a trick, use its ID as if it were another command. E.g.: `!drivers`
                - When adding or editing, you can optionally add the `-supress` arg to supress embeds in your trick's links.
                - When listing tricks, you can optionally specify how many you want per page.
               
               
                ## Examples:
                
                ## How to add tricks: 
                `!tricks -a exampletrick -content this is an example trick`
                `!tricks -a exampletrickalias -alias exampletrick`
                
                ## How to modify tricks:
                `!tricks -e exampletrick -supress -content editing the trick. https://bing.com`
                `!tricks -e exampletrickalias -alias exampletrick`
                
                ## How to lock/unlock a trick:
                `!tricks -lock exampletrick` // Do it again to unlock
               
                ## How to show tricks:
                `!tricks -s exampletrick`
                
                ## How to remove tricks:
                `!tricks -r exampletrick`
                
                ## How to show a list of tricks:
                `!tricks -l 10`
                """;
    }

    private void removeTricksList(Message message) {
        if (PAGES.containsKey(message.getId())) {
            message.editMessage(createTricksString(PAGES.get(message.getId()))).setComponents().queue();
            PAGES.remove(message.getId());
        }
    }

    private void updateTrickListMessage(PagedList<String> tricks, Message message, boolean addButtons, String buttonID) {
        switch (buttonID) {
            case "next" -> tricks.next();
            case "prev" -> tricks.previous();
        }

        String result = createTricksString(tricks);

        if (addButtons) {
            // Add buttons!
            Button prev = Button.primary("prev".formatted(message.getId()), "previous");
            Button next = Button.primary("next".formatted(message.getId()), "next");

            message.editMessage(result).setActionRow(prev, next).queue();
        } else {
            message.editMessage(result).queue();
        }
    }

    private void updateTrickListMessage(PagedList<String> tricks, Message message, boolean addButtons) {
        updateTrickListMessage(tricks, message, addButtons, "");
    }

    private String createTricksString(PagedList<String> tricks) {
        String result = "List of Tricks (%s / %s) \r".formatted(tricks.getPage(), tricks.totalPages());

        PagedList.Page<String> entries = tricks.current();

        int i = 0;
        for (String entry : entries.getEntries()) {
            i++;
            result = result + "%s: %s \r".formatted(i, entry);
        }

        return result;
    }

    private PagedList<String> createTricks(String guildID, int entries) {
        PagedList<String> tricks = new PagedList<>();

        Object[] LIST = TRICKS.get(guildID).keySet().toArray();
        tricks.rebuild(Arrays.copyOf(LIST, LIST.length, String[].class), entries);

        return tricks;
    }

    public void onButton(DButtonInteractionEvent event) {
        var interaction = event.get();

        Message message = interaction.getMessage();
        String ID = message.getId();

        if (PAGES.containsKey(ID)) {
            updateTrickListMessage(PAGES.get(ID), message, false, interaction.getButton().getId());
            interaction.getInteraction().deferEdit().queue();
        }
    }
}
