package com.github.edurbs.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.adapter.FormatBook;
import com.github.edurbs.adapter.HtmlArchiver;
import com.github.edurbs.adapter.HtmlParser;
import com.github.edurbs.domain.MepsBookName;
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
        MepsBookName mepsBookName = scriptureEarthBookName.getMepsName();
        String formattedFileName = mepsBookName.getMepsFormat();
        htmlArchiver.saveFormattedHtmlToFile(html, formattedFileName);
        return html;
    }

    private String getHtmlFromFile(String bookCodeName) {
        return htmlArchiver.getHtmlFileContent(bookCodeName);
    }

    private void format() {
        htmlParser.readHtml(html);

        // step 2
        // add meps header in first line
        addHeader();
        // On the second line, paste or type the Bible book name and make sure it is bold.
        makeBookNameBold();


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

        // step 4.A.4.b
        makeVerseNumbersBold();

        // sped 4.B Title 
        // Make sure that a Percent sign (%) appears at the start of the first line with the book number and at the start of the second line with the Bible book name.
        addPercentSignToBookName();

        html = htmlParser.getHtml();
    }

    private void makeBookNameBold() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'makeBookNameBold'");
    }

    private void addPercentSignToBookName() {
        var mt =  new TagAttribute("span", "class", "mt");
        htmlParser.addTextBefore(mt, "%");
    }

    private void addHeader() {
        int ordinal = scriptureEarthBookName.getMepsName().getOrdinal();
        String ordinalWithTwoNumbers = String.format("%02d", ordinal);
        String lineHeader = "%%%s".formatted(ordinalWithTwoNumbers);
        var tagAttribute = new TagAttribute("div", "data-verse", "title");
        htmlParser.addTagBefore(tagAttribute, new TagAttribute("div", "class", "mepsCode"), lineHeader);
    }

    private void addSpaceAfterChapterNumbers() {
        var tagAttribute = new TagAttribute("span", "class", "c-drop");
        htmlParser.addSpaceAfterText(tagAttribute);
    }

    private void addCurlyBracketsAroundChapterNumbers() {
        var tagAttribute = new TagAttribute("span", "class", "c-drop");
        htmlParser.surroundTextWith(tagAttribute, "{", "}");
    }

    private void addVerseNumberIfMissing() {
        if (scriptureEarthBookName.getChapters() == 1) {
            var tagAttribute = new TagAttribute("span", "class", "c-drop");
            var newTagAttribute = new TagAttribute("span", "class", "v");
            htmlParser.addTagAfter(tagAttribute, newTagAttribute, "1");
        }
    }

    private void makeVerseNumbersBold() {
        var tagAttribute = new TagAttribute("span", "class", "v");
        htmlParser.makeTextBold(tagAttribute);
        htmlParser.addSpaceAfterText(tagAttribute);
    }

    private void removeCss() {
        var tagAttribute = new TagAttribute("link", "rel", "");
        htmlParser.removeTag(tagAttribute);
    }

    private void fixHardReturns() {
        var tagDataVerse = new TagAttribute("div", "data-verse", "");
        htmlParser.changeTag(tagDataVerse, "span");
        var tagCDrop = new TagAttribute("div", "class", "c-drop");
        htmlParser.changeTag(tagCDrop, "span");
    }

    private void removeGlueSpace() {
        htmlParser.replace("&nbsp;", " "); // Non-breaking space
        var elementTag = new TagAttribute("span", "class", "vsp");
        htmlParser.removeTag(elementTag);
    }

    private void cleanText() {
        var videoTag = new TagAttribute("div", "class", "video-block");
        htmlParser.removeTag(videoTag);
        var footerTag = new TagAttribute("div", "class", "footer");
        htmlParser.removeTag(footerTag);
    }

}
