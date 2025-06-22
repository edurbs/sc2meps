package com.github.edurbs;

import com.github.edurbs.application.ExtractBookUseCase;
import com.github.edurbs.infrastructure.SeleniumBookExtractor;

public class Main {
    public static void main(String[] args) {
        String chromePath = args.length > 0 ? args[0] : "/home/eduardo/chrome-linux64/chrome";
        String chromeDriverPath = args.length > 1 ? args[1] : "/home/eduardo/chromedriver-linux64/chromedriver";
        ExtractBookUseCase useCase = new SeleniumBookExtractor(chromePath, chromeDriverPath, "#GEN", 3 );
        useCase.extractBook();
    }
}
