module org.mangorage.mangobotplugin {
    requires java.naming;
    requires okio;
    requires kotlin.stdlib;

    requires org.slf4j.simple;

    requires org.mangorage.bootstrap;
    requires org.mangorage.mangobotcore;

    requires net.dv8tion.jda;
    requires com.google.gson;

    requires net.minecraftforge.eventbus;


    requires java.desktop;
    requires luaj.jme;
    requires org.apache.logging.log4j;
    requires jakarta.persistence;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.slf4j;
    requires protocol.jvm;
    requires lavalink.client;
    requires reactor.core;

    opens org.mangorage.mangobotplugin;

    exports org.mangorage.mangobotplugin.entrypoint;
    exports org.mangorage.mangobotplugin.commands.trick;

    exports  org.mangorage.mangobotplugin.commands.music;

    opens org.mangorage.mangobotplugin.pagedlist to net.dv8tion.jda;
    opens org.mangorage.mangobotplugin.commands.trick to com.google.gson, org.hibernate.orm.core;


    provides org.mangorage.mangobotcore.api.plugin.v1.Plugin with org.mangorage.mangobotplugin.entrypoint.MangoBot;
    provides org.mangorage.bootstrap.api.module.IModuleConfigurator with org.mangorage.mangobotplugin.module.ModuleConfigurator;

    uses org.mangorage.mangobotcore.api.plugin.v1.Plugin;
    uses org.mangorage.bootstrap.api.module.IModuleConfigurator;
}