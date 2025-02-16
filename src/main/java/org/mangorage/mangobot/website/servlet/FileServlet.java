package org.mangorage.mangobot.website.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import htmlflow.HtmlFlow;
import htmlflow.HtmlView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mangorage.mangobot.website.impl.StandardHttpServlet;
import org.mangorage.mangobot.website.util.ResolveString;
import org.mangorage.mangobot.website.WebServer;
import org.mangorage.mangobot.website.servlet.file.TargetFile;
import org.mangorage.mangobot.website.servlet.file.UploadConfig;
import org.xmlet.htmlapifaster.EnumRelType;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileServlet extends StandardHttpServlet {

    private static final ResolveString UPLOADS_DATA = WebServer.WEBPAGE_ROOT.resolve("uploads").resolve("data");
    private static final ResolveString UPLOADS_CONFIGS = WebServer.WEBPAGE_ROOT.resolve("uploads").resolve("cfg");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, String> EXTENSIONS = new HashMap<>();

    static {
        // Images
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

        // Documents
        EXTENSIONS.put(".pdf", "application/pdf");
        EXTENSIONS.put(".txt", "text/plain");
        EXTENSIONS.put(".csv", "text/csv");
        EXTENSIONS.put(".json", "application/json");
        EXTENSIONS.put(".xml", "application/xml");

        // Audio
        EXTENSIONS.put(".mp3", "audio/mpeg");
        EXTENSIONS.put(".wav", "audio/wav");
        EXTENSIONS.put(".ogg", "audio/ogg");
        EXTENSIONS.put(".flac", "audio/flac");
        EXTENSIONS.put(".aac", "audio/aac");

        // Video
        EXTENSIONS.put(".mp4", "video/mp4");
        EXTENSIONS.put(".avi", "video/x-msvideo");
        EXTENSIONS.put(".mov", "video/quicktime");
        EXTENSIONS.put(".wmv", "video/x-ms-wmv");
        EXTENSIONS.put(".flv", "video/x-flv");
        EXTENSIONS.put(".webm", "video/webm");

        // Fonts
        EXTENSIONS.put(".ttf", "font/ttf");
        EXTENSIONS.put(".otf", "font/otf");
        EXTENSIONS.put(".woff", "font/woff");
        EXTENSIONS.put(".woff2", "font/woff2");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve parameters
        String id = request.getParameter("id");
        String target = request.getParameter("target"); // optional
        String download = request.getParameter("dl");     // optional
        String delete = request.getParameter("delete");     // optional

        if (id == null || id.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required.");
            return;
        }

        UploadConfig config = fetchConfig(id);
        if (config == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid File ID");
            return;
        }

        boolean isOwner = config.isAccount(request, response);

        // Insufficient permission to delete
        if (!isOwner && delete != null) {
            HtmlFlow.doc(response.getWriter())
                    .html()
                    .head()
                    .meta().addAttr("http-equiv", "refresh").addAttr("content", "5;url=/file?id=" + id).__()
                    .link().attrRel(EnumRelType.STYLESHEET).attrHref(getStyles()).__()
                    .__()
                    .body()
                    .h1().text("Insufficient Permission").__()
                    .h3().text("Redirecting you back in 5 seconds...").__()
                    .__()
                    .__();
            return;
        }

        // Delete action
        if (isOwner && delete != null) {
            if (target != null) {
                TargetFile targetFile = config.targets().get(target);
                if (targetFile != null) {
                    config.targets().remove(target);
                    Path uploadCfgPath = Paths.get(UPLOADS_CONFIGS.value());
                    Path dataPath = Paths.get(UPLOADS_DATA.value());
                    targetFile.delete(dataPath);
                    if (!Files.exists(uploadCfgPath)) {
                        Files.createDirectories(uploadCfgPath);
                    }
                    Files.write(uploadCfgPath.resolve(id), GSON.toJson(config).getBytes());
                } else {
                    HtmlFlow.doc(response.getWriter())
                            .html()
                            .head()
                            .link().attrRel(EnumRelType.STYLESHEET).attrHref(getStyles()).__()
                            .__()
                            .body()
                            .h1().text("Invalid Target").__()
                            .__()
                            .__();
                    return;
                }
            } else {
                Path uploadCfgPath = Paths.get(UPLOADS_CONFIGS.value());
                Path dataPath = Paths.get(UPLOADS_DATA.value());
                config.delete(uploadCfgPath, dataPath);
            }

            HtmlFlow.doc(response.getWriter())
                    .html()
                    .head()
                    .meta().addAttr("http-equiv", "refresh").addAttr("content", "5;url=/file?id=" + id).__()
                    .link().attrRel(EnumRelType.STYLESHEET).attrHref(getStyles()).__()
                    .__()
                    .body()
                    .h1().text("Deleted").__()
                    .h3().text("Redirecting you back in 5 seconds...").__()
                    .__()
                    .__();
            return;
        }

        // Display file management page if no specific target is provided
        if (target == null) {
            var view = HtmlFlow.doc(response.getWriter())
                    .html()
                    .head()
                    .title().text("File Management").__()
                    .link().attrRel(EnumRelType.STYLESHEET).attrHref(getStyles()).__()
                    .__()
                    .body()
                    .div().attrClass("container")
                    .h1().attrClass("title").text("File Management").__()
                    .div().attrClass("url-section")
                    .input().attrType(EnumTypeInputType.TEXT)
                    .attrValue("https://mangobot.mangorage.org/file?id=" + id)
                    .attrReadonly(true)
                    .attrId("copyInput")
                    .__()
                    .button().attrClass("copy-button")
                    .attrOnclick("document.getElementById('copyInput').select(); document.execCommand('copy');")
                    .text("Click to copy URL to clipboard")
                    .__().__();

            // Show delete option for owner
            if (isOwner) {
                view = view.div().attrClass("owner-actions")
                        .a().attrHref("/file?id=" + id + "&delete=1").text("Delete File").__().__();
            }

            // Iterate through target files and display their options
            org.xmlet.htmlapifaster.Div<org.xmlet.htmlapifaster.Body<org.xmlet.htmlapifaster.Html<htmlflow.HtmlPage>>> finalView = view;
            config.targets().forEach((k, targetFile) -> {
                finalView.div().attrClass("target-section")
                        .h4().a().attrHref("/file?id=" + id + "&target=" + targetFile.index()).text(targetFile.name()).__().__()
                        .a().attrHref("/file?id=" + id + "&target=" + targetFile.index() + "&dl=1").text("Download").__();
                if (isOwner) {
                    finalView.a().attrHref("/file?id=" + id + "&target=" + targetFile.index() + "&delete=1").text("Delete").__();
                }
                finalView.__();
            });

            view.__().__();
            return;
        } else if (download == null) {
            // Display file inline
            TargetFile targetFile = config.targets().get(target);
            if (targetFile != null) {
                handleFileRequest(targetFile, false, response);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Target");
            }
        } else {
            // Download file
            TargetFile targetFile = config.targets().get(target);
            if (targetFile != null) {
                handleFileRequest(targetFile, true, response);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Target");
                return;
            }
        }
    }

    private UploadConfig fetchConfig(String id) {
        Path file = Paths.get(UPLOADS_CONFIGS.value()).resolve(id);
        if (Files.exists(file)) {
            try (FileReader reader = new FileReader(file.toFile())) {
                return GSON.fromJson(reader, UploadConfig.class);
            } catch (IOException ignored) {
                return null;
            }
        }
        return null;
    }

    private void handleFileRequest(TargetFile targetFile, boolean download, HttpServletResponse response) throws IOException {
        File file = new File(UPLOADS_DATA.value(), targetFile.path());

        if (file.exists() && file.isFile()) {
            String contentType = download ? "application/octet-stream"
                    : EXTENSIONS.getOrDefault(targetFile.extension(), "text/plain");
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());
            if (download) {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + targetFile.name() + "\"");
            }
            try (InputStream fileInputStream = new FileInputStream(file)) {
                fileInputStream.transferTo(response.getOutputStream());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
    }

    @Override
    public boolean useDefaultStyles() {
        return false;
    }
}
