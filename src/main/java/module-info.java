module org.mangorage.mangobotplugin {
    requires org.mangorage.mangobotcore;

    requires net.dv8tion.jda;
    requires com.google.gson;
    requires jdk.unsupported;

    requires net.minecraftforge.eventbus;
    requires org.jetbrains.annotations;

    requires static lavaplayer;
    requires static common;
    requires java.desktop;


    exports org.mangorage.mangobotplugin.entrypoint;
    exports org.mangorage.mangobotplugin.commands.trick;

    opens org.mangorage.mangobotplugin.commands.trick to com.google.gson;
    opens org.mangorage.mangobotplugin.commands.trick.lua to com.google.gson;

    provides org.mangorage.mangobotcore.plugin.api.Plugin with org.mangorage.mangobotplugin.entrypoint.MangoBot;
    uses org.mangorage.mangobotcore.plugin.api.Plugin;
}