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

        // step 3.1
        cleanText();

        // step 3.3
        removeGlueSpace();
        fixHardReturns();
        removeCss();

        // step 4.A.1.a



        makeVerseNumbersBold();

        html = htmlParser.getHtml();
    }

    private void makeVerseNumbersBold() {
        htmlParser.makeElementBoldByTagAndClass("span", "v");
        htmlParser.addSpaceAfterElementByTagAndClass("span", "v");
    }

    private void removeCss() {
        htmlParser.removeElementByTagAndProperty("link", "rel");
    }

    private void fixHardReturns() {
        htmlParser.changeElementByTagAndProperty("div", "data-verse", "span");
    }

    private void removeGlueSpace() {
        htmlParser.replace("&nbsp;", " "); // Non-breaking space
        htmlParser.removeElementByTagAndClass("span", "vsp");
    }

    private void cleanText() {
        htmlParser.removeElementByTagAndClass("div","video-block");
        htmlParser.removeElementByTagAndClass("div", "footer");
    }

}
