package com.github.edurbs.adapter;

public interface Extractor {
    void extractBook();
    String getBookContent();
    String getBookCodeName();
    void setBookCodeName(String bookCodeName);
    void setChapters(Integer chapters);
}
