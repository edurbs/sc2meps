package com.github.edurbs.application;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.adapter.FormatBook;
import com.github.edurbs.adapter.HtmlArchiver;
import com.github.edurbs.adapter.HtmlParser;
import com.github.edurbs.domain.MepsBookName;
import com.github.edurbs.domain.ScriptureEarthBookName;
import com.github.edurbs.domain.Superscription;
import com.github.edurbs.infrastructure.AppConfig;

public class FormatBookUseCase implements FormatBook {
    private static final Logger logger = LoggerFactory.getLogger(FormatBookUseCase.class);
    private static final String TAG_ATTR_CLASS = "class";
    private static final String CLASS_C_DROP = "c-drop";
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

    private boolean withCss() {
        return AppConfig.withCss();
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

        // step 3.1
        // Remove unwanted text such as introductions, comments, footers, and page
        // numbers.
        cleanText();

        // step 3.3
        // Clean up soft and hard returns.
        removeGlueSpace();
        fixHardReturns();
        
        // step 4.A.2
        // For books not containing chapters, add verse number one to the beginning of
        // the first verse, if it has not been included in the pasted text.
        addVerseNumberOneIfMissing();
        
        // step 4.A.3.a
        // Add curly brackets { } around the number. For example, chapter 10 would
        // appear as {10}.
        addCurlyBracketsAroundChapterNumbers();

        // step 4.A.3.b
        // Ensure that one space exists after each chapter number.
        addSpaceAfterChapterNumbers();
        
        // step 4.A.4.b
        makeVerseNumbersBold();

        // step 4.B Title
        // Make sure that a Percent sign (%) appears at the start of the first line with
        // the book number and at the start of the second line with the Bible book name.
        addPercentSignToBookName();

        // step 4.B Headings
        // Place a Dollar sign ($) at the start of a line with a superscription.
        addDollarSignToSuperscription();



        // step 4.B Headings
        // Place an At sign (@) at the start of a line with any heading other than a
        // book division or superscription.
        addAtSignToHeadings();

        // ********************
        // step 4.B Poetic text
        // ********************
        handlePoeticText();

        // step 4.B Body text
        // Place a Plus sign (+) at the start of a line when body text immediately follows any type of heading.
        addPlusSignAfterHeadings();

        // ***********************
        // step 4.B Body footnotes
        // ***********************
        handleFootnotes();

        // FINAL FORMAT
        // step 2
        // add meps header in first line
        addHeader();

        // On the second line, paste or type the Bible book name and make sure it is
        // bold.
        makeBookNameBold();


        if(!withCss()){
            removeCss();
            removeInlineNotes();
        }
        html = htmlParser.getHtml();
        
    }

    private void handlePoeticText() {
        // Add a soft return (Shift+Enter) at the end of each line.
        // TODO
        // muda div class q para span class q
        // adicionar <br> no final do texto
        var tagDivPoeticText = new TagAttribute("div", "class", "q");
        htmlParser.changeTag(tagDivPoeticText, "span");

        var tagBr = new TagAttribute("br", "", "");
        var tagSpanPoeticText = new TagAttribute("span", "class", "q");
        htmlParser.addTagAfter(tagSpanPoeticText, tagBr, "");

        // Add a hard return at the end of a stanza.
        // TODO
        // When poetic text starts in the middle of a verse, add a soft return (Shift+Enter) to the end of the line preceding the poetic text.
        // TODO
        // Place an Equals sign (=) before the first chapter or verse number where poetic text starts.
        // TODO
        // If poetic text starts in the middle of a verse, no Equals sign (=) is necessary.
        // TODO
        // If a Bible book begins with poetic text, place the Equals sign (=) at the beginning of the second verse containing poetic text instead.
        // TODO
        // Place a Plus sign (+) at the start of a line when body text immediately follows poetic text
        // TODO

    }

    private void addPlusSignAfterHeadings() {
        List<TagAttribute> mainHeadingTags = new ArrayList<>();
        mainHeadingTags.add(new TagAttribute("span", "data-verse", "title"));
        mainHeadingTags.add(new TagAttribute("div", TAG_ATTR_CLASS, "s"));
        htmlParser.prependTextToNextTagIfNotSameTag(mainHeadingTags, "+");
    }

    private void handleFootnotes() {
        // Replace each footnote reference symbol with an Asterisk (*) in the body text.
        // Add a hard return at the end of each line with footnote text.
        // Place a Number sign (#) at the start of a line with footnote text.
        // Place all footnote text at the end of the Bible book.        
        var tagFootnoteOriginal = new TagAttribute("div", "id", "X-1");
        List<String> allTextFootnote = htmlParser.getTextTags(tagFootnoteOriginal);
        StringBuilder mepsFootnotes = getStringBuilder(allTextFootnote);
        htmlParser.addHtmlAtEnd(mepsFootnotes.toString());        
        var tagFootnoteReferenceSymbol = new TagAttribute("sup", TAG_ATTR_CLASS, "footnote");
        htmlParser.changeTagAndText(tagFootnoteReferenceSymbol, "span", "*");
    }

    private StringBuilder getStringBuilder(List<String> allTextFootnote) {
        StringBuilder mepsFootnotes = new StringBuilder();
        for (String textFootnote : allTextFootnote) {
            String subStringUntilFirstSpace = textFootnote.substring(0, textFootnote.indexOf(" "));
            String subStringAfterFirstSpace = textFootnote.substring(textFootnote.indexOf(" "));
            String reference = "#"+subStringUntilFirstSpace.replace(".", ":");
            String newFootnote = "<div>%s %s</div>".formatted(reference, subStringAfterFirstSpace);
            mepsFootnotes.append(newFootnote);
        }
        return mepsFootnotes;
    }

    private void addAtSignToHeadings() {
        List<TagAttribute> headingTags = getHeadingTags();
        headingTags.forEach(tag -> htmlParser.addTextBefore(tag, "@"));
    }

    private List<TagAttribute> getHeadingTags() {
        List<TagAttribute> headings = new ArrayList<>();
        headings.add(new TagAttribute("span", TAG_ATTR_CLASS, "mt2"));
        for (int i = 1; i <= 25; i++) {
            headings.add(new TagAttribute("div", "id", "s" + i));
        }
        return headings;
    }

    private void addDollarSignToSuperscription() {
        boolean isNotPsalm = !scriptureEarthBookName.equals(ScriptureEarthBookName.BOOK_PSA);
        if (isNotPsalm) {
            return;
        }
        List<String> chapters = getChapters();
        List<String> formattedChapters = new ArrayList<>();
        for (String chapter : chapters) {
            htmlParser.readHtml(chapter);
            int chapterNumber = getFirstChapterNumber();
            if(chapterNumber==0){
                continue;
            }
            if (Superscription.thisChapterHas(chapterNumber)) {
                addSuperscription();
            }
            formattedChapters.add(htmlParser.getHtml());
        }
        String chapterHtml = addPsalmFooter(formattedChapters);
        htmlParser.readHtml(chapterHtml);
    }

    private List<String> getChapters() {
        var chapterDivisionTag = new TagAttribute("div", TAG_ATTR_CLASS, "chapter");
        return htmlParser.getHtmlTags(chapterDivisionTag);
    }

    private int getFirstChapterNumber() {
        var chapterTag = new TagAttribute("span", TAG_ATTR_CLASS, CLASS_C_DROP);
        String stringChapterNumber = htmlParser.getTagText(chapterTag);
        if (stringChapterNumber.isEmpty()) {
            return 0;
        }
        stringChapterNumber = stringChapterNumber.replace("{", "").replace("}", "");
        return Integer.parseInt(stringChapterNumber);
    }

    private String addPsalmFooter(List<String> formattedChapters) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        if (withCss()) {
            sb.append(
                    "<link rel=\"stylesheet\" href=\"https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/0.BD-KWcsM.css\">");
            sb.append(
                    "<link rel=\"stylesheet\" href=\"https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-app.css\">");
        }
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div class=\"mt\"><span class=\"percent\">%</span><span class=\"mt\">");
        sb.append("<b>Salmu</b>");
        sb.append("</span></div>");
        sb.append(String.join("\n", formattedChapters));
        sb.append("</body></html>");
        return sb.toString();
    }

    private void addSuperscription() {
        var tagSuperscription = new TagAttribute("span", "id", "nonea");
        String superscriptionText = htmlParser.getTagText(tagSuperscription);
        if (superscriptionText.isEmpty()) {
            // add empy string before the tag div class m
            var tagM = new TagAttribute("div", TAG_ATTR_CLASS, "m");
            htmlParser.addTagBefore(tagM, new TagAttribute("div", TAG_ATTR_CLASS, "d"), "$");    
        }else{
            htmlParser.addTagBefore(tagSuperscription, new TagAttribute("span", TAG_ATTR_CLASS, "superscription"), "$");
        }
    }

    private void makeBookNameBold() {
        var mt = new TagAttribute("span", TAG_ATTR_CLASS, "mt");
        htmlParser.makeTextBold(mt);
    }

    private void addPercentSignToBookName() {
        var mt = new TagAttribute("span", TAG_ATTR_CLASS, "mt");
        htmlParser.addTagBefore(mt, new TagAttribute("span", TAG_ATTR_CLASS, "percent"), "%");
    }

    private void addHeader() {
        int ordinal = scriptureEarthBookName.getMepsName().getOrdinal();
        String ordinalWithTwoNumbers = String.format("%02d", ordinal);
        String lineHeader = "%%%s".formatted(ordinalWithTwoNumbers);
        var tagTitle = new TagAttribute("div", TAG_ATTR_CLASS, "mt");
        htmlParser.addTagBefore(tagTitle, new TagAttribute("div", TAG_ATTR_CLASS, "mepsCode"), lineHeader);
    }

    private void addSpaceAfterChapterNumbers() {
        var tagAttribute = new TagAttribute("span", TAG_ATTR_CLASS, CLASS_C_DROP);
        htmlParser.addSpaceAfterText(tagAttribute);
    }

    private void addCurlyBracketsAroundChapterNumbers() {
        var tagAttribute = new TagAttribute("span", TAG_ATTR_CLASS, CLASS_C_DROP);
        htmlParser.surroundTextWith(tagAttribute, "{", "}");
    }

    private void addVerseNumberOneIfMissing() {
        if (scriptureEarthBookName.getChapters() == 1) {
            var tagAttribute = new TagAttribute("span", TAG_ATTR_CLASS, CLASS_C_DROP);
            var newTagAttribute = new TagAttribute("span", TAG_ATTR_CLASS, "v");
            htmlParser.addTagAfter(tagAttribute, newTagAttribute, "1");
        }
    }

    private void makeVerseNumbersBold() {
        var tagAttribute = new TagAttribute("span", TAG_ATTR_CLASS, "v");
        htmlParser.makeTextBold(tagAttribute);
        htmlParser.addSpaceAfterText(tagAttribute);
    }

    private void fixHardReturns() {
        var tagDataVerse = new TagAttribute("div", "data-verse", "");
        htmlParser.changeTag(tagDataVerse, "span");
        var tagCDrop = new TagAttribute("div", TAG_ATTR_CLASS, CLASS_C_DROP);
        htmlParser.changeTag(tagCDrop, "span");
    }

    private void removeGlueSpace() {
        htmlParser.replace("&nbsp;", " "); // Non-breaking space
        var elementTag = new TagAttribute("span", TAG_ATTR_CLASS, "vsp");
        htmlParser.removeTag(elementTag);
    }

    private void cleanText() {
        var videoTag = new TagAttribute("div", TAG_ATTR_CLASS, "video-block");
        htmlParser.removeTag(videoTag);
        var footerTag = new TagAttribute("div", TAG_ATTR_CLASS, "footer");
        htmlParser.removeTag(footerTag);
        var linkTag = new TagAttribute("div", TAG_ATTR_CLASS, "r");
        htmlParser.removeTag(linkTag);
    }

    private void removeInlineNotes() {
        TagAttribute tagInlineNote = new TagAttribute("span", "data-graft", "");
        htmlParser.removeTag(tagInlineNote);
    }

    private void removeCss(){
        var linkCss = new TagAttribute("link", "rel", "stylesheet");
        htmlParser.removeTag(linkCss);
        htmlParser.removeStyleFromTags("span");
    }

}
