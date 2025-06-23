package com.github.edurbs.domain;

import org.junit.jupiter.api.Test;

class BookTest {
    @Test
    void testHtml() {
        MepsBookName mepsBookName = MepsBookName.BOOK_66_REV;
        String htmlContent = "<html><body>Test Content</body></html>";
        Book book = new Book(mepsBookName, htmlContent);
        
        // Check if the HTML content is set correctly
        assert book.html().equals(htmlContent) : "HTML content does not match";
        
        // Check if the MepsBookName is set correctly
        assert book.mepsBookName().equals(mepsBookName) : "MepsBookName does not match";
    }

}
