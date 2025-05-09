package org.mangorage.mangobotplugin.module;



import java.util.List;

public final class ModuleConfigurator  {


    public List<String> getChildren(String s) {
        System.out.println("Found This -> " + s);
        if (s.equals("lavaplayer")) {
            return List.of("lavaplayer.natives");
        }
        return List.of();
    }
}
