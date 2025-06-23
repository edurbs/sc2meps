package com.github.edurbs.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ScriptureEarthBookNameTest {
    @Test
    void testGetMepsBookName() {
        assertEquals(MepsBookName.BOOK_66_REV, ScriptureEarthBookName.BOOK_REV.getMepsName());
    }
    @Test
    void testGetName() {
        assertEquals("REV", ScriptureEarthBookName.BOOK_REV.getName());
    }
}
