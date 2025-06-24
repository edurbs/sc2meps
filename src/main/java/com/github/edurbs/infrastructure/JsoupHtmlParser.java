package com.github.edurbs.infrastructure;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.edurbs.adapter.HtmlParser;

public class JsoupHtmlParser implements HtmlParser {
    private Document document;

    @Override
    public void removeElementByTagAndClass(String tag, String elementClass) {
        document.select(tag + "." + elementClass).remove();
    }

    @Override
    public void addSpaceAfterElementByTagAndClass(String tag, String elementClass) {
        Elements elements = document.select(tag + "." + elementClass);
        for (Element element : elements) {
            Element spaceElement = new Element("span").text(" "); // Non-breaking space
            element.after(spaceElement);
        }
    }

    @Override
    public void removeElementByTagAndProperty(String tag, String property) {
        Elements elements = document.select(tag + "[" + property + "]");
        for (Element element : elements) {
            element.remove();
        }
    }

    @Override
    public void changeElementByTagAndProperty(String tag, String property, String newTag) {
        Elements elements = document.select(tag + "[" + property + "]");
        for (Element element : elements) {
            Element newElement = new Element(newTag);
            newElement.html(element.html());
            element.replaceWith(newElement);
        }
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
    public void replace(String string, String replacement) {
        Elements elements = document.getAllElements();
        for (Element element : elements) {
            String newHtml = element.html().replace(string, replacement);
            element.html(newHtml);
            if(!element.hasText()){
                element.remove();
            }
        }        
    }

    @Override
    public void makeElementBoldByTagAndClass(String tag, String elementClass) {
        Elements elements = document.select(tag + "[class]."+"v");
        for (Element element : elements) {
            element.html("<b>" + element.html() + "</b>");
        }
    }

}
