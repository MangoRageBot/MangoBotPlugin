package org.mangorage.mangobot.website.servlet.file;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mangorage.mangobot.website.WebUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public record UploadConfig
        (
                String id,
                String account_id,
                HashMap<String, TargetFile> targets
        )
{

        public void delete(Path cfgPath, Path dataPath) {
            try {
                Files.deleteIfExists(
                        cfgPath.resolve(id)
                );
                targets.forEach((k, t) -> t.delete(dataPath));
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }

        public boolean isAccount(HttpServletRequest request, HttpServletResponse response) {
                var id = WebUtil.getOrCreateUserToken(request, response);
                return id.equals(account_id);
        }
}
