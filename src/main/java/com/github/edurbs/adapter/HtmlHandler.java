package com.github.edurbs.adapter;

public interface HtmlHandler {
    void saveHtmlToFile(StringBuilder content, String bookCodeName);
    String getHtmlFileContent(String bookCodeName);
    void saveFormattedHtmlToFile(StringBuilder content, String bookCodeName);
    String getFormattedHtmlFileContent(String bookCodeName);
}
