package com.github.edurbs.adapter;

public interface HtmlArchiver {
    void saveHtmlToFile(String content, String bookCodeName);
    String getHtmlFileContent(String bookCodeName);
    void saveFormattedHtmlToFile(String content, String bookCodeName);
    String getFormattedHtmlFileContent(String bookCodeName);
}
