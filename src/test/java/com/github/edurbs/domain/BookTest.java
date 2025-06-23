package com.github.edurbs.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BookTest {
    @Test
    void testHtml() {
        ScriptureEarthBookName scriptureEarthBookName = ScriptureEarthBookName.BOOK_REV;
        String htmlContent = "<html><body>Test Content</body></html>";
        Book book = new Book(scriptureEarthBookName, htmlContent);
        assertEquals(htmlContent, book.html());
        assertEquals(scriptureEarthBookName, book.scriptureEarthBookName());
    }

}
