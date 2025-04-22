module org.mangorage.mangobotplugin {
    requires org.mangorage.mangobotcore;
    requires net.dv8tion.jda;
    requires com.google.gson;
    requires luaj.jse;

    requires net.minecraftforge.eventbus;

    opens org.mangorage.mangobotplugin to org.mangorage.mangobotcore;
}