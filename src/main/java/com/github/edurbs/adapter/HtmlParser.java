package com.github.edurbs.adapter;

import java.util.List;

import com.github.edurbs.application.TagAttribute;

public interface HtmlParser {
    
    void readHtml(String html);
    String getHtml();
    void removeTag(TagAttribute tagAttribute);
    void changeTag(TagAttribute tagAttribute, String newTag);
    void changeTagAndText(TagAttribute tagAttribute, String newTag, String text);
    void addTagAfter(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text);
    void addTagBefore(TagAttribute tagAttribute, TagAttribute newTagAttribute, String text);
    void makeTextBold(TagAttribute tagAttribute);
    void addSpaceAfterText(TagAttribute tagAttribute);
    void replace(String string, String replacement);
    void surroundTextWith(TagAttribute tagAttribute, String prefix, String suffix);
    void addTextBefore(TagAttribute tagAttribute, String text);
    List<String> getHtmlTags(TagAttribute tagAttribute);
    List<String> getTextTags(TagAttribute tagAttribute);
    String getTagText(TagAttribute chapterTag);
    void addHtmlAtEnd(String html);
    void prependTextToNextTagIfNotSameTag(List<TagAttribute> tagList, String string);
    void removeStyleFromTags(String tagName);

}
