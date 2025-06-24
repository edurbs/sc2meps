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
    private String html;

    public FormatBookUseCase(Extractor extractor, HtmlArchiver htmlArchiver, HtmlParser htmlParser) {
        this.extractor = extractor;
        this.htmlArchiver = htmlArchiver;
        this.htmlParser = htmlParser;
    }
 
    public String execute(ScriptureEarthBookName scriptureEarthBookName) {
        String bookCodeName = scriptureEarthBookName.getName();
        Integer chapters = scriptureEarthBookName.getChapters();
        html = getHtmlFromFile(bookCodeName);
        if (html.isEmpty()) {
            html = extractor.extractBook(bookCodeName, chapters);
        }
        if (html.isEmpty()) {
            logger.error("Book content is empty. Please check the extraction process. {}", bookCodeName);
        }
        format();
        htmlArchiver.saveFormattedHtmlToFile(html, bookCodeName);
        return html;
    }

    private String getHtmlFromFile(String bookCodeName) {
        return htmlArchiver.getHtmlFileContent(bookCodeName);
    }

    private void format() {
        htmlParser.readHtml(html);
        cleanText();
        removeGlueSpace();
        fixHardReturns();
        html = htmlParser.getHtml();
    }

    private void fixHardReturns() {
        htmlParser.changeElementByTagAndProperty("div", "data-verse", "span");
    }

    private void removeGlueSpace() {
        htmlParser.replace("&nbsp;", " "); // Non-breaking space
    }

    private void cleanText() {
        htmlParser.removeElementByClass("video-block");
        htmlParser.removeElementByClass("video-block");
    }

}
