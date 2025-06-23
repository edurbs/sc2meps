package com.github.edurbs.application;

public interface HtmlHandler {
    void saveToFile(StringBuilder content, String bookCodeName);
    String getFileContent(String bookCodeName);
}
