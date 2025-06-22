package com.github.edurbs;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.application.FormatBibleUseCase;
import com.github.edurbs.infrastructure.SeleniumBookExtractor;

public class Main {
    public static void main(String[] args) {
        String chromePath = args.length > 0 ? args[0] : "/home/eduardo/chrome-linux64/chrome";
        String chromeDriverPath = args.length > 1 ? args[1] : "/home/eduardo/chromedriver-linux64/chromedriver";
        String url = args.length > 2 ? args[2] : "https://scriptureearth.org/data/xav/sab/xav/#/text";
        Extractor extractor = new SeleniumBookExtractor(chromePath, chromeDriverPath, url);
        FormatBibleUseCase formatBibleUseCase = new FormatBibleUseCase(extractor);
        formatBibleUseCase.execute();
        
    }
}
