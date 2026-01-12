package org.mangorage.mangobotplugin.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.mangorage.mangobotcore.api.jda.command.v1.CommandResult;
import org.mangorage.mangobotcore.api.jda.command.v1.ICommand;
import org.mangorage.mangobotcore.api.util.misc.Arguments;

import java.awt.*;
import java.util.List;

public final class PingsCommand implements ICommand {
    public static final MessageEmbed EMBED =
            new EmbedBuilder()
                    .setTitle("Please disable pings when replying to others")
                    .setImage("https://images-ext-2.discordapp.net/external/nUYuix4co3xyLw0ZFtBh5r2uxogGjmgr-4OTPW4cl8I/https/i.imgur.com/7YCb3AN.png")
                    .setColor(Color.WHITE)
                    .setDescription("""
                                    So you don't accidentally break a rule or annoy others, make sure to turn off pings when replying to others. Discord currently turns this on every time you start a reply, so please be vigilant. Thank you.
                                                                    
                                    Go vote on [Discord Feedback](https://support.discord.com/hc/en-us/community/posts/360052518273-Replies-remember-setting) so they make changes.
                                    """
                    ).build();


    @Override
    public String id() {
        return "pings";
    }

    @Override
    public List<String> commands() {
        return List.of("pings");
    }

    @Override
    public String usage() {
        return "Pings Usage: N/A";
    }

    @Override
    public CommandResult execute(Message message, Arguments arguments) {
        var referenced = message.getReferencedMessage();
        if (referenced == null) {
            message.getChannel().sendMessageEmbeds(EMBED).queue();
        } else {
            referenced.replyEmbeds(EMBED).queue();
        }
        return CommandResult.PASS;
    }
}
