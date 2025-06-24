package com.github.edurbs.adapter;

import com.github.edurbs.application.TagAttribute;

public interface HtmlParser {
    
    void readHtml(String html);
    String getHtml();
    void removeElement(TagAttribute tagAttribute);
    void changeElement(TagAttribute tagAttribute, String newTag);
    void addTextAfterElement(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text);
    void addTextBeforeElement(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text);
    void makeElementBold(TagAttribute tagAttribute);
    void addSpaceAfterElement(TagAttribute tagAttribute);
    void replace(String string, String replacement);
    void surroundElementWith(TagAttribute tagAttribute, String prefix, String suffix);

}
