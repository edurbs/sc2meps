package com.github.edurbs.infrastructure;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.github.edurbs.adapter.HtmlParser;

public class JsoupHtmlParser implements HtmlParser {

    @Override
    public String removeDiv(String html, String divClass) {
        Document document = parseHtml(html);
        document.select("div." + divClass).remove();
        return document.html();
    }

    private Document parseHtml(String html) {
        return Jsoup.parse(html);
    }

}
