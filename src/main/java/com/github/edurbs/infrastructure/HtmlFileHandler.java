package com.github.edurbs.infrastructure;

import com.github.edurbs.application.HtmlHandler;

public class HtmlFileHandler implements HtmlHandler { 
    private final String filePath;
    private static final String SUFFIX = "_complete_raw.html";

    public HtmlFileHandler(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void saveToFile(StringBuilder content, String bookCodeName) {
        java.nio.file.Path path = java.nio.file.Paths.get(getFilePath(bookCodeName));
        try {
            java.nio.file.Files.writeString(path, content.toString());
            System.out.println("File saved successfully to: " + path.toAbsolutePath());
        } catch (java.io.IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    @Override
    public String getFileContent(String bookCodeName) {
        java.nio.file.Path path = java.nio.file.Paths.get(getFilePath(bookCodeName));
        if (java.nio.file.Files.exists(path)) {
            try {
                return java.nio.file.Files.readString(path);
            } catch (java.io.IOException e) {
                System.err.println("Error reading the file: " + e.getMessage());
            }
        } else {
            System.err.println("File does not exist: " + path.toAbsolutePath());
        }
        return "";
    }

    private String getFilePath(String bookCodeName) {
        return filePath + bookCodeName + SUFFIX;
    }

}
