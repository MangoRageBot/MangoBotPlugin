package org.mangorage.mangobot.website.servlet;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mangorage.mangobot.website.servlet.file.TargetFile;
import org.mangorage.mangobot.website.servlet.file.UploadConfig;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileServlet extends HttpServlet {

    private static final String UPLOADS_DATA = "webpage-root/uploads/data/";
    private static final String UPLOADS_CONFIGS = "webpage-root/uploads/cfg/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    private static final Map<String, String> EXTENSIONS = new HashMap<>();

    static {
        // Images - Displayed directly in the browser.
        EXTENSIONS.put(".jpg", "image/jpeg");
        EXTENSIONS.put(".jpeg", "image/jpeg");
        EXTENSIONS.put(".png", "image/png");
        EXTENSIONS.put(".gif", "image/gif");
        EXTENSIONS.put(".bmp", "image/bmp");
        EXTENSIONS.put(".webp", "image/webp");
        EXTENSIONS.put(".ico", "image/x-icon");
        EXTENSIONS.put(".svg", "image/svg+xml");
        EXTENSIONS.put(".tif", "image/tiff");
        EXTENSIONS.put(".tiff", "image/tiff");

        // Documents - Displayed directly in the browser.
        EXTENSIONS.put(".pdf", "application/pdf");
        EXTENSIONS.put(".txt", "text/plain");
        EXTENSIONS.put(".csv", "text/csv");
        EXTENSIONS.put(".json", "application/json");
        EXTENSIONS.put(".xml", "application/xml");
//        EXTENSIONS.put(".html", "text/html");
//        EXTENSIONS.put(".htm", "text/html");
//        EXTENSIONS.put(".css", "text/css");
//        EXTENSIONS.put(".js", "application/javascript");

        // Audio - Played directly in the browser.
        EXTENSIONS.put(".mp3", "audio/mpeg");
        EXTENSIONS.put(".wav", "audio/wav");
        EXTENSIONS.put(".ogg", "audio/ogg");
        EXTENSIONS.put(".flac", "audio/flac");
        EXTENSIONS.put(".aac", "audio/aac");

        // Video - Played directly in the browser.
        EXTENSIONS.put(".mp4", "video/mp4");
        EXTENSIONS.put(".avi", "video/x-msvideo");
        EXTENSIONS.put(".mov", "video/quicktime");
        EXTENSIONS.put(".wmv", "video/x-ms-wmv");
        EXTENSIONS.put(".flv", "video/x-flv");
        EXTENSIONS.put(".webm", "video/webm");

        // Fonts - Used by web browsers to render content.
        EXTENSIONS.put(".ttf", "font/ttf");
        EXTENSIONS.put(".otf", "font/otf");
        EXTENSIONS.put(".woff", "font/woff");
        EXTENSIONS.put(".woff2", "font/woff2");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the 'id' parameter from the query string
        // -> /file?id=UUID&target=x&dl=1

        String id = request.getParameter("id");
        String target = request.getParameter("target"); // OPTIONAL
        String download = request.getParameter("dl"); // OPTIONAL


        if (id != null && !id.isBlank()) {
            UploadConfig config = fetchConfig(id);
            if (config == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid File ID");
                return;
            }
            if (target == null) {
                var flow = HtmlFlow
                        .doc(response.getWriter())
                        .html()
                        .body()
                        .input()
                        .attrType(EnumTypeInputType.TEXT)
                        .attrValue("https://mangobot.mangorage.org/file?id="+id)
                        .attrReadonly(true) // Make it readonly so users can only copy
                        .attrId("copyInput")
                        .__()
                        .button()
                        .attrOnclick("document.getElementById('copyInput').select(); document.execCommand('copy');")
                        .text("Click to copy to clipboard to share!")
                        .__();

                config.targets().forEach((k, targetFile) -> {
                    flow
                            .h4()
                            .a()
                            .attrHref("/file?id=%s&target=%s".formatted(id, k))
                            .text(targetFile.name())
                            .__()
                            .a()
                            .attrHref("/file?id=%s&target=%s&dl=1".formatted(id, k))
                            .text("Download")
                            .__();
                });

            } else if (download == null) {
                var targetFile = config.targets().get(target);
                if (targetFile != null) {
                    handleFileRequest(targetFile, false, response);
                    return;
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Target");
                }
            } else {
                var targetFile = config.targets().get(target);
                if (targetFile != null) {
                    handleFileRequest(targetFile, true, response);
                    return;
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Target");
                }
                return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required.");
        }
    }

    private UploadConfig fetchConfig(String id) {
        Path file = Paths.get(UPLOADS_CONFIGS).resolve(id);
        if (Files.exists(file)) {
            try {
                return GSON.fromJson(new FileReader(file.toFile()), UploadConfig.class);
            } catch (FileNotFoundException ignored) {
                return null;
            }
        }
        return null;
    }

    private void handleFileRequest(TargetFile targetFile, boolean download, HttpServletResponse response) throws IOException {
        File file = new File(UPLOADS_DATA, targetFile.path());

        // Serve the file content
        if (file.exists() && file.isFile()) {
            String contentType = download ? "application/octet-stream" : EXTENSIONS.getOrDefault(targetFile.extension(), "text/plain");
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());

            if (download)
                response.setHeader("Content-Disposition", STR."attachment; filename=\"\{targetFile.name()}\"");

            try (InputStream fileInputStream = new FileInputStream(file)) {
                fileInputStream.transferTo(response.getOutputStream());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
    }
}