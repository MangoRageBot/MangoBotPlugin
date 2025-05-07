module org.mangorage.mangobotplugin {
    requires org.mangorage.mangobotcore;

    requires net.dv8tion.jda;
    requires com.google.gson;
    requires jdk.unsupported;

    requires net.minecraftforge.eventbus;
    requires org.jetbrains.annotations;

    requires lavaplayer;
    requires common;

    requires java.desktop;
    requires luaj.jme;
    requires org.apache.logging.log4j;


    exports org.mangorage.mangobotplugin.entrypoint;
    exports org.mangorage.mangobotplugin.commands.trick;

    exports org.mangorage.mangobotplugin to net.dv8tion.jda;

    opens org.mangorage.mangobotplugin.commands.trick to com.google.gson;
    opens org.mangorage.mangobotplugin.commands.trick.lua to com.google.gson;

    exports org.mangorage.mangobotplugin.commands.music;
    opens org.mangorage.mangobotplugin.commands.music;

    provides org.mangorage.mangobotcore.plugin.api.Plugin with org.mangorage.mangobotplugin.entrypoint.MangoBot;
    uses org.mangorage.mangobotcore.plugin.api.Plugin;
}