package org.mangorage.mangobotplugin.commands.internal.homedepot;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;

public final class HomeDepotCommand extends AbstractJDACommand {
    public HomeDepotCommand(String name) {
        super(name);
        addSubCommand(
                new HomeDepotScanQRSubCommand()
        );
        addSubCommand(
                new HomeDepotSendAlertSubCommand()
        );
    }

    @Override
    public JDACommandResult run(Message message, String[] strings, CommandParseResult commandParseResult) throws Throwable {

        return JDACommandResult.PASS;
    }
}
