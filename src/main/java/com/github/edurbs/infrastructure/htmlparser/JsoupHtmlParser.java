package com.github.edurbs.infrastructure.htmlparser;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.HtmlParser;
import com.github.edurbs.application.TagAttribute;

public class JsoupHtmlParser implements HtmlParser {
    private Document document;
    private static final Logger logger = LoggerFactory.getLogger(JsoupHtmlParser.class);

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
            //newElement.html(element.html()+"&nbsp;");
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
    public void changeTag(TagAttribute tagAttribute, String newTag) {
        changeTagAndMaybeText(tagAttribute, newTag, null);
    }

    private void changeTagAndMaybeText(TagAttribute tagAttribute, String newTag, String text) {
        for (Element element : getElements(tagAttribute)) {
            Element newElement = new Element(newTag);
            element.attributes().forEach(elementAttr -> newElement.attr(elementAttr.getKey(), elementAttr.getValue()));
            if (text != null) {
                newElement.html(text);
            } else {
                newElement.html(element.html());
            }
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
            Element textElement = new Element(newTagAttribute.tag())
                    .attr(newTagAttribute.attributeKey(), newTagAttribute.attributeValue())
                    .text(text);
            if(isAfter){
                element.after(textElement);
            }else{
                element.before(textElement);
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
        if (elements.isEmpty()) {
            return "";
        }
        return elements.first().text();
    }

    @Override
    public void addHtmlAtEnd(String html) {
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
                Element elementString = new Element("span").text(string);
                nextTag.prependChild(elementString);
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
