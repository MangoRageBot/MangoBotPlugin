package org.mangorage.mangobotplugin.entrypoint;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.mangorage.commonutils.jda.ButtonActionRegistry;
import org.mangorage.commonutils.jda.MessageSettings;
import org.mangorage.commonutils.jda.slash.command.Command;
import org.mangorage.mangobotcore.config.api.ConfigTypes;
import org.mangorage.mangobotcore.config.api.IConfig;
import org.mangorage.mangobotcore.config.api.IConfigSetting;
import org.mangorage.mangobotcore.jda.command.api.CommandManager;
import org.mangorage.mangobotcore.plugin.api.MangoBotPlugin;
import org.mangorage.mangobotcore.plugin.api.Plugin;
import org.mangorage.mangobotplugin.BotEventListener;
import org.mangorage.mangobotplugin.pagedlist.PagedListManager;
import org.mangorage.mangobotplugin.actions.TrashButtonAction;
import org.mangorage.mangobotplugin.commands.PingCommand;
import org.mangorage.mangobotplugin.commands.PingsCommand;
import org.mangorage.mangobotplugin.commands.internal.EmojiCommand;
import org.mangorage.mangobotplugin.commands.music.commands.PauseCommand;
import org.mangorage.mangobotplugin.commands.music.commands.PlayCommand;
import org.mangorage.mangobotplugin.commands.music.commands.PlayingCommand;
import org.mangorage.mangobotplugin.commands.music.commands.QueueCommand;
import org.mangorage.mangobotplugin.commands.music.commands.SkipCommand;
import org.mangorage.mangobotplugin.commands.music.commands.StopCommand;
import org.mangorage.mangobotplugin.commands.music.commands.VolumeCommand;
import org.mangorage.mangobotplugin.commands.trick.TrickCommand;

import java.nio.file.Path;
import java.util.EnumSet;

@MangoBotPlugin(id = MangoBot.ID)
public final class MangoBot implements Plugin {
    public static final String ID = "mangobot";

    public static final ButtonActionRegistry ACTION_REGISTRY = new ButtonActionRegistry();

    // Where we create our "config"
    public final static IConfig CONFIG =  IConfig.create(Path.of("plugins/%s/.env".formatted(MangoBot.ID)));
    // Where we create Settings for said Config
    public static final IConfigSetting<String> BOT_TOKEN = IConfigSetting.create(CONFIG, "BOT_TOKEN", ConfigTypes.STRING, "empty");

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

    private final CommandManager commandManager = CommandManager.create();
    private final PagedListManager pagedListManager = new PagedListManager();

    private JDA jda;

    public MangoBot() {
        ACTION_REGISTRY.register(new TrashButtonAction());

        commandManager.register(new EmojiCommand());

        commandManager.register(new PingCommand());
        commandManager.register(new TrickCommand(this));
        commandManager.register(new PingsCommand());

        commandManager.register(new PauseCommand());
        commandManager.register(new PlayCommand());
        commandManager.register(new PlayingCommand());
        commandManager.register(new QueueCommand(pagedListManager));
        commandManager.register(new StopCommand());
        commandManager.register(new VolumeCommand());
        commandManager.register(new SkipCommand());
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

        jda.updateCommands()
                .addCommands(
                        Command.globalCommands
                ).queue();
        
        System.out.println("Launched");
    }

    public JDA getJDA() {
        return jda;
    }

    public CommandManager getCommandManager() {
        return commandManager;
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

}
