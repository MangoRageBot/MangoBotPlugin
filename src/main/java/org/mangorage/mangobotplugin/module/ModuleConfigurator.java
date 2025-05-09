package org.mangorage.mangobotplugin.module;

import org.mangorage.bootstrap.api.module.IModuleConfigurator;

import java.util.List;

public final class ModuleConfigurator implements IModuleConfigurator {

    @Override
    public List<String> getChildren(String s) {
        System.out.println("Found This -> " + s);
        if (s.equals("lavaplayer")) {
            return List.of("lavaplayer.natives");
        }
        return List.of();
    }
}
