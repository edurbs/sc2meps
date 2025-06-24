package com.github.edurbs.adapter;

public interface HtmlParser {

    void removeElementByTagAndClass(String tag, String elementClass);
    void changeElementByTagAndProperty(String tag, String property, String newTag);
    void removeElementByTagAndProperty(String tag, String property);
    void makeElementBoldByTagAndClass(String tag, String elementClass);
    void addSpaceAfterElementByTagAndClass(String tag, String elementClass);

    void replace(String string, String replacement);

    void readHtml(String html);
    String getHtml();

}
