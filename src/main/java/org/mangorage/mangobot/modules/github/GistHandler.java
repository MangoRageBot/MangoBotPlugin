package org.mangorage.mangobot.modules.github;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GistHandler {
    public static Gist createGist(String content, String fileName) {
        GitHubClient CLIENT = PasteRequestModule.GITHUB_CLIENT.get();
        GistService service = new GistService(CLIENT);

        Gist gist = new Gist();
        gist.setPublic(false);
        gist.setDescription("Automatically made from MangoBot.");


        Map<String, GistFile> fileMap = new HashMap<>();
        GistFile file = new GistFile();
        file.setContent(content);
        file.setFilename(fileName);
        fileMap.put(fileName, file);

        gist.setFiles(fileMap);

        try {
            return service.createGist(gist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
