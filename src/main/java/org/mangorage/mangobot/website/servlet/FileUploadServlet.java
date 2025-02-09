package org.mangorage.mangobot.website.servlet;

import htmlflow.HtmlFlow;
import htmlflow.HtmlPage;
import htmlflow.HtmlTemplate;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.mangorage.mangobot.website.impl.AbstractServlet;
import org.xmlet.htmlapifaster.EnumEnctypeType;
import org.xmlet.htmlapifaster.EnumMethodType;
import org.xmlet.htmlapifaster.EnumTypeInputType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@MultipartConfig
public class FileUploadServlet extends AbstractServlet {
    private static final String UPLOAD_DIR = "webpage-root/uploads";

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) res;

        // Handle GET request
        if ("GET".equals(httpReq.getMethod())) {
            // Serve the static HTML form first.
            HtmlFlow
                    .doc(httpResp.getWriter())
                    .html()
                    .head().title().text("File Upload Page").__().__()
                    .body()
                    .h1().text("Upload a File").__()
                    .form().attrMethod(EnumMethodType.POST).attrAction("/upload").attrEnctype(EnumEnctypeType.MULTIPART_FORM_DATA)
                    .input().attrType(EnumTypeInputType.FILE).attrName("file").__()
                    .br().__()
                    .input().attrType(EnumTypeInputType.SUBMIT).attrValue("Upload").__()
                    .__()
                    .hr().__();

            // Now, create an HtmlView for dynamic content.
            HtmlFlow.view(httpResp.getWriter(), page -> {
                var body = page.html().body();
                String uploadedFile = httpReq.getParameter("file");
                if (uploadedFile != null) {
                    if (uploadedFile.endsWith(".png") || uploadedFile.endsWith(".jpg") || uploadedFile.endsWith(".jpeg")) {
                        body.img()
                                .attrSrc("/" + UPLOAD_DIR + "/" + uploadedFile)
                                .attrAlt("Uploaded Image")
                                .attrWidth(300L)
                                .__();
                    } else if (uploadedFile.endsWith(".txt") || uploadedFile.endsWith(".log")) {
                        body.h3().text("File Content:").__();
                        try {
                            body.pre().text(readFile(UPLOAD_DIR + "/" + uploadedFile)).__();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
        // Handle POST request
        else if ("POST".equals(httpReq.getMethod())) {
            Part filePart = httpReq.getPart("file");
            UUID ID = UUID.randomUUID();

            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, uploadPath.resolve(ID.toString()), StandardCopyOption.REPLACE_EXISTING);
            }

            httpResp.sendRedirect("/file?id=" + ID);
        }
    }

    private String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}