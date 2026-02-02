package org.mangorage.mangobotplugin.commands.internal.homedepot;

import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.PermissionNode;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;
import org.mangorage.mangobotcore.api.jda.command.v2.JDAPermissionNode;

public final class HomeDepotCommand extends AbstractJDACommand {
    private static final JDAPermissionNode DEV_PERMISSION_NODE = JDAPermissionNode.create("homedepot.dev");

    static {
       DEV_PERMISSION_NODE.authorizeUser(null, 194596094200643584L);
    }

    public HomeDepotCommand(String name) {
        super(name, "Home Depot Command");
        addSubCommand(
                new HomeDepotScanQRSubCommand()
        );
        addSubCommand(
                new HomeDepotSendAlertSubCommand()
        );
    }

    @Override
    public PermissionNode<Message> getPermissionNode() {
        return DEV_PERMISSION_NODE;
    }

    @Override
    public JDACommandResult run(CommandContext<Message> commandContext) throws Throwable {
        return JDACommandResult.PASS;
    }
}
