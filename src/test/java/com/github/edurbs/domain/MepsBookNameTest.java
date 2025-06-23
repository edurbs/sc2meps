package com.github.edurbs.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MepsBookNameTest {
    @Test
    void testGetMepsFormat() {
        assertEquals("66_REV", MepsBookName.BOOK_66_REV.getMepsFormat());
    }

    @Test
    void testGetNumberOfChapters() {
        assertEquals(1, MepsBookName.BOOK_65_JUD.getNumberOfChapters());
    }

    @Test
    void testGetNumberOfScriptures() {
        assertEquals(25, MepsBookName.BOOK_65_JUD.getNumberOfScriptures(1));
    }


    @Test
    void testValueOf() {

    }

    @Test
    void testValues() {

    }
}
