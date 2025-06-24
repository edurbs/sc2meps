package com.github.edurbs.infrastructure.htmlparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.edurbs.adapter.HtmlParser;
import com.github.edurbs.application.TagAttribute;

public class JsoupHtmlParser implements HtmlParser {
    private Document document;

    private Elements getElements(TagAttribute tagAttribute) {
        if(tagAttribute.attributeValue().isEmpty()) {
            return document.select("%s[%s]".formatted(
                tagAttribute.tag(),
                tagAttribute.attributeKey()
            ));
        }
        return document.select("%s[%s].%s".formatted(
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
    public void removeElement(TagAttribute tagAttribute) {
        getElements(tagAttribute).remove();
    }

    @Override
    public void addSpaceAfterElement(TagAttribute tagAttribute) {
        for (Element element : getElements(tagAttribute)) {
            Element newElement = new Element(element.tagName());
            element.attributes().forEach(elementAttr -> newElement.attr(elementAttr.getKey(), elementAttr.getValue()));
            newElement.html(element.html()+"&nbsp;");
            element.replaceWith(newElement);
        }
    }

    @Override
    public void changeElement(TagAttribute tagAttribute, String newTag) {
        for (Element element : getElements(tagAttribute)) {
            Element newElement = new Element(newTag);
            element.attributes().forEach(elementAttr -> newElement.attr(elementAttr.getKey(), elementAttr.getValue()));
            newElement.html(element.html());
            element.replaceWith(newElement);
        }
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
    public void addTextAfterElement(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text) {
        for (Element element :  getElements(tagAttribute)) {
            Element textElement = new Element(newTagAttribute.tag())
                    .attr(newTagAttribute.attributeKey(), newTagAttribute.attributeValue())
                    .text(text);
            element.after(textElement);
        }
    }

    @Override
    public void surroundElementWith(TagAttribute tagAttribute, String prefix, String suffix) {
        for (Element element : getElements(tagAttribute)) {
            element.html(prefix + element.html() + suffix);
        }
    }

    @Override
    public void makeElementBold(TagAttribute tagAttribute) {
        for (Element element : getElements(tagAttribute)) {
            element.html("<b>" + element.html() + "</b>");
        }
    }

}
