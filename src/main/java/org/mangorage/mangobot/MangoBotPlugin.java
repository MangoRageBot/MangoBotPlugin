/*
 * Copyright (c) 2023. MangoRage
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.mangorage.mangobot;

import static org.mangorage.mangobot.core.BotPermissions.CUSTOM_VC_ADMIN;
import static org.mangorage.mangobot.core.BotPermissions.MOD_MAIL;
import static org.mangorage.mangobot.core.BotPermissions.PERMISSION_ADMIN;
import static org.mangorage.mangobot.core.BotPermissions.PLAYING;
import static org.mangorage.mangobot.core.BotPermissions.PREFIX_ADMIN;
import static org.mangorage.mangobot.core.BotPermissions.TRICK_ADMIN;

import java.nio.file.Path;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.mangorage.basicutils.config.Config;
import org.mangorage.basicutils.config.ConfigSetting;
import org.mangorage.basicutils.config.ISetting;
import org.mangorage.jdautils.command.Command;
import org.mangorage.mangobot.config.GuildConfig;
import org.mangorage.mangobot.core.BotEventListener;
import org.mangorage.mangobot.core.BotPermissions;
import org.mangorage.mangobot.core.Listeners;
import org.mangorage.mangobot.modules.actions.TrashButtonAction;
import org.mangorage.mangobot.modules.basic.commands.AntiPingCommand;
import org.mangorage.mangobot.modules.basic.commands.GetEmbedsCommand;
import org.mangorage.mangobot.modules.basic.commands.HelpCommand;
import org.mangorage.mangobot.modules.basic.commands.InfoCommand;
import org.mangorage.mangobot.modules.basic.commands.JoinCommand;
import org.mangorage.mangobot.modules.basic.commands.LeaveCommand;
import org.mangorage.mangobot.modules.basic.commands.PermissionCommand;
import org.mangorage.mangobot.modules.basic.commands.PingCommand;
import org.mangorage.mangobot.modules.basic.commands.PrefixCommand;
import org.mangorage.mangobot.modules.basic.commands.VersionCommand;
import org.mangorage.mangobot.modules.developer.WhitelistBotCommand;
import org.mangorage.mangobot.modules.tricks.TrickCommand;
import org.mangorage.mangobot.modules.developer.EchoCommand;
import org.mangorage.mangobot.modules.developer.KickBotCommand;
import org.mangorage.mangobot.modules.developer.RestartCommand;
import org.mangorage.mangobot.modules.developer.RunCode;
import org.mangorage.mangobot.modules.developer.SpeakCommand;
import org.mangorage.mangobot.modules.developer.TerminateCommand;
import org.mangorage.mangobot.modules.github.GHIssueStatus;
import org.mangorage.mangobot.modules.github.GHPRStatus;
import org.mangorage.mangobot.modules.github.IssueScanCommand;
import org.mangorage.mangobot.modules.github.PRScanCommand;
import org.mangorage.mangobot.modules.github.PasteRequestModule;
import org.mangorage.mangobot.modules.mappings.ClassMapCommand;
import org.mangorage.mangobot.modules.mappings.DefMapCommand;
import org.mangorage.mangobot.modules.mappings.FCICommand;
import org.mangorage.mangobot.modules.mappings.MCPCommand;
import org.mangorage.mangobot.modules.mappings.MappingsMainCommand;
import org.mangorage.mangobot.modules.mappings.MappingsManager;
import org.mangorage.mangobot.modules.mappings.VarMapCommand;
import org.mangorage.mangobot.modules.mappings.YCCommand;
import org.mangorage.mangobot.modules.mappings.YFCommand;
import org.mangorage.mangobot.modules.mappings.YMCommand;
import org.mangorage.mangobot.modules.music.commands.PauseCommand;
import org.mangorage.mangobot.modules.music.commands.PlayCommand;
import org.mangorage.mangobot.modules.music.commands.PlayingCommand;
import org.mangorage.mangobot.modules.music.commands.QueueCommand;
import org.mangorage.mangobot.modules.music.commands.StopCommand;
import org.mangorage.mangobot.modules.music.commands.VolumeCommand;
import org.mangorage.mangobotapi.core.events.DiscordEvent;
import org.mangorage.mangobotapi.core.events.LoadEvent;
import org.mangorage.mangobotapi.core.events.SaveEvent;
import org.mangorage.mangobotapi.core.modules.action.ButtonActionRegistry;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;
import org.mangorage.mangobotapi.core.plugin.api.PluginMessageEvent;
import org.mangorage.mangobotapi.core.plugin.impl.Plugin;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@Plugin(id = MangoBotPlugin.ID)
public class MangoBotPlugin extends CorePlugin {
    public static final String ID = "mangobot";
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

    // Where we create our "config"
    public final static Config CONFIG = new Config(Path.of("plugins/%s/.env".formatted(MangoBotPlugin.ID)));

    // Where we create Settings for said Config
    public static final ISetting<String> MAPPINGS_VERSION = ConfigSetting.create(CONFIG, "MAPPINGS_VERSION", "empty");
    public static final ISetting<String> BOT_TOKEN = ConfigSetting.create(CONFIG, "BOT_TOKEN", "empty");
    public static final ISetting<String> GITHUB_TOKEN = ConfigSetting.create(CONFIG, "PASTE_TOKEN", "empty");
    public static final ISetting<String> GITHUB_USERNAME = ConfigSetting.create(CONFIG, "GITHUB_USERNAME", "RealMangoRage");
    public static final ISetting<String> DEEPL_TOKEN = ConfigSetting.create(CONFIG, "DEEPL_TOKEN", "empty");
    public static final ButtonActionRegistry ACTION_REGISTRY = new ButtonActionRegistry();


    public MangoBotPlugin() {
        super(
                MangoBotPlugin.ID,
                JDABuilder.createDefault(BOT_TOKEN.get())
                        .setEnabledIntents(intents)
                        .enableCache(cacheFlags)
                        .setActivity(
                                Activity.of(
                                        Activity.ActivityType.CUSTOM_STATUS,
                                        """
                                                    MangoBot is on version %s
                                                """.formatted(VersionCommand.getVersion()),
                                        "https://www.discord.minecraftforge.net/"
                                )
                        )
                        .setStatus(OnlineStatus.ONLINE)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .setEventManager(new AnnotatedEventManager())
                        .setEnableShutdownHook(true)
                        .setAutoReconnect(true)
                        .build()
        );

        getJDA().addEventListener(new BotEventListener(this));
        getPluginBus().addGenericListener(100, GuildMemberJoinEvent.class, DiscordEvent.class, this::onUserJoin);
    }

    public void onUserJoin(DiscordEvent<GuildMemberJoinEvent> event) {
        var e = event.getInstance();

        System.out.println(e.getUser().getGlobalName());
        System.out.println(e.getUser().getName());
        System.out.println(e.getUser().getEffectiveName());

        var gn = e.getUser().getName();

        if (gn.contains("lexlover") && e.getGuild().getIdLong() == 1129059589325852724L) {
            e.getMember()
                    .ban(6, TimeUnit.DAYS)
                    .reason("Spam")
                    .queue();
        }
    }

    @Override
    public void startup() {
        BotPermissions.init();
    }

    private static final List<Long> AUTO_PUBLISH_CHANNELS = List.of(1129095997461647402L, 1129077934330744882L);

    public void onMessage(DiscordEvent<MessageReceivedEvent> event) {
        var dEvent = event.getInstance();
        var channel = dEvent.getChannel().getIdLong();
        if (AUTO_PUBLISH_CHANNELS.contains(channel)) {
            var msg = dEvent.getMessage();
            if (dEvent.isFromType(ChannelType.NEWS))
                msg.crosspost().queue();
        }
    }


    @Override
    public void registration() {
        var cmdRegistry = getCommandRegistry();
        var permRegistry = getPermissionRegistry();

        ACTION_REGISTRY.register(new TrashButtonAction());

        permRegistry.register(PLAYING);
        permRegistry.register(TRICK_ADMIN);
        permRegistry.register(PREFIX_ADMIN);
        permRegistry.register(MOD_MAIL);
        permRegistry.register(PERMISSION_ADMIN);
        permRegistry.register(CUSTOM_VC_ADMIN);


        // Basic Commands
        cmdRegistry.addBasicCommand(new HelpCommand(this));
        cmdRegistry.addBasicCommand(new InfoCommand(this));
        cmdRegistry.addBasicCommand(new JoinCommand());
        cmdRegistry.addBasicCommand(new LeaveCommand());
        cmdRegistry.addBasicCommand(new PermissionCommand(this));
        cmdRegistry.addBasicCommand(new PingCommand());
        cmdRegistry.addBasicCommand(new PrefixCommand(this));
        cmdRegistry.addBasicCommand(new VersionCommand(this));
        cmdRegistry.addBasicCommand(new AntiPingCommand(this));

        // Developer Commands
        cmdRegistry.addBasicCommand(new KickBotCommand(this));
        cmdRegistry.addBasicCommand(new RestartCommand());
        cmdRegistry.addBasicCommand(new SpeakCommand(this));
        cmdRegistry.addBasicCommand(new TerminateCommand());
        cmdRegistry.addBasicCommand(new EchoCommand());
        cmdRegistry.addBasicCommand(new WhitelistBotCommand(this));

        // Music Commands
        cmdRegistry.addBasicCommand(new PlayCommand());
        cmdRegistry.addBasicCommand(new PauseCommand());
        cmdRegistry.addBasicCommand(new PlayingCommand());
        cmdRegistry.addBasicCommand(new QueueCommand());
        cmdRegistry.addBasicCommand(new StopCommand());
        cmdRegistry.addBasicCommand(new VolumeCommand());


        // Tricks
        cmdRegistry.addBasicCommand(new TrickCommand(this));


        // Mappings
        MappingsManager latest_mappings_manager = MappingsManager.new_(); // Soon we need to do for multiple versions but not ATM
        latest_mappings_manager.init(MAPPINGS_VERSION);
        cmdRegistry.addBasicCommand(new MappingsMainCommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new ClassMapCommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new DefMapCommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new VarMapCommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new MCPCommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new FCICommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new YCCommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new YMCommand(latest_mappings_manager,this));
        cmdRegistry.addBasicCommand(new YFCommand(latest_mappings_manager,this));

        
        // Test
        cmdRegistry.addBasicCommand(new RunCode());
        cmdRegistry.addBasicCommand(new GetEmbedsCommand());


        cmdRegistry.addBasicCommand(new PRScanCommand(this));
        cmdRegistry.addBasicCommand(new IssueScanCommand(this));

        GuildConfig.loadServerConfigs();
        permRegistry.save();
        PasteRequestModule.register(getPluginBus());
        new GHPRStatus(this);
        new GHIssueStatus(this);
        

        getPluginBus().addListener(10, PluginMessageEvent.class, pm -> {
            if (pm.getMethod().equals("getDate")) {
                if (pm.getObject().get() == null) return;
                if (pm.getObject().get() instanceof Date date) {
                    System.out.println("Received a Plugin Comms with Data: %s from %s".formatted(date, pm.getOrigin().getId()));
                }
            }
        });

        new Listeners(this);
        getPluginBus().addGenericListener(10, MessageReceivedEvent.class, DiscordEvent.class, this::onMessage);
    }

    @Override
    public void finished() {
        getPluginBus().post(new LoadEvent());
        getJDA()
                .updateCommands()
                .addCommands(Command.globalCommands)
                .queue();
    }

    @Override
    public void shutdownPre() {
        getPluginBus().post(new SaveEvent());
    }


    @Override
    public void shutdownPost() {

    }

    @Override
    public String getCommandPrefix() {
        return "mb?";
    }

    public static String getToken() {
		if (BOT_TOKEN.get().equals("empty") || BOT_TOKEN.get().equals("")) {
			System.out.println("Empty bot token, replace the bot token with the one from discord in" + CONFIG.getFile() + " or by typing it in here if you are not in gradle:");
			Scanner scanner = new Scanner(System.in);

			if (scanner.hasNext()) {
				String token = scanner.nextLine();
				BOT_TOKEN.set(token);
				scanner.close();
				return token;
			} else {
				System.out.println("Blank response, this is expected from being run within Gradle. You need to put your token here " + CONFIG.getFile());
				scanner.close();
				return BOT_TOKEN.get();
			}

		} else {
			return BOT_TOKEN.get();
		}
    }



}
