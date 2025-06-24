package com.github.edurbs.adapter;

import com.github.edurbs.application.TagAttribute;

public interface HtmlParser {
    
    void readHtml(String html);
    String getHtml();
    void removeTag(TagAttribute tagAttribute);
    void changeTag(TagAttribute tagAttribute, String newTag);
    void addTagAfter(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text);
    void addTagBefore(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text);
    void makeTextBold(TagAttribute tagAttribute);
    void addSpaceAfterText(TagAttribute tagAttribute);
    void replace(String string, String replacement);
    void surroundTextWith(TagAttribute tagAttribute, String prefix, String suffix);
    void addTextBefore(TagAttribute tagAttribute, String text);

}
