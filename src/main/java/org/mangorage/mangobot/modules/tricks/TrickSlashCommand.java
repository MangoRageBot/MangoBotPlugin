package org.mangorage.mangobot.modules.tricks;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.ISlashCommand;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;

public class TrickSlashCommand implements ISlashCommand {

    private final TrickCommand trickCommand;
    private final CorePlugin plugin;

    public TrickSlashCommand(TrickCommand command, CorePlugin plugin) {
        this.trickCommand = command;
        this.plugin = plugin;
    }
    @NotNull
    @Override
    public CommandResult execute(SlashCommandInteraction interaction, Arguments arguments) {
        if (interaction.getGuild() == null) return CommandResult.PASS;

        var id = interaction.getOption("id");
        if (id != null) {
            var trick = id.getAsString();
            var guildID = interaction.getGuild().getId();
            var channel = interaction.getChannel();
            var tricks = trickCommand.CONTENT.get(guildID);
            if (tricks != null) {
                var trickData = tricks.get(trick);
                if (trickData == null) return CommandResult.PASS;
                trickCommand.executeTrick(trickData, channel, arguments);
                interaction.deferReply().queue();
            }
        }

        return CommandResult.PASS;
    }

    @Override
    public String commandId() {
        return "trick";
    }

    @Override
    public String description() {
        return "A simple command for tricks";
    }

    @Override
    public void registerSubCommands(SlashCommandData command) {
        command.addOption(OptionType.STRING, "id", "the id of trick", false, true);
    }


    @Override
    public void onAutoCompleteEvent(CommandAutoCompleteInteraction commandAutoCompleteInteraction) {
        if (commandAutoCompleteInteraction.getFocusedOption().getName().equals("id")) {
            var guild = commandAutoCompleteInteraction.getGuild();
            if (guild != null) {
                var tricks = trickCommand.CONTENT.get(guild.getId());
                if (tricks != null) {
                    commandAutoCompleteInteraction.replyChoiceStrings(tricks.keySet()).queue();
                }
            }
        }
    }
}
