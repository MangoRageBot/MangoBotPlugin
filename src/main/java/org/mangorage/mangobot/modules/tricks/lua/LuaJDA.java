package org.mangorage.mangobot.modules.tricks.lua;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.mangorage.mangobot.modules.tricks.Trick;
import org.mangorage.mangobot.modules.tricks.TrickCommand;
import org.mangorage.mangobot.modules.tricks.lua.builders.LuaEmbedBuilder;
import org.mangorage.mangobot.modules.tricks.lua.builders.LuaMessageResponseBuilder;
import org.mangorage.mangobot.modules.tricks.lua.helpers.LuaInfoHelper;
import org.mangorage.mangobot.modules.tricks.lua.helpers.LuaObjectHelper;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;


public final class LuaJDA {
    private final CorePlugin corePlugin;
    private final Message message;
    private final MessageChannel messageChannel;
    private final Trick trick;


    public LuaJDA(CorePlugin plugin, Trick trick, Message message, MessageChannel channel) {
        this.corePlugin = plugin;
        this.message = message;
        this.messageChannel = channel;
        this.trick = trick;
    }

    public Object getStored(String key) {
        return trick.getMemoryBank().bank().get(key);
    }

    public Object getStoredOrSetAndGet(String key, Object value) {
        var result = getStored(key);
        if (result == null) {
            storeValue(key, value);
            return value;
        }
        return result;
    }

    public void storeValue(String key, Object o) {
        trick.getMemoryBank().bank().put(key, o);
        TrickCommand.TRICK_DATA_HANDLER.save(corePlugin.getPluginDirectory(), trick);
    }

    public LuaEmbedBuilder createEmbed() {
        return new LuaEmbedBuilder();
    }

    public LuaMessageResponseBuilder respond() {
        return new LuaMessageResponseBuilder(message.reply(""));
    }

    public LuaInfoHelper getInfoHelper() {
        return new LuaInfoHelper(message, messageChannel);
    }

    public LuaObjectHelper getObjectHelper() {
        return new LuaObjectHelper();
    }
}
