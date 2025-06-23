package com.github.edurbs.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.domain.Book;
import com.github.edurbs.domain.MepsBookName;
import com.github.edurbs.domain.ScriptureEarthBookName;

public class FormatBibleUseCase {

    private static final Logger logger = LoggerFactory.getLogger(FormatBibleUseCase.class);

    private final Extractor extractor;
    private final HtmlHandler htmlHandler;

    public FormatBibleUseCase(Extractor extractor, HtmlHandler htmlHandler) {
        this.extractor = extractor;
        this.htmlHandler = htmlHandler;
    }

    public void execute() {
        for (ScriptureEarthBookName scriptureEarthBook : ScriptureEarthBookName.values()) {
            String scriptureEarthBookName = scriptureEarthBook.getName();
            MepsBookName mepsBookName = scriptureEarthBook.getMepsName();
            Integer chapters = mepsBookName.getNumberOfChapters();
            FormatBookUseCase formatBookUseCase = new FormatBookUseCase(extractor, scriptureEarthBookName, chapters, htmlHandler);
            Book book = new Book(mepsBookName, formatBookUseCase.execute());
            showStatus(book);
        }
    }

    private void showStatus(Book book) {
        logger.info("Formatted book: {}", book.mepsBookName().name());
    }

}
