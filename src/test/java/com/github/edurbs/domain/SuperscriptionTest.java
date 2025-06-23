package com.github.edurbs.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SuperscriptionTest {
    @Test
    void testThisChapterHas() {
        assertFalse(Superscription.thisChapterHas(1));
        assertTrue(Superscription.thisChapterHas(3));
    }
}
