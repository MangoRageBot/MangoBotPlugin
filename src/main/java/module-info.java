import org.mangorage.bootstrap.api.module.IModuleConfigurator;
import org.mangorage.mangobotcore.api.plugin.v1.Plugin;
import org.mangorage.mangobotplugin.module.ModuleConfigurator;

module org.mangorage.mangobotplugin {
    requires java.naming;
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
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens org.mangorage.mangobotplugin;

    exports org.mangorage.mangobotplugin.entrypoint;
    exports org.mangorage.mangobotplugin.commands.trick;

    opens org.mangorage.mangobotplugin.pagedlist to net.dv8tion.jda;
    opens org.mangorage.mangobotplugin.commands.trick to com.google.gson, org.hibernate.orm.core;


    provides Plugin with org.mangorage.mangobotplugin.entrypoint.MangoBot;
    provides IModuleConfigurator with ModuleConfigurator;

    uses Plugin;
    uses IModuleConfigurator;
}