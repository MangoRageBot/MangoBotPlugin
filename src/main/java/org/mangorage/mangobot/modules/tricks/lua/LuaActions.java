package org.mangorage.mangobot.modules.tricks.lua;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;

import java.util.ArrayList;
import java.util.List;

public final class LuaActions {
    private final CorePlugin corePlugin;
    private final List<ILuaAction> actions = new ArrayList<>();
    private final Message message;
    private final MessageChannel messageChannel;

    public LuaActions(CorePlugin plugin, Message message, MessageChannel channel) {
        this.corePlugin = plugin;
        this.message = message;
        this.messageChannel = channel;
    }

    public void reply(String s) {
        actions.add(new MessageAction(s, true, message));
    }

    public void finish() {
        actions.forEach(a -> a.submit(corePlugin.getMessageSettings()));
    }
}
