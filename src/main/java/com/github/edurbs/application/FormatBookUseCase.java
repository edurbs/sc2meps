package com.github.edurbs.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.adapter.FormatBook;
import com.github.edurbs.adapter.HtmlArchiver;
import com.github.edurbs.adapter.HtmlParser;
import com.github.edurbs.domain.ScriptureEarthBookName;

public class FormatBookUseCase implements FormatBook {
    private static final Logger logger = LoggerFactory.getLogger(FormatBookUseCase.class);
    private final HtmlArchiver htmlArchiver;
    private final Extractor extractor;
    private final HtmlParser htmlParser;

    public FormatBookUseCase(Extractor extractor, HtmlArchiver htmlArchiver, HtmlParser htmlParser) {
        this.extractor = extractor;
        this.htmlArchiver = htmlArchiver;
        this.htmlParser = htmlParser;
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
        String formattedBookContent = format(bookContent);
        htmlArchiver.saveFormattedHtmlToFile(formattedBookContent, bookCodeName);
        return formattedBookContent;
    }

    private String getHtmlFromFile(String bookCodeName) {
        return htmlArchiver.getHtmlFileContent(bookCodeName);
    }

    private String format(String bookContent) {
        String formattedContent = bookContent;
        formattedContent = cleanText(formattedContent);
        return formattedContent;
    }

    private String cleanText(String text) {
        String textCleaned = text; 
        textCleaned = htmlParser.removeDiv(textCleaned, "video-block");
        textCleaned = htmlParser.removeDiv(textCleaned, "footer-line");
        return textCleaned;
    }


}
