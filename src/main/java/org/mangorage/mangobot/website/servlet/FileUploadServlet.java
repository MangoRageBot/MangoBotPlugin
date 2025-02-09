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
import org.xmlet.htmlapifaster.EnumRelType;
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
            // Retrieve the file part
            Part filePart = httpReq.getPart("file"); // Use the correct name attribute value
            if (filePart == null || filePart.getSize() == 0) {
                httpResp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file was uploaded.");
                return;
            }

            // Generate a unique ID for the file
            UUID ID = UUID.randomUUID();
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String fileExtension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";

            // Define the upload directory
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the file to the upload directory
            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, uploadPath.resolve(ID + fileExtension), StandardCopyOption.REPLACE_EXISTING);
            }

            // Redirect to a success page or provide the uploaded file's link
            httpResp.sendRedirect("/file?id=" + ID + fileExtension);
        }
    }

}