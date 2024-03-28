package org.mangorage.mangobot.modules.tricks.lua;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.mangorage.mangobot.MangoBotPlugin;
import org.mangorage.mangobotapi.core.util.MessageSettings;

public final class MessageAction implements ILuaAction {
    private final String string;
    private final boolean isReply;
    private final Object instance;

    public MessageAction(String s, boolean reply, Object instance) {
        this.string = s;
        this.isReply = reply;
        this.instance = instance;
    }

    @Override
    public void submit(MessageSettings settings) {
        if (isReply && instance instanceof Message message) {
            settings.apply(message.reply(string)).queue();
        } else if (!isReply && instance instanceof MessageChannel channel) {
            settings.apply(channel.sendMessage(string)).queue();
        }
    }
}
