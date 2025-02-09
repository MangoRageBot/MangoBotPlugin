package org.mangorage.mangobot.website.servlet;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "webpage-root/uploads";  // Your uploads folder path
    private static final Map<String, String> EXTENSIONS = new HashMap<>();

    static {
        EXTENSIONS.put(".jpg", "image/jpeg");
        EXTENSIONS.put(".jpeg", "image/jpeg");
        EXTENSIONS.put(".png", "image/png");
        EXTENSIONS.put(".gif", "image/gif");
        EXTENSIONS.put(".pdf", "application/pdf");
        EXTENSIONS.put(".json", "application/json");
        EXTENSIONS.put(".mp4", "video/mp4");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the 'id' parameter from the query string
        String fileId = request.getParameter("id");

        if (fileId == null || fileId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File ID is required.");
            return;
        }

        // Handle file request
        handleFileRequest(fileId, response);
    }

    private void handleFileRequest(String fileName, HttpServletResponse response) throws IOException {
        // Sanitize the file name to prevent directory traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid file name.");
            return;
        }

        File file = new File(UPLOAD_DIR, fileName);

        // Serve the file content
        if (file.exists() && file.isFile()) {
            String contentType = determineContentType(file);
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());

            try (InputStream fileInputStream = new FileInputStream(file)) {
                fileInputStream.transferTo(response.getOutputStream());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
    }

    private String determineContentType(File file) {
        //String contentType = "application/octet-stream";  // Default content type for unknown files
        String contentType = "text/plain";

        String fileName = file.getName().toLowerCase();
        var index = fileName.lastIndexOf(".");

        if (index == -1) {
            return contentType;
        }

        return EXTENSIONS.getOrDefault(fileName.substring(index), contentType);
    }

}