package org.mangorage.mangobot.website.servlet.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record TargetFile
        (
                String index,
                String name,
                String path,
                String extension
        )
{
        public void delete(Path dataPath) {
            try {
                Files.deleteIfExists(
                        dataPath.resolve(path)
                );
            } catch (IOException ignored) {}
        }
}
