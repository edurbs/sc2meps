package com.github.edurbs.adapter;

public interface HtmlParser {

    void removeElementByClass(String elementClass);
    void changeElementByTagAndProperty(String tag, String property, String newTag);
    void makeElementBoldByTagAndClass(String tag, String elementClass);

    void replace(String string, String replacement);

    void readHtml(String html);
    String getHtml();

}
