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
        for (ScriptureEarthBookName scriptureEarthBook : ScriptureEarthBookName.values()) {
            String scriptureEarthBookName = scriptureEarthBook.getName();
            MepsBookName mepsBookName = scriptureEarthBook.getMepsName();
            // ScriptureEarthBookName.fromString(scriptureEarthBookName).getMepsBookName();
            Integer chapters = mepsBookName.getNumberOfChapters();
            FormatBookUseCase formatBookUseCase = new FormatBookUseCase(extractor, scriptureEarthBookName, chapters );
            Book book = new Book(mepsBookName, formatBookUseCase.execute());
            showStatus(book);
        }
    }

    private void showStatus(Book book) {
        System.out.println("Formatted book: " + book.mepsBookName().name());
        System.out.println("Book content: " + book.html().substring(0, Math.min(book.html().length(), 100)) + "...");
    }

}
