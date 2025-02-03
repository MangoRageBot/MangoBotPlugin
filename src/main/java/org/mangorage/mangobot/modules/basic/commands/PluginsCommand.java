package org.mangorage.mangobot.modules.basic.commands;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.IBasicCommand;
import org.mangorage.mangobotapi.core.plugin.PluginManager;

public class PluginsCommand implements IBasicCommand {
    @Override
    public @NotNull CommandResult execute(Message message, Arguments arguments) {
        StringBuilder builder = new StringBuilder();

        builder.append("List of Plugins:").append("\n");

        PluginManager.getPluginContainers().forEach(container -> {
            builder.append("Plugin '%s' with version '%s'".formatted(container.getId(), container.getMetadata().version())).append("\n");
        });

        message.reply(builder).mentionRepliedUser(false).queue();


        return CommandResult.PASS;
    }

    @Override
    public String commandId() {
        return "plugins";
    }
}
