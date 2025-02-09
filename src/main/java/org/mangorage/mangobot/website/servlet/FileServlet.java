package org.mangorage.mangobot.website.servlet;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "webpage-root/uploads";  // Your uploads folder path

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

        // If the file is an HTML file, return a forbidden response
        /**
        if (fileName.endsWith(".html")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "HTML files cannot be loaded!");
            return;
        }
         **/

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
        if (fileName.endsWith(".txt")) {
            contentType = "text/plain";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (fileName.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (fileName.endsWith(".json")) {
            contentType = "application/json";
        } else if (fileName.endsWith(".css")) {
            contentType = "text/css";
        } else if (fileName.endsWith(".js")) {
            contentType = "application/javascript";
        }

        return contentType;
    }

}