package com.github.edurbs.application;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.domain.Book;
import com.github.edurbs.domain.MepsBookName;
import com.github.edurbs.domain.ScriptureEarthBookName;

public class FormatBibleUseCase {

    private final Extractor extractor;

    public FormatBibleUseCase(Extractor extractor) {
        this.extractor = extractor;
    }

    public void execute() {
        ScriptureEarthBookName scriptureEarthBook = ScriptureEarthBookName.BOOK_GEN;
        String scriptureEarthBookName = scriptureEarthBook.getName();
        MepsBookName mepsBookName = ScriptureEarthBookName.BOOK_GEN.getMepsBookName();
        Integer chapters = mepsBookName.getNumberOfChapters();
        FormatBookUseCase formatBookUseCase = new FormatBookUseCase(extractor, scriptureEarthBookName, chapters );
        Book genesis = new Book(mepsBookName, formatBookUseCase.execute());
        showStatus(genesis);
    }

    private void showStatus(Book genesis) {
        System.out.println("Formatted book: " + genesis.mepsBookName().name());
        System.out.println("Book content: " + genesis.html().substring(0, Math.min(genesis.html().length(), 100)) + "...");
    }

}
