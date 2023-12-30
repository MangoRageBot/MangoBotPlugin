package org.mangorage.mangobot.modules.github;

import java.util.ArrayList;
import java.util.Arrays;

import org.mangorage.mangobot.config.GuildConfig;
import org.mangorage.mangobot.core.BotPermissions;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.IBasicCommand;

import net.dv8tion.jda.api.entities.Message;

public class PRScanCommand implements IBasicCommand{

	@Override
	public CommandResult execute(Message message, Arguments args) {
		// TODO Auto-generated method stub
        String type = args.get(0);
        String answer = args.get(1);
        GuildConfig guildConfig = GuildConfig.guildsConfig(message.getGuildId());
        
        if (!BotPermissions.TRICK_ADMIN.hasPermission(message.getMember()))
            return CommandResult.NO_PERMISSION;
        
        if(type!=null) {
        	if(type.equals("-add")||type.equals("-追加")) {
        		String[] repos_arr =	guildConfig.GIT_REPOS_PR_SCANNED.get().split(",");
        		ArrayList<String> repos = new ArrayList<String>(Arrays.asList(repos_arr));
        		repos.add(answer);
                String result = String.join(",", repos);
                guildConfig.GIT_REPOS_PR_SCANNED.set(result);
        		message.reply("Added: " + answer);
        	}else if(type.equals("-remove")||type.equals("-取り除く")){
            	String[] repos_arr =	guildConfig.GIT_REPOS_PR_SCANNED.get().split(",");
        		ArrayList<String> repos = new ArrayList<String>(Arrays.asList(repos_arr));
        		repos.remove(answer);
                String result = String.join(",", repos);
                guildConfig.GIT_REPOS_PR_SCANNED.set(result);
        		message.reply("Removed: " + answer);
        	}else if(type.equals("-list")||type.equals("-リスト")){
        	String[] repos =	guildConfig.GIT_REPOS_PR_SCANNED.get().split(",");
        	StringBuilder builder = new StringBuilder();
        	for(String repo:repos) {
        		builder.append(repo+"\n");
        	}
        	message.reply(builder);	
        	}else if(type.equals("-setchannel")||type.equals("-チャンネルを設定する")){
        		GHPRStatus.indexed_channels.remove(guildConfig.GIT_REPOS_PR_SCANNED_CHANNELID.get());
        		guildConfig.GIT_REPOS_PR_SCANNED_CHANNELID.set(answer);
        		GHPRStatus.indexed_channels.add(answer);
        	}else {
        		message.reply("Invalid arg "+type);
        		return CommandResult.FAIL;
        	}
        	
        	
        }else {
        	message.reply("PR Scan Command Usage:"
        			+ "``!prscan -add Org/Repo`` Adds a Repository"
        			+ "``!prscan -remove Org/Repo`` Removes a Repository"
        			+ "``!prscan -list`` Lists Indexed Repository"
        			+ "``!prscan -setchannel`` Sets this as the current channel to list the pull requests"
        			);	
        }
		
		
		return CommandResult.PASS;
	}

	@Override
	public String commandId() {
		return "prscan";
	}

}
