import org.mangorage.bootstrap.api.module.IModuleConfigurator;
import org.mangorage.mangobotcore.api.plugin.v1.Plugin;
import org.mangorage.mangobotplugin.module.ModuleConfigurator;

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
    requires com.google.zxing;
    requires com.google.zxing.javase;


    exports org.mangorage.mangobotplugin.entrypoint;
    exports org.mangorage.mangobotplugin.commands.trick;

    exports org.mangorage.mangobotplugin to net.dv8tion.jda;

    exports org.mangorage.mangobotplugin.commands.music;
    exports org.mangorage.mangobotplugin.pagedlist to net.dv8tion.jda;

    opens org.mangorage.mangobotplugin.commands.trick to com.google.gson;
    opens org.mangorage.mangobotplugin.commands.trick.lua to com.google.gson;
    opens org.mangorage.mangobotplugin.commands.music;


    provides Plugin with org.mangorage.mangobotplugin.entrypoint.MangoBot;
    provides IModuleConfigurator with ModuleConfigurator;

    uses Plugin;
    uses IModuleConfigurator;
}