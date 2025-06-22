package com.github.edurbs.application;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.domain.ScriptureEarthBookName;

public class FormatBibleUseCase {

    private final Extractor extractor;

    public FormatBibleUseCase(Extractor extractor) {
        this.extractor = extractor;
    }

    public void execute() {
        String book = ScriptureEarthBookName.BOOK_GEN.getName();
        Integer chapters = ScriptureEarthBookName.BOOK_GEN.getMepsBookName().getNumberOfChapters();
        FormatBookUseCase formatBookUseCase = new FormatBookUseCase(extractor, book, chapters );
        formatBookUseCase.execute();
    }

}
