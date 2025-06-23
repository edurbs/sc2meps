package com.github.edurbs;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.adapter.FormatBible;
import com.github.edurbs.adapter.FormatBook;
import com.github.edurbs.adapter.HtmlArchiver;
import com.github.edurbs.adapter.HtmlParser;
import com.github.edurbs.application.FormatBibleUseCase;
import com.github.edurbs.application.FormatBookUseCase;
import com.github.edurbs.infrastructure.HtmlFileArchiver;
import com.github.edurbs.infrastructure.JsoupHtmlParser;
import com.github.edurbs.infrastructure.SeleniumBookExtractor;

public class Main {
    public static void main(String[] args) {
        String htmlPath = "html/";
        HtmlArchiver htmlArchiver = new HtmlFileArchiver(htmlPath);
        String chromePath = args.length > 0 ? args[0] : "/home/eduardo/chrome-linux64/chrome";
        String chromeDriverPath = args.length > 1 ? args[1] : "/home/eduardo/chromedriver-linux64/chromedriver";
        String url = args.length > 2 ? args[2] : "https://scriptureearth.org/data/xav/sab/xav/#/text";
        Extractor extractor = new SeleniumBookExtractor(chromePath, chromeDriverPath, htmlArchiver, url);
        HtmlParser htmlParser = new JsoupHtmlParser(); 
        FormatBook formatBook = new FormatBookUseCase(extractor, htmlArchiver, htmlParser);
        FormatBible formatBible = new FormatBibleUseCase(extractor, htmlArchiver, formatBook);
        formatBible.execute();
        
    }
}
