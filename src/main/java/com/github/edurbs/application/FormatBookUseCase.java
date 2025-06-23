package com.github.edurbs.application;

import com.github.edurbs.adapter.Extractor;

public class FormatBookUseCase {
    private final Extractor extractor;
    private String bookCodeName;
    private Integer chapters;
    private HtmlHandler htmlHandler;
    public FormatBookUseCase(Extractor extractor, String bookCodeName, Integer chapters, HtmlHandler htmlHandler) {
        this.extractor = extractor;
        this.bookCodeName = bookCodeName;
        this.chapters = chapters;
        this.htmlHandler = htmlHandler;
    }
    public String execute(){
        String bookContent = getHtmlFromFile(bookCodeName);
        if (bookContent.isEmpty()) {
            extractor.setBookCodeName(bookCodeName);
            extractor.setChapters(chapters);
            extractor.extractBook();
            bookContent = extractor.getBookContent();
        }
        if (bookContent.isEmpty()) {
            System.err.println("Book content is empty. Please check the extraction process. %s".formatted(bookCodeName));
        }
        return bookContent;
    }

    private String getHtmlFromFile(String bookCodeName) {
        return htmlHandler.getFileContent(bookCodeName);
    }
}
