package com.github.edurbs.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.application.HtmlHandler;

public class HtmlFileHandler implements HtmlHandler { 
    private static final Logger logger = LoggerFactory.getLogger(HtmlFileHandler.class);
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
            logger.info("File saved successfully to: {}", path.toAbsolutePath());
        } catch (java.io.IOException e) {
            logger.error("Error saving file: {}", e.getMessage());
        }
    }

    @Override
    public String getFileContent(String bookCodeName) {
        java.nio.file.Path path = java.nio.file.Paths.get(getFilePath(bookCodeName));
        if (java.nio.file.Files.exists(path)) {
            try {
                return java.nio.file.Files.readString(path);
            } catch (java.io.IOException e) {
                logger.error("Error reading the file: {}", e.getMessage());
            }
        } else {
            logger.error("File does not exist: {}", path.toAbsolutePath());
        }
        return "";
    }

    private String getFilePath(String bookCodeName) {
        return filePath + bookCodeName + SUFFIX;
    }

}
