package org.mangorage.mangobotplugin.module;

import org.mangorage.bootstrap.api.module.IModuleConfigurator;

import java.util.List;

public final class ModuleConfigurator implements IModuleConfigurator {

    @Override
    public List<String> getChildren(String s) {
        return switch (s) {
            case "lavaplayer" -> List.of("lavaplayer.natives");
            case "opus.java", "opus.java.api" -> List.of("opus.java.natives");
            default -> List.of();
        };
    }
}
