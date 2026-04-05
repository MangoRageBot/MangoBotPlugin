package org.mangorage.mangobotplugin.entrypoint;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LinkState;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.loadbalancing.VoiceRegion;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import dev.arbjerg.lavalink.protocol.v4.VoiceState;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.mangorage.mangobotcore.api.command.v1.ICommandDispatcher;
import org.mangorage.mangobotcore.api.config.v1.ConfigTypes;
import org.mangorage.mangobotcore.api.config.v1.IConfig;
import org.mangorage.mangobotcore.api.config.v1.IConfigSetting;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.permission.v1.JDAPermissionManager;
import org.mangorage.mangobotcore.api.jda.permission.v1.JDAPermissionNode;
import org.mangorage.mangobotcore.api.plugin.v1.MangoBotPlugin;
import org.mangorage.mangobotcore.api.plugin.v1.Plugin;
import org.mangorage.mangobotcore.api.plugin.v1.PluginManager;
import org.mangorage.mangobotcore.api.util.data.DatabaseHandler;
import org.mangorage.mangobotcore.api.util.jda.ButtonActionRegistry;
import org.mangorage.mangobotcore.api.util.jda.MessageSettings;
import org.mangorage.mangobotcore.api.util.jda.slash.command.Command;
import org.mangorage.mangobotplugin.BotEventListener;
import org.mangorage.mangobotplugin.commands.internal.homedepot.HomeDepotCommand;
import org.mangorage.mangobotplugin.commands.misc.HelpCommand;
import org.mangorage.mangobotplugin.commands.music.impl.MusicCommand;
import org.mangorage.mangobotplugin.commands.trick.TrickManager;
import org.mangorage.mangobotplugin.commands.trick.impl.TrickCommand;
import org.mangorage.mangobotplugin.actions.TrashButtonAction;
import org.mangorage.mangobotplugin.commands.misc.PingCommand;
import org.mangorage.mangobotplugin.commands.misc.PingsCommand;
import org.slf4j.LoggerFactory;

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
    private final JDAPermissionManager permissionManager = JDAPermissionManager.create(
            DatabaseHandler.create(
                    MangoBot.BOT_DATABASE_URL.get(),
                    MangoBot.BOT_DATABASE_USERNAME.get(),
                    MangoBot.BOT_DATABASE_PASSWORD.get(),
                    JDAPermissionNode.class
            )
    );

    private final LavalinkClient client;
    private JDA jda;

    public MangoBot() {
        ACTION_REGISTRY.register(new TrashButtonAction());

        this.client = new LavalinkClient(
                Helpers.getUserIdFromToken(BOT_TOKEN.get())
        );

        client.addNode(
                new NodeOptions.Builder()
                        .setName("Main")
                        .setServerUri("https://lavalinkv4.serenetia.com:443")
                        .setPassword("https://seretia.link/discord")
                        .build()
        );

        commandDispatcher.register(new HelpCommand("help", getCommandDispatcher()));
        commandDispatcher.register(new PingCommand("ping"));
        commandDispatcher.register(new PingsCommand("pings"));;
        commandDispatcher.register(new HomeDepotCommand("homedepot"));
        commandDispatcher.register(new TrickCommand("trick", this));
        commandDispatcher.register(new MusicCommand("music", client));

        final var node = permissionManager.getPermissionNode("test_node");
        node.authorizeUser(null, 194596094200643584L);
        node.authoriseRoleId(null, 1128896227896224096L);
        node.addRequiredPermission(Permission.ADMINISTRATOR);
        permissionManager.savePermissionNode(node);
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
                .setVoiceDispatchInterceptor(
                        new VoiceDispatchInterceptor() {
                            @Override
                            public void onVoiceServerUpdate(VoiceServerUpdate update) {
                                String channelId = null;

                                if (update.getGuild().getSelfMember().getVoiceState() != null &&
                                        update.getGuild().getSelfMember().getVoiceState().getChannel() != null) {
                                    channelId = update.getGuild().getSelfMember().getVoiceState().getChannel().getId();
                                }

                                VoiceState state = new VoiceState(
                                        update.getToken(),
                                        update.getEndpoint(),
                                        update.getSessionId(),
                                        channelId // yeah, still sketchy here too
                                );

                                VoiceRegion region = VoiceRegion.fromEndpoint(update.getEndpoint());
                                var link = client.getOrCreateLink(update.getGuildIdLong(), region);

                                link.onVoiceServerUpdate(state);
                            }

                            @Override
                            public boolean onVoiceStateUpdate(VoiceStateUpdate update) {
                                var channel = update.getChannel();
                                var link = client.getLinkIfCached(update.getGuildIdLong());
                                if (link == null) return false;

                                var player = link.getNode().getCachedPlayer(update.getGuildIdLong());
                                if (player == null) return false;

                                var playerState = player.getState();

                                if (channel == null) {
                                    if (playerState.getConnected()) {
                                        link.setState$lavalink_client(LinkState.CONNECTED);
                                    } else {
                                        link.setState$lavalink_client(LinkState.DISCONNECTED);
                                        link.destroy().subscribe();
                                    }
                                }

                                // Return true if a connection was previously established
                                return playerState.getConnected();
                            }
                        }
                )
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
