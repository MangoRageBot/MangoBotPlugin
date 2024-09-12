package org.mangorage.mangobot.modules.developer;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.IBasicCommand;
import org.mangorage.mangobotapi.core.data.DataHandler;
import org.mangorage.mangobotapi.core.data.IEmptyFileNameResolver;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

public class WhitelistBotCommand implements IBasicCommand {
    private static final List<String> ALLOWED_USERS = List.of(
                "194596094200643584"
    );
    private record SaveData(Set<String> serverIds, boolean whiteliston) implements IEmptyFileNameResolver {}

    private static final DataHandler<SaveData> WHITELIST_DATA = DataHandler.create()
            .path("data/serverwl/data")
            .file()
            .build(SaveData.class);


    private final Set<String> SERVERS = new CopyOnWriteArraySet<>();
    private final CorePlugin plugin;
    private final Timer timer = new Timer();
    private final TimerTask TASK = new TimerTask() {
        @Override
        public void run() {
            if (whitelistOn) {
                System.out.println("Checking Guilds bot is in....");
                plugin.getJDA().getGuilds().forEach(g -> {
                    if (!SERVERS.contains(g.getId())) {
                        System.out.println("Left Guild -> " + g.getIdLong());
                        g.leave().queue();
                    }
                });
            }
        }
    };
    private boolean whitelistOn = false;


    public WhitelistBotCommand(CorePlugin plugin) {
        this.plugin = plugin;
        var data = WHITELIST_DATA.loadFile(plugin.getPluginDirectory());

        data.ifPresent(d -> {
            SERVERS.addAll(d.serverIds());
            whitelistOn = d.whiteliston();
        });

        timer.scheduleAtFixedRate(TASK, 10000, 60_000 * 30);
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments arguments) {
        if (arguments.hasArg("toggle")) {
            whitelistOn = !whitelistOn;
            message.reply("Set Server Whitelist to " + (whitelistOn ? "On" : "Off")).queue();
            WHITELIST_DATA.save(plugin.getPluginDirectory(), new SaveData(SERVERS, whitelistOn));
            if (whitelistOn) runTask();
        } else if (arguments.hasArg("add")) {
            handle(true, arguments.findArg("add"), message);
        } else if (arguments.hasArg("remove")) {
            handle(false, arguments.findArg("remove"), message);
        }
        return CommandResult.PASS;
    }

    private void runTask() {

    }

    private void handle(boolean add, String id, Message message) {
        if (SERVERS.contains(id) && add) {
            message.reply("Server with Id %s is already whitelisted".formatted(id)).queue();
            return;
        }

        if (!SERVERS.contains(id) && !add) {
            message.reply("Server with Id %s is already removed from whitelist".formatted(id)).queue();
            return;
        }

        message.reply( (add ? "Added " : "Removed ") + "server with ID of " + id).queue();

        if (add) {
            SERVERS.add(id);
            if (whitelistOn) runTask();
        }

        if (!add) {
            SERVERS.remove(id);
        }

        WHITELIST_DATA.save(plugin.getPluginDirectory(), new SaveData(SERVERS, whitelistOn));
    }

    @Override
    public String commandId() {
        return "serverwl";
    }

    @Override
    public List<String> allowedUsers() {
        return ALLOWED_USERS;
    }

    @Override
    public String usage() {
        return """
                serverwl toggle
                serverwl add/remove id
                """;
    }
}
