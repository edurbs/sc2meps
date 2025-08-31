package com.github.edurbs.infrastructure.htmlparser;

import java.util.List;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.edurbs.adapter.HtmlParser;
import com.github.edurbs.application.TagAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupHtmlParser implements HtmlParser {
    private static final Logger logger = LoggerFactory.getLogger(JsoupHtmlParser.class);
    private Document document;
    private final String DATA_VERSE = "data-verse";

    private Elements getElements(TagAttribute tagAttribute) {
        if(tagAttribute.attributeKey().isEmpty()) {
            return document.select(tagAttribute.tag());
        }
        if(tagAttribute.attributeValue().isEmpty()) {
            return document.select("%s[%s]".formatted(
                tagAttribute.tag(),
                tagAttribute.attributeKey()
            ));
        }
        return document.select("%s[%s=%s]".formatted(
            tagAttribute.tag(),
            tagAttribute.attributeKey(),
            tagAttribute.attributeValue()
        ));
    }

    @Override
    public void checkTotalVerses(List<Integer> chaptersSize, String book, int numberOfChapters){
        getElements(new TagAttribute("div", "class", "chapter")).forEach(chapter -> {
            ChapterInfo chapterInfo = getChapterInfo(chapter);
            if(chapterInfo==null){
                return;
            }
            int chapterNumber = Integer.parseInt(chapterInfo.chapterId);
            if(chaptersSize.get(chapterNumber - 1) == 0){
                logger.error("Chapter {} not found", chapterNumber);
            }
            Elements verses = chapter.select("span.v");
            Integer versesFound = verses.size()+(numberOfChapters==1 ? 0 : 1);
            Integer totalVerses = chaptersSize.get(chapterNumber - 1);
            if(!Objects.equals(totalVerses, versesFound)){
                logger.error("In book {}, chapter {} has {} verses, but should have {}", book, chapterNumber, versesFound, totalVerses);
            }
        });
    }

    @Override
    public void handleUnitedVerses(String dash, String see){
        boolean isChapterEnd = false;
        boolean isFirstVerseOfChapter = false;
        TagAttribute spanClassV = new TagAttribute("span", "class", "v");
        for (Element elementUnited : getElements(spanClassV)){
            isChapterEnd = checkElementUnited(dash, see, elementUnited, isChapterEnd, isFirstVerseOfChapter);
        }
        isChapterEnd = false;
        isFirstVerseOfChapter = true;
        TagAttribute chapterTag = new TagAttribute("span", "class", "c-drop");
        Elements chapters = getElements(chapterTag);
        Elements firstVerses = new Elements();
        for (Element chapter : chapters) {
            Element nextElementSibling = chapter.nextElementSibling();
            if (nextElementSibling != null) {
                firstVerses.add(nextElementSibling);
            }
        }
        for (Element elementUnited : firstVerses){
            isChapterEnd = checkElementUnited(dash, see, elementUnited, isChapterEnd, isFirstVerseOfChapter);
        }
    }

    private boolean checkElementUnited(String dash, String see, Element elementUnited, boolean isChapterEnd, boolean isFirstVerseOfChapter) {
        Element unitedParent = elementUnited.parent();
        if ((unitedParent == null || !unitedParent.hasAttr(DATA_VERSE)) && !isFirstVerseOfChapter) {
            return isChapterEnd;
        }
        if(isFirstVerseOfChapter){
            unitedParent = elementUnited;
        }
        String dataVerse = unitedParent.attr(DATA_VERSE);
        String[] range = splitVerseRange(dataVerse, dash);
        if (range == null) {
            return isChapterEnd;
        }
        String firstVerse = range[0];
        String finalVerse = range[1];
        ChapterInfo chapterInfo = getChapterInfo(unitedParent);
        if (chapterInfo == null) {
            return isChapterEnd;
        }
        Element firstVerseNumber = elementUnited.selectFirst("span.v");
        if (isFirstVerseOfChapter) {
            firstVerseNumber = elementUnited.previousElementSibling();
        }else if(firstVerseNumber!=null){
            firstVerseNumber.html("<b>%s</b>".formatted(firstVerse));
        }
        if (firstVerseNumber == null){
            return isChapterEnd;
        }
        addFirstVerseReference(firstVerseNumber, firstVerse, chapterInfo.chapterId, finalVerse);
        Element afterFinalVerse = findVerseAfterFinalVerse(chapterInfo, finalVerse);
        if (afterFinalVerse == null) {
            // the union is in the last verse of the chapter
            afterFinalVerse = chapterInfo.chapterDiv().select("span[%s^=%d]".formatted(
                    DATA_VERSE, Integer.parseInt(firstVerse)
            )).last();
            isChapterEnd =true;
        }
        StringBuilder sb = getVerseReferences(see, firstVerse, finalVerse, chapterInfo.chapterId);
        if (sb == null) {
            isChapterEnd = false;
            logger.error("Error parsing verse references for {} {}:{}-{}", see, chapterInfo.chapterId, firstVerse, finalVerse);
            return isChapterEnd;
        }
        addVerseReferences(elementUnited, afterFinalVerse, sb, isChapterEnd, isFirstVerseOfChapter);
        isChapterEnd = false;
        return isChapterEnd;
    }

    private String[] splitVerseRange(String dataVerse, String separator) {
        if (dataVerse == null || separator == null) return null;
        int idx = dataVerse.indexOf(separator);
        if (idx <= 0 || idx >= dataVerse.length() - separator.length()) return null;
        String first = dataVerse.substring(0, idx).trim();
        String last = dataVerse.substring(idx + separator.length()).trim();
        if (first.isEmpty() || last.isEmpty()) return null;
        return new String[] { first, last };
    }

    private Element findVerseAfterFinalVerse(ChapterInfo chapterInfo, String finalVerse) {
        try {
            int next = Integer.parseInt(finalVerse) + 1;
            Elements matches = chapterInfo.chapterDiv().select("span[%s^=%d]".formatted(
                    DATA_VERSE, next
            ));
            if (matches.isEmpty()) {
                return null;
            }
            return matches.first();
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private ChapterInfo getChapterInfo(Element elementUnitedParent) {
        Element chapterDiv = elementUnitedParent;
        String chapterId = "";
        while (chapterDiv != null) {
            if (chapterDiv.tagName().equalsIgnoreCase("div") && chapterDiv.hasClass("chapter")) {
                chapterId = chapterDiv.id();
                break;
            }
            chapterDiv = chapterDiv.parent();
        }
        if(chapterDiv==null){
            return null;
        }
        chapterId = chapterId.replaceAll("\\D+", "");
        if (chapterId.isEmpty()) {
            return null;
        }
        return new ChapterInfo(chapterDiv, chapterId);
    }

    private record ChapterInfo(Element chapterDiv, String chapterId) {
    }

    private void addFirstVerseReference(Element firstVerseNumberElement, String firstVerse, String chapterNumberOnly, String finalVerse) {
        firstVerseNumberElement.append(" \uF850%s:%s-%s\uF851 ".formatted(
                chapterNumberOnly,
                firstVerse,
                finalVerse
        ));
    }

    private void addVerseReferences(Element elementUnited, Element verseAfterFinalVerseElement, StringBuilder sb, boolean isInChapterEnd, boolean isFirstVerseOfChapter) {
        Element elementUnitedParent = elementUnited.parent();
        if(elementUnitedParent==null){
            return;
        }
        Element grandParentElementUnited = elementUnitedParent.parent();
        if(grandParentElementUnited==null){
            return;
        }
        Element grandParentVerseAfterFinalVerseElement = verseAfterFinalVerseElement.parent();
        if(grandParentVerseAfterFinalVerseElement==null){
            return;
        }
        if(isInChapterEnd){
            verseAfterFinalVerseElement.append(sb.toString());
        }else if(isFirstVerseOfChapter || grandParentElementUnited.equals(grandParentVerseAfterFinalVerseElement)){
            verseAfterFinalVerseElement.prepend(sb.toString());
        }else{
            grandParentElementUnited.append(sb.toString());
        }
    }

    private StringBuilder getVerseReferences(String see, String firstVerse, String finalVerse, String chapterNumberOnly) {
        int start, end;
        try {
            start = Integer.parseInt(firstVerse);
            end = Integer.parseInt(finalVerse);
        } catch (NumberFormatException e) {
            return null;
        }
        if (start > end) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start+1; i <= end; i++) {
            sb.append(" <span class=\"v\"> <b>").append(i).append("</b> </span> ")
                    .append("\uF850").append(see).append(" ")
                    .append(chapterNumberOnly).append(':').append(firstVerse)
                    .append("\uF851 ");
        }
        return sb;
    }

    @Override
    public void readHtml(String html) {
        this.document = Jsoup.parse(html);
    }

    @Override
    public String getHtml() {
        return document.outerHtml();
    }

    @Override
    public void removeTag(TagAttribute tagAttribute) {
        getElements(tagAttribute).remove();
    }

    @Override
    public void addSpaceAfterText(TagAttribute tagAttribute) {
        for (Element element : getElements(tagAttribute)) {
            Element newElement = new Element(element.tagName());
            element.attributes().forEach(elementAttr -> newElement.attr(elementAttr.getKey(), elementAttr.getValue()));
            newElement.html(element.html()+" ");
            element.replaceWith(newElement);
        }
    }

    @Override
    public void addTextBefore(TagAttribute tagAttribute, String text) {
        for (Element element : getElements(tagAttribute)) {
            Element newElement = new Element(element.tagName());
            element.attributes().forEach(elementAttr -> newElement.attr(elementAttr.getKey(), elementAttr.getValue()));
            newElement.html(text + element.html());
            element.replaceWith(newElement);
        }
    }

    @Override
    public void addSiblingBefore(TagAttribute tagAttribute, String text) {
        for (Element element : getElements(tagAttribute)) {
            element.before("<span>%s</span>".formatted(text));
        }
    }

    @Override
    public void changeTag(TagAttribute tagAttribute, String newTag) {
        changeTagAndMaybeText(tagAttribute, newTag, null);
    }

    private void changeTagAndMaybeText(TagAttribute tagAttribute, String newTag, String text) {
        for (Element element : getElements(tagAttribute)) {
            Element newElement = new Element(newTag);
            element.attributes().forEach(elementAttr -> newElement.attr(elementAttr.getKey(), elementAttr.getValue()));
            newElement.html(Objects.requireNonNullElseGet(text, element::html));
            element.replaceWith(newElement);
        }
    }   

    @Override
    public void changeTagAndText(TagAttribute tagAttribute, String newTag, String text) {
        changeTagAndMaybeText(tagAttribute, newTag, text);
    }

    @Override
    public void replace(String string, String replacement) {
        for (Element element : document.getAllElements()) {
            String newHtml = element.html().replace(string, replacement);
            element.html(newHtml);
            if(!element.hasText()){
                element.remove();
            }
        }        
    }

    @Override
    public void addTagAfter(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text) {
        addTextAfterOrBeforeElement(tagAttribute, newTagAttribute, text, true);
    }
    
    @Override
    public void addTagBefore(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text) {
        addTextAfterOrBeforeElement(tagAttribute, newTagAttribute, text, false);
    }

    private void addTextAfterOrBeforeElement(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text, boolean isAfter) {
        for (Element element : getElements(tagAttribute)) {
            Element textElement = new Element(newTagAttribute.tag());
            if(!newTagAttribute.attributeKey().isEmpty() && !newTagAttribute.attributeValue().isEmpty()) {
                textElement.attr(newTagAttribute.attributeKey(), newTagAttribute.attributeValue());
            }
            if(!text.isBlank()){
                textElement.text(text);
            }
            if(isAfter){
                element.after(textElement);
            }else{
                element.before(textElement);
            }
        }
    }

    @Override
    public void addChildIfNextTagIsTheSame(TagAttribute parentTag, TagAttribute childTag){
        for (Element element : getElements(parentTag)) {
            Element childElement = new Element(childTag.tag());
            if(!childTag.attributeKey().isEmpty() && !childTag.attributeValue().isEmpty()) {
                childElement.attr(childTag.attributeKey(), childTag.attributeValue());
            }
            // check if the next tag is the same, and if is the same, then add the child
            Element nextElement = element.nextElementSibling();
            if(elementIsEquals(element, nextElement)){
                element.appendChild(childElement);
            }
        }
    }

    private boolean elementIsEquals(Element element, Element nextElement) {
        if (nextElement == null || element == null) return false;
        if (!element.tagName().equals(nextElement.tagName())) return false;
        if (element.attributes().size() != nextElement.attributes().size()) return false;
        return element.attributes().asList().stream()
                .allMatch(attr -> attr.getValue().equals(nextElement.attr(attr.getKey())));
    }

    @Override
    public void changeTagIfNextIsAndAddTag(TagAttribute tagAttribute, TagAttribute nextTag, TagAttribute newTagAttribute){
        // if after a tagAttribute there is nextTag, then
        // change the tagAttribute to nextTag
        // and add a newTagAttribute as the last child of this tagAttribute
        Element nextElement = createElement(nextTag);
        for (Element element : getElements(tagAttribute)) {
            Element nextElementSibling = element.nextElementSibling();
            if(elementIsEquals(nextElementSibling, nextElement)){
                element.tagName(nextTag.tag());
                Element newElement = createElement(newTagAttribute);
                element.appendChild(newElement);
            }
        }
    }

    private Element createElement(TagAttribute tagAttribute) {
        Element element = new Element(tagAttribute.tag());
        if(!tagAttribute.attributeKey().isEmpty() && !tagAttribute.attributeValue().isEmpty()) {
            element.attr(tagAttribute.attributeKey(), tagAttribute.attributeValue());
        }
        return element;
    }

    @Override
    public void prependTextToNextTagIfNotSameTagAndIfIsInList(List<TagAttribute> tagList, String string, List<TagAttribute> listToPrepend){
        tagList.forEach(tag -> prependTextToNextTagIfNotExistsInTagList(tag, listToPrepend, string));
    }

    @Override
    public void addTextBeforeIfSomeChecksTrue(TagAttribute tagToSearch, String classToCheck, TagAttribute subTagToCheckAttribute, TagAttribute subTagToCheckClass, String textToAdd) {
        for (Element element : getElements(tagToSearch)) {
            Element lastElement = element.previousElementSibling();
            if(lastElement != null && !lastElement.hasClass(classToCheck) && element.hasClass(classToCheck)){
                Element firstChild = element.firstElementChild();
                if(firstChild!= null && firstChild.tagName().equals(subTagToCheckAttribute.tag()) && firstChild.hasAttr(subTagToCheckAttribute.attributeKey())){
                    Element firstGrandchild = firstChild.firstElementChild();
                    if(elementIsEquals(firstGrandchild, createElement(subTagToCheckClass))){
                        element.prependText(textToAdd);
                    }
                }

            }
        }
    }

    @Override
    public void surroundTextWith(TagAttribute tagAttribute, String prefix, String suffix) {
        for (Element element : getElements(tagAttribute)) {
            element.html(prefix + element.html() + suffix);
        }
    }

    @Override
    public void makeTextBold(TagAttribute tagAttribute) {
        for (Element element : getElements(tagAttribute)) {
            element.html("<b>" + element.html() + "</b>");
        }
    }

    @Override
    public List<String> getHtmlTags(TagAttribute tagAttribute) {
        return getElements(tagAttribute).stream()
                .map(Element::outerHtml)
                .toList();
    }

    @Override
    public List<String> getTextTags(TagAttribute tagAttribute) {
        return getElements(tagAttribute).stream()
                .map(Element::text)
                .toList();
    }

    @Override
    public String getTagText(TagAttribute chapterTag) {
        Elements elements = getElements(chapterTag);
        Element element = elements.first();
        if (element == null) {return "";}
        return element.text();
    }

    @Override
    public void addHtmlAtEnd(String html) {
        String div = document.outerHtml();
        document.empty();
        document.body().append(div);
        document.body().append(html);
    }

    @Override
    public void prependTextToNextTagIfNotSameTag(List<TagAttribute> tagList, String string) {
        tagList.forEach(tag -> prependTextToNextTagIfNotExistsInTagList(tag, tagList, string));
    }

    private void prependTextToNextTagIfNotExistsInTagList(TagAttribute tag,List<TagAttribute> tagList, String string) {
        getElements(tag).forEach(element -> {
            Element nextTag = element.nextElementSibling();
            if(tagIsNotInTagList(tagList, nextTag)) {
                Element child = nextTag.firstElementChild();
                if(child != null){
                    child.prependText(string);
                }
            }
        });
    }

    private boolean tagIsNotInTagList(List<TagAttribute> tagList, Element nextTag) {
        return nextTag != null && tagList.stream().noneMatch(t -> 
                t.tag().equals(nextTag.tagName()) &&
                nextTag.hasAttr(t.attributeKey()) &&
                t.attributeValue().equals(nextTag.attr(t.attributeKey()))
        );
    }


    @Override
    public void removeStyleFromTags(String tagName) {
        document.select("[style]").forEach(element -> element.removeAttr("style"));
    }

}
