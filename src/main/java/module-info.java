module org.mangorage.mangobotplugin {
    requires org.mangorage.mangobotcore;
    requires net.dv8tion.jda;
    requires com.google.gson;
    requires luaj.jse;

    requires net.minecraftforge.eventbus;

    exports org.mangorage.mangobotplugin.entrypoint;
    exports org.mangorage.mangobotplugin.commands.trick;
}