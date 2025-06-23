package com.github.edurbs.adapter;

public interface HtmlHandler {
    void saveToFile(StringBuilder content, String bookCodeName);
    String getFileContent(String bookCodeName);
}
