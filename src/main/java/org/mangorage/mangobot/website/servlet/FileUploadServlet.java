package org.mangorage.mangobot.website.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import htmlflow.HtmlFlow;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.mangorage.mangobot.website.ResolveString;
import org.mangorage.mangobot.website.WebServer;
import org.mangorage.mangobot.website.WebUtil;
import org.mangorage.mangobot.website.impl.AbstractServlet;
import org.mangorage.mangobot.website.servlet.file.TargetFile;
import org.mangorage.mangobot.website.servlet.file.UploadConfig;
import org.xmlet.htmlapifaster.EnumEnctypeType;
import org.xmlet.htmlapifaster.EnumMethodType;
import org.xmlet.htmlapifaster.EnumRelType;
import org.xmlet.htmlapifaster.EnumTypeInputType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;

@MultipartConfig
public class FileUploadServlet extends AbstractServlet {

    private static final ResolveString UPLOADS_DATA = WebServer.WEBPAGE_ROOT.resolve("uploads").resolve("data");
    private static final ResolveString UPLOADS_CONFIGS =  WebServer.WEBPAGE_ROOT.resolve("uploads").resolve("cfg");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) res;

        // Handle GET request
        if ("GET".equals(httpReq.getMethod())) {
            HtmlFlow
                    .doc(httpResp.getWriter())
                    .html()
                    .head()
                    .title().text("File Upload Page").__()
                    .link().attrRel(EnumRelType.STYLESHEET).attrHref("/css/dragDropUpload.css").__() // Use external CSS if desired
                    .__() // Close head
                    .body()
                    .h1().text("Upload a File").__() // Add page heading
                    .form().attrMethod(EnumMethodType.POST).attrAction("/upload").attrEnctype(EnumEnctypeType.MULTIPART_FORM_DATA)
                    .div().attrId("drop-area")
                    .p().text("Drag and drop a file here or click to select").__() // Instructions text
                    .input().attrType(EnumTypeInputType.FILE).attrId("file-input").attrName("file").__() // Hidden file input
                    .__() // Close div
                    .br().__()
                    .input().attrType(EnumTypeInputType.SUBMIT).attrValue("Upload").__() // Submit button
                    .__() // Close form
                    // .hr().__()   <-- Remove or comment out this line
                    .script().attrSrc("/js/dragDropUpload.js").__() // Link to JS script
                    .__(); // Close body and document

        }
        // Handle POST request
        else if ("POST".equals(httpReq.getMethod())) {

            String uploadId = STR."\{UUID.randomUUID()}";
            HashMap<String, TargetFile> targets = new HashMap<>();
            Integer index = 0;

            for (Part filePart : httpReq.getParts()) {
                String fileId = STR."\{UUID.randomUUID()}";
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                String fileExtension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";

                targets.put(
                        index.toString(),
                        new TargetFile(
                                index.toString(),
                                fileName,
                                fileId,
                                fileExtension
                        )
                );

                index++;
                // Define the upload directory
                Path filePath = Paths.get(UPLOADS_DATA.value());
                if (!Files.exists(filePath)) {
                    Files.createDirectories(filePath);
                }

                // Save the file to the upload directory
                try (InputStream input = filePart.getInputStream()) {
                    Files.copy(input, filePath.resolve(fileId), StandardCopyOption.REPLACE_EXISTING);
                }

            }

            Path uploadCfgPath = Paths.get(UPLOADS_CONFIGS.value());
            if (!Files.exists(uploadCfgPath)) {
                Files.createDirectories(uploadCfgPath);
            }

            Files.write(
                    uploadCfgPath.resolve(uploadId),
                    GSON.toJson(new UploadConfig(uploadId, WebUtil.getOrCreateUserToken(httpReq, httpResp), targets)).getBytes()
            );

            httpResp.sendRedirect("/file?id=" + uploadId);
        }
    }

}