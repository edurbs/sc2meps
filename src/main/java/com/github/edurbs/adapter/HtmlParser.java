package com.github.edurbs.adapter;

public interface HtmlParser {
    
    void readHtml(String html);
    String getHtml();
    void removeElementByTagAndClass(String tag, String elementClass);
    void changeElementByTagAndProperty(String tag, String property, String newTag);
    void changeElementByClassAndProperty(String tag, String elementClass, String newTag);
    void removeElementByTagAndProperty(String tag, String property);
    void addTextAfterElementByTagAndClass(String tag, String elementClass, String newTag, String newClass, String text);
    void makeElementBoldByTagAndClass(String tag, String elementClass);
    void addSpaceAfterElementByTagAndClass(String tag, String elementClass);
    void replace(String string, String replacement);
    void surroundElementByTagAndClassWithText(String tag, String elementClass, String prefix, String suffix);

}
