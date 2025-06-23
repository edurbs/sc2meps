package com.github.edurbs.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.adapter.FormatBook;
import com.github.edurbs.adapter.HtmlArchiver;
import com.github.edurbs.domain.ScriptureEarthBookName;

public class FormatBookUseCase implements FormatBook {
    private static final Logger logger = LoggerFactory.getLogger(FormatBookUseCase.class);
    private final HtmlArchiver htmlHandler;
    private final Extractor extractor;

    public FormatBookUseCase(Extractor extractor, HtmlArchiver htmlHandler) {
        this.extractor = extractor;
        this.htmlHandler = htmlHandler;
    }
 
    public String execute(ScriptureEarthBookName scriptureEarthBookName) {
        String bookCodeName = scriptureEarthBookName.getName();
        Integer chapters = scriptureEarthBookName.getChapters();
        String bookContent = getHtmlFromFile(bookCodeName);
        if (bookContent.isEmpty()) {
            bookContent = extractor.extractBook(bookCodeName, chapters);
        }
        if (bookContent.isEmpty()) {
            logger.error("Book content is empty. Please check the extraction process. {}", bookCodeName);
        }
        return format(bookContent);
    }

    private String getHtmlFromFile(String bookCodeName) {
        return htmlHandler.getHtmlFileContent(bookCodeName);
    }
    private String format(String bookContent) {
        // Implement the formatting logic here
        // For now, just return the content as is
        return bookContent;
    }

    private String cleanText(String text) {
        // Remove unwanted text such as introductions, comments, footers, and page numbers.
        return text;
    }


}
