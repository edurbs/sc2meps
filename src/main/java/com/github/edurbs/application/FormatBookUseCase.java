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
    private ScriptureEarthBookName scriptureEarthBookName;

    public FormatBookUseCase(Extractor extractor, HtmlArchiver htmlArchiver, HtmlParser htmlParser) {
        this.extractor = extractor;
        this.htmlArchiver = htmlArchiver;
        this.htmlParser = htmlParser;
    }
 
    public String execute(ScriptureEarthBookName scriptureEarthBookName) {
        this.scriptureEarthBookName = scriptureEarthBookName;
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
        // Remove unwanted text such as introductions, comments, footers, and page numbers.
        cleanText();

        // step 3.3
        // Clean up soft and hard returns.
        removeGlueSpace();
        fixHardReturns();
        removeCss();        

        // step 4.A.2
        // For books not containing chapters, add verse number one to the beginning of the first verse, if it has not been included in the pasted text.
        addVerseNumberIfMissing();

        // step 4.A.3.a
        // Add curly brackets { } around the number. For example, chapter 10 would appear as {10}.
        addCurlyBracketsAroundChapterNumbers();

        // step 4.A.3.b
        // Ensure that one space exists after each chapter number.
        addSpaceAfterChapterNumbers();


        makeVerseNumbersBold();

        html = htmlParser.getHtml();
    }

    private void addSpaceAfterChapterNumbers() {
        htmlParser.addSpaceAfterElementByTagAndClass("span", "c-drop");
    }

    private void addCurlyBracketsAroundChapterNumbers() {
        htmlParser.surroundElementByTagAndClassWithText("span", "c-drop", "{", "}");
    }

    private void addVerseNumberIfMissing() {
        if (scriptureEarthBookName.getChapters() == 1) {
            htmlParser.addTextAfterElementByTagAndClass("span", "c-drop", "span", "v", "1");
        }
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
        htmlParser.changeElementByClassAndProperty("div", "c-drop", "span");
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
