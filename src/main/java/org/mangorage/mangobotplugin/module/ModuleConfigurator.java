package org.mangorage.mangobotplugin.module;

import org.mangorage.bootstrap.api.module.IModuleConfigurator;
import org.mangorage.bootstrap.api.module.IModuleLayer;

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

    @Override
    public void configureModuleLayer(IModuleLayer moduleLayer) {
        moduleLayer.addReads(
                "org.jboss.logging",
                "java.logging"
        );
    }
}
