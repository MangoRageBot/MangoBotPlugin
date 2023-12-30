package org.mangorage.mangobot.modules.tricks;

import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.ISlashCommand;

public class TrickSlashCommand implements ISlashCommand {

    private final TrickCommand trickCommand;

    public TrickSlashCommand(TrickCommand command) {
        this.trickCommand = command;
    }
    @NotNull
    @Override
    public CommandResult execute(SlashCommandInteraction interaction, Arguments arguments) {
        if (interaction.getGuild() == null) return CommandResult.PASS;

        var id = interaction.getOption("id");
        if (id != null) {
            var trick = id.getAsString();
            var guildID = interaction.getGuild().getId();
            var tricks = trickCommand.CONTENT.get(guildID);
            if (tricks != null) {
                var trickData = tricks.get(trick);
                if (trickData == null) return CommandResult.PASS;
                trickCommand.executeTrick(trickData, interaction, arguments);
                return CommandResult.PASS;
            }
        }

        interaction.reply("Failed to execute trick!").queue();
        return CommandResult.FAIL;
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
    public void onAutoCompleteEvent(CommandAutoCompleteInteraction interaction) {
        if (interaction.getFocusedOption().getName().equals("id")) {
            var guild = interaction.getGuild();
            if (guild != null) {
                var tricks = trickCommand.CONTENT.get(guild.getId());
                if (tricks != null) {
                    var options = tricks.keySet().stream()
                            .filter(word -> word.startsWith(interaction.getFocusedOption().getValue())) // only display words that start with the user's current input
                            .limit(25)
                            .toList();
                    interaction.replyChoiceStrings(options).queue();
                }
            }
        }
    }
}
