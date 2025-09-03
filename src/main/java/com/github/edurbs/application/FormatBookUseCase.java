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
        // TODO Remover numero do capitulo quando tiver só 1 capítulo
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

        handleUnitedVerses();

        fixInvalidChapters();

        // each chapter must have the correct number of scriptures
        checkChapterSize();


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

    private void fixInvalidChapters() {
        List<String> chapters = getChapters();
        List<String> validChapters = new ArrayList<>();
        for(int chapterNumber = 1; chapterNumber <= scriptureEarthBookName.getChapters(); chapterNumber++){
            int chapterNumberFromHtml = -1;
            String chapterHtml = "";
            for(String chapter : chapters){
                htmlParser.readHtml(chapter);
                chapterNumberFromHtml = getChapterNumber();
                if(chapterNumberFromHtml==0 && scriptureEarthBookName.getChapters()==1 && chapterNumber==1){
                    return;
                }
                if(chapterNumberFromHtml==chapterNumber){
                    chapterHtml = htmlParser.getHtml();
                    break;
                }
            }
            if(chapterHtml.isBlank()){
                StringBuilder newChapter = new StringBuilder();
                StringBuilder verses = new StringBuilder();
                for(int verse = 1; verse <= scriptureEarthBookName.getNumberOfScriptures(chapterNumber); verse++){
                    verses.append("<b>%s</b> <span>--</span> ".formatted(verse));
                }
                newChapter.append("<div class=\"chapter\" id=\"chapter-%d\">{%d} %s</div>".formatted(chapterNumber, chapterNumber, verses));
                chapterHtml = newChapter.toString();
            }
            if(!chapterHtml.isBlank()){
                validChapters.add(chapterHtml);
            }
        }
        String chapterHtml = addBookHtml(validChapters);
        htmlParser.readHtml(chapterHtml);
    }

    private void checkChapterSize() {
        List<Integer> chaptersSize = new ArrayList<>();
        for (int chapterNumber = 1; chapterNumber <= scriptureEarthBookName.getChapters(); chapterNumber++) {
            chaptersSize.add(scriptureEarthBookName.getNumberOfScriptures(chapterNumber));
        }
        htmlParser.checkTotalVerses(chaptersSize, scriptureEarthBookName.getMepsName().name(), scriptureEarthBookName.getMepsName().getNumberOfChapters());
    }

    private void handleUnitedVerses() {
        String dash = "-";
        String see = "ꞌMadâꞌâ";
        htmlParser.handleUnitedVerses(dash, see);
    }

    private void handlePoeticText() {
        // Add a soft return (Shift+Enter) at the end of each line.
        // muda div class q para span class q
        // adicionar <br> no final do texto
        addSoftReturnAtEndOfEachLineOfPoeticText();

        // Add a hard return at the end of a stanza.
        // ok??

        // When poetic text starts in the middle of a verse, add a soft return (Shift+Enter) to the end of the line preceding the poetic text.
        addSoftReturnWhenPoeticTextStartInTheMiddleOfAVerse();

        // Place an Equals sign (=) before the first chapter or verse number where poetic text starts.
        addEqualsSignAtVerseWherePoeticTextStarts();

        // If poetic text starts in the middle of a verse, no Equals sign (=) is necessary.
        // ok??

        // If a Bible book begins with poetic text, place the Equals sign (=) at the beginning of the second verse containing poetic text instead.
        // ok??

        // Place a Plus sign (+) at the start of a line when body text immediately follows poetic text
        addPlusSignAfterPoeticText();
    }

    private void addEqualsSignAtVerseWherePoeticTextStarts() {
        // last div or span is not a class Q
        // this span is class Q
        // this span contains:
        //   a span that has a attribute data-verse
        //   and this last span has a span class v
        //   if all true, then add Equals sign to the start of this span
        var tagToSearch = new TagAttribute("span", TAG_ATTR_CLASS, "q");
        String classToCheck = "q";
        var subTagToCheckAttribute = new TagAttribute("span", "data-verse", "");
        var subTagToCheckClass = new TagAttribute("span", TAG_ATTR_CLASS, "v");
        htmlParser.addTextBeforeIfSomeChecksTrue(tagToSearch, classToCheck, subTagToCheckAttribute, subTagToCheckClass, "=");
    }

    private void addSoftReturnWhenPoeticTextStartInTheMiddleOfAVerse() {
        // if after a div class P there is a span class Q, then
        // change the div class P to span class P
        // and add a BR as the last child of this span class P
        var tagNormalText = new TagAttribute("div", "class", "p");
        var firstVerseTag = new TagAttribute("div", "class", "m");
        var tagSpanPoeticText = new TagAttribute("span", "class", "q");
        var tagBr = new TagAttribute("br", "", "");
        htmlParser.changeTagIfNextIsAndAddTag(tagNormalText, tagSpanPoeticText, tagBr);
        htmlParser.changeTagIfNextIsAndAddTag(firstVerseTag, tagSpanPoeticText, tagBr);
    }

    private void addPlusSignAfterPoeticText() {
        List<TagAttribute> poeticTextTags = new ArrayList<>();
        poeticTextTags.add(new TagAttribute("span", TAG_ATTR_CLASS, "q"));
        List<TagAttribute> normalText = new ArrayList<>();
        normalText.add(new TagAttribute("div", TAG_ATTR_CLASS, "s"));
        normalText.addAll(poeticTextTags);
        htmlParser.prependTextToNextTagIfNotSameTagAndIfIsInList(poeticTextTags, "+", normalText);

    }

    private void addSoftReturnAtEndOfEachLineOfPoeticText() {
        var tagDivPoeticText = new TagAttribute("div", "class", "q");
        htmlParser.changeTag(tagDivPoeticText, "span");

        var tagBr = new TagAttribute("br", "", "");
        var tagSpanPoeticText = new TagAttribute("span", "class", "q");
        htmlParser.addChildIfNextTagIsTheSame(tagSpanPoeticText, tagBr);
    }

    private void addPlusSignAfterHeadings() {
        List<TagAttribute> mainHeadingTags = new ArrayList<>();
        mainHeadingTags.add(new TagAttribute("span", "data-verse", "title"));
        mainHeadingTags.add(new TagAttribute("div", TAG_ATTR_CLASS, "s"));
        mainHeadingTags.add(new TagAttribute("div", TAG_ATTR_CLASS, "d"));
        mainHeadingTags.add(new TagAttribute("div", TAG_ATTR_CLASS, "ms"));
        for (int i = 1; i <= 25; i++) {
            mainHeadingTags.add(new TagAttribute("div", "class", "ms" + i));
        }
        htmlParser.prependTextToNextTagIfNotSameTag(mainHeadingTags, "+");
    }

    private void handleFootnotes() {
        // Replace each footnote reference symbol with an Asterisk (*) in the body text.
        // Add a hard return at the end of each line with footnote text.
        // Place a Number sign (#) at the start of a line with footnote text.
        // Place all footnote text at the end of the Bible book.        
        var tagFootnoteOriginal = new TagAttribute("div", "type", "footnote");
        List<String> allTextFootnote = htmlParser.getTextTags(tagFootnoteOriginal);
        StringBuilder mepsFootnotes = getNotes(allTextFootnote);
        htmlParser.addHtmlAtEnd(mepsFootnotes.toString());        
    }

    private StringBuilder getNotes(List<String> allTextFootnote) {
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
            headings.add(new TagAttribute("div", "id", "ms" + i));
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
            int chapterNumber = getChapterNumber();
            if(chapterNumber==0){
                continue;
            }
            if (Superscription.thisChapterHas(chapterNumber)) {
                addSuperscription();
            }
            formattedChapters.add(htmlParser.getHtml());
        }
        String chapterHtml = addBookHtml(formattedChapters);
        htmlParser.readHtml(chapterHtml);
    }

    private List<String> getChapters() {
        var chapterDivisionTag = new TagAttribute("div", TAG_ATTR_CLASS, "chapter");
        return htmlParser.getHtmlTags(chapterDivisionTag);
    }

    private int getChapterNumber() {
        var chapterTag = new TagAttribute("span", TAG_ATTR_CLASS, CLASS_C_DROP);
        String stringChapterNumber = htmlParser.getTagText(chapterTag);
        if (stringChapterNumber.isEmpty() && scriptureEarthBookName.getChapters()>1) {
            return 0;
        }else if(stringChapterNumber.isEmpty() && scriptureEarthBookName.getChapters()==1){
            return 1;
        }
        stringChapterNumber = stringChapterNumber.replace("{", "").replace("}", "");
        String[] parts = stringChapterNumber.split(" ");
        stringChapterNumber = parts[0];
        return Integer.parseInt(stringChapterNumber);
    }

    private String addBookHtml(List<String> formattedChapters) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\">");
        if (withCss()) {
            sb.append(
                    "<link rel=\"stylesheet\" href=\"https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/0.BD-KWcsM.css\">");
            sb.append(
                    "<link rel=\"stylesheet\" href=\"https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-app.css\">");
        }
        sb.append("</head>");
        sb.append("<body>");
        //sb.append("<div class=\"mt\"><span class=\"percent\">%</span><span class=\"mt\">");
        sb.append("<div class=\"mt99\"><span class=\"mt\">");
        String bookName = htmlParser.getTagText(new TagAttribute("span", TAG_ATTR_CLASS, "mt"));
        sb.append("<b>").append(bookName).append("</b>");
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
        var tagTitle = new TagAttribute("div", TAG_ATTR_CLASS, "mt99");
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
            var tagChapter = new TagAttribute("span", TAG_ATTR_CLASS, CLASS_C_DROP);
            var tagFirstVerde = new TagAttribute("span", TAG_ATTR_CLASS, "v");
            htmlParser.addTagAfter(tagChapter, tagFirstVerde, "1");
            htmlParser.removeTag(tagChapter);
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
        var chaptersFromTo = new TagAttribute("div", TAG_ATTR_CLASS, "mr");
        htmlParser.removeTag(chaptersFromTo);
    }

    private void removeInlineNotes() {
        TagAttribute tagInlineNote = new TagAttribute("span", "data-graft", "");
        htmlParser.changeTagAndText(tagInlineNote, "span", "* ");
    }

    private void removeCss(){
        var linkCss = new TagAttribute("link", "rel", "stylesheet");
        htmlParser.removeTag(linkCss);
        htmlParser.removeStyleFromTags("span");
    }

}
