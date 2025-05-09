module org.mangorage.mangobotplugin {
    requires okio;
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j.simple;

    requires org.mangorage.mangobotcore;

    requires net.dv8tion.jda;
    requires com.google.gson;

    requires net.minecraftforge.eventbus;
    requires org.jetbrains.annotations;

    requires lavaplayer;
    requires common;

    requires java.desktop;
    requires luaj.jme;
    requires org.apache.logging.log4j;
    requires org.mangorage.bootstrap;


    exports org.mangorage.mangobotplugin.entrypoint;
    exports org.mangorage.mangobotplugin.commands.trick;

    exports org.mangorage.mangobotplugin to net.dv8tion.jda;

    opens org.mangorage.mangobotplugin.commands.trick to com.google.gson;
    opens org.mangorage.mangobotplugin.commands.trick.lua to com.google.gson;

    exports org.mangorage.mangobotplugin.commands.music;
    opens org.mangorage.mangobotplugin.commands.music;
    exports org.mangorage.mangobotplugin.pagedlist to net.dv8tion.jda;

    provides org.mangorage.mangobotcore.plugin.api.Plugin with org.mangorage.mangobotplugin.entrypoint.MangoBot;
    //provides org.mangorage.bootstrap.api.module.IModuleConfigurator with org.mangorage.mangobotplugin.module.ModuleConfigurator;

    uses org.mangorage.mangobotcore.plugin.api.Plugin;
}