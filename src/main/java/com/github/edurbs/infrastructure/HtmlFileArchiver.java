package com.github.edurbs.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.HtmlArchiver;

public class HtmlFileArchiver implements HtmlArchiver { 
    private static final Logger logger = LoggerFactory.getLogger(HtmlFileArchiver.class);
    private final String filePath;
    private static final String RAW_SUFFIX = "_complete_raw.html";
    private static final String FORMATTED_SUFFIX = ".html";

    public HtmlFileArchiver(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void saveHtmlToFile(String content, String bookCodeName) {
        saveToFile(content, getHtmlFileName(bookCodeName));
    }

    @Override
    public void saveFormattedHtmlToFile(String content, String bookCodeName) {
        saveToFile(content, getFormattedHtmlFileName(bookCodeName));
    }

    private void saveToFile(String content, String fileName){
        java.nio.file.Path path = java.nio.file.Paths.get(fileName);
        try {
            java.nio.file.Files.writeString(path, content);
            logger.info("Formatted file saved successfully to: {}", path.toAbsolutePath());
        } catch (java.io.IOException e) {
            logger.error("Error saving formatted file: {}", e.getMessage());
        }
    }

    @Override
    public String getHtmlFileContent(String bookCodeName) {
        return getFileContent(getHtmlFileName(bookCodeName));
    }

    @Override
    public String getFormattedHtmlFileContent(String bookCodeName) {
        return getFileContent(getFormattedHtmlFileName(bookCodeName));
    }
   
    private String getFileContent(String suffix) {
        java.nio.file.Path path = java.nio.file.Paths.get(suffix);
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

    private String getHtmlFileName(String bookCodeName) {
        return filePath + bookCodeName + RAW_SUFFIX;
    }

    private String getFormattedHtmlFileName(String bookCodeName) {
        return filePath + bookCodeName + FORMATTED_SUFFIX;
    }

}
