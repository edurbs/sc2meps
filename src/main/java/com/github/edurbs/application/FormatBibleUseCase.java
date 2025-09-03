package com.github.edurbs.application;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.adapter.FormatBible;
import com.github.edurbs.adapter.FormatBook;
import com.github.edurbs.adapter.HtmlArchiver;
import com.github.edurbs.domain.Book;
import com.github.edurbs.domain.ScriptureEarthBookName;

public class FormatBibleUseCase implements FormatBible {
    private static final Logger logger = LoggerFactory.getLogger(FormatBibleUseCase.class);
    private final Extractor extractor;
    private final HtmlArchiver htmlHandler;
    private final FormatBook formatBook;

    public FormatBibleUseCase(Extractor extractor, HtmlArchiver htmlHandler, FormatBook formatBook) {
        this.extractor = extractor;
        this.htmlHandler = htmlHandler;
        this.formatBook = formatBook;
    }

    public void execute() {
        for (ScriptureEarthBookName scriptureEarthBook : ScriptureEarthBookName.values()) {
            String bookNameMepsFormat = scriptureEarthBook.getMepsName().getMepsFormat();
            logger.info("Started book: {}", bookNameMepsFormat);
            String bookContent = formatBook.execute(scriptureEarthBook);
            Book book = new Book(scriptureEarthBook, bookContent);
            logger.info("Formatted book: {}", bookNameMepsFormat);
        }
    }

}
