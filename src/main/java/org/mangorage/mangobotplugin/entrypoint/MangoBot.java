package org.mangorage.mangobotplugin.entrypoint;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.mangorage.mangobotcore.api.command.v1.ICommandDispatcher;
import org.mangorage.mangobotcore.api.config.v1.ConfigTypes;
import org.mangorage.mangobotcore.api.config.v1.IConfig;
import org.mangorage.mangobotcore.api.config.v1.IConfigSetting;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.plugin.v1.MangoBotPlugin;
import org.mangorage.mangobotcore.api.plugin.v1.Plugin;
import org.mangorage.mangobotcore.api.util.jda.ButtonActionRegistry;
import org.mangorage.mangobotcore.api.util.jda.MessageSettings;
import org.mangorage.mangobotcore.api.util.jda.slash.command.Command;
import org.mangorage.mangobotplugin.BotEventListener;
import org.mangorage.mangobotplugin.commands.internal.homedepot.HomeDepotCommand;
import org.mangorage.mangobotplugin.commands.misc.HelpCommand;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.commands.trick.impl.TrickCommand;
import org.mangorage.mangobotplugin.pagedlist.PagedListManager;
import org.mangorage.mangobotplugin.actions.TrashButtonAction;
import org.mangorage.mangobotplugin.commands.misc.PingCommand;
import org.mangorage.mangobotplugin.commands.misc.PingsCommand;
import java.nio.file.Path;
import java.util.EnumSet;

@MangoBotPlugin(id = MangoBot.ID)
public final class MangoBot implements Plugin {
    public static final String ID = "mangobot";

    public static final ButtonActionRegistry ACTION_REGISTRY = new ButtonActionRegistry();

    // Where we create our "config"
    public final static IConfig CONFIG = IConfig.create(Path.of("plugins/%s/.env".formatted(MangoBot.ID)));

    // Where we create Settings for said Config
    public static final IConfigSetting<String> BOT_TOKEN = IConfigSetting.create(CONFIG, "BOT_TOKEN", ConfigTypes.STRING, "empty");
    public static final IConfigSetting<String> BOT_DATABASE_URL = IConfigSetting.create(CONFIG, "BOT_DATABASE_URL", ConfigTypes.STRING, "empty");
    public static final IConfigSetting<String> BOT_DATABASE_USERNAME = IConfigSetting.create(CONFIG, "BOT_DATABASE_USERNAME", ConfigTypes.STRING, "empty");
    public static final IConfigSetting<String> BOT_DATABASE_PASSWORD = IConfigSetting.create(CONFIG, "BOT_DATABASE_PASSWORD", ConfigTypes.STRING, "empty");
    public static final IConfigSetting<Boolean> BOT_USE_DATABASE = IConfigSetting.create(CONFIG, "BOT_USE_DATABASE", ConfigTypes.BOOLEAN, false);

    private static final EnumSet<GatewayIntent> intents = EnumSet.of(
            // Enables MessageReceivedEvent for guild (also known as servers)
            GatewayIntent.GUILD_MESSAGES,
            // Enables the event for private channels (also known as direct messages)
            GatewayIntent.DIRECT_MESSAGES,
            // Enables access to message.getContentRaw()
            GatewayIntent.MESSAGE_CONTENT,
            // Enables MessageReactionAddEvent for guild
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            // Enables MessageReactionAddEvent for private channels
            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.SCHEDULED_EVENTS,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_PRESENCES
    );

    private static final EnumSet<CacheFlag> cacheFlags = EnumSet.of(
            CacheFlag.EMOJI,
            CacheFlag.ROLE_TAGS,
            CacheFlag.VOICE_STATE,
            CacheFlag.ACTIVITY,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.MEMBER_OVERRIDES,
            CacheFlag.STICKER,
            CacheFlag.SCHEDULED_EVENTS,
            CacheFlag.FORUM_TAGS
    );

    private final ICommandDispatcher<Message, JDACommandResult> commandDispatcher = ICommandDispatcher.create(JDACommandResult.INVALID_COMMAND);
    private final TrickManager trickManager = new TrickManager(this);
    private final PagedListManager pagedListManager = new PagedListManager();

    private JDA jda;

    public MangoBot() {
        ACTION_REGISTRY.register(new TrashButtonAction());

        commandDispatcher.register(new HelpCommand("help", getCommandDispatcher()));
        commandDispatcher.register(new PingCommand("ping"));
        commandDispatcher.register(new PingsCommand("pings"));;
        commandDispatcher.register(new HomeDepotCommand("homedepot"));
        commandDispatcher.register(new TrickCommand("trick", this));
    }

    @Override
    public String getId() {
        return ID;
    }

    public void load() {
        jda = JDABuilder.createDefault(BOT_TOKEN.get())
                .setEnabledIntents(intents)
                .enableCache(cacheFlags)
                .setActivity(
                        Activity.of(
                                Activity.ActivityType.WATCHING,
                                "https://mangobot.mangorage.org/"
                        )
                )
                .setStatus(OnlineStatus.ONLINE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setEventManager(new AnnotatedEventManager())
                .setEnableShutdownHook(true)
                .setAutoReconnect(true)
                .build();

        getJDA().addEventListener(new BotEventListener(this));

        registerSlashCommands();

        jda.updateCommands()
                .addCommands(
                        Command.globalCommands
                ).queue();

        System.out.println("Launched");
    }

    public JDA getJDA() {
        return jda;
    }

    public ICommandDispatcher<Message, JDACommandResult> getCommandDispatcher() {
        return commandDispatcher;
    }

    public PagedListManager getPagedListManager() {
        return pagedListManager;
    }

    public Path getPluginDirectory() {
        return Path.of("plugins").resolve(ID).toAbsolutePath();
    }

    public MessageSettings getMessageSettings() {
        return MessageSettings.create().build();
    }

    public TrickManager getTrickManager() {
        return trickManager;
    }

    void registerSlashCommands() {
        Command.slash("flipper", "flips things")
                .executes(listener -> {
                    listener.getMessageChannel().sendTyping().queue();
                    listener.getMessageChannel().sendMessage("You flipped a rock and exploded the universe!").queue();
                })
                .buildAndRegister();
    }
}
