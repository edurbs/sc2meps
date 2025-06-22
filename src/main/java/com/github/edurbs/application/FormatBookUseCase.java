package com.github.edurbs.application;

import com.github.edurbs.adapter.Extractor;

public class FormatBookUseCase {
    private final Extractor extractor;
    private String bookCodeName;
    private Integer chapters;
    public FormatBookUseCase(Extractor extractor, String bookCodeName, Integer chapters) {
        this.extractor = extractor;
        this.bookCodeName = bookCodeName;
        this.chapters = chapters;
    }
    public void execute(){
        String bookContent = "";
        java.nio.file.Path bookFilePath = java.nio.file.Paths.get("%s_complete_raw.html".formatted(bookCodeName));
        if (java.nio.file.Files.exists(bookFilePath)) {
            try {
                bookContent = java.nio.file.Files.readString(bookFilePath);
            } catch (java.io.IOException e) {
                System.err.println("Error reading the book file: " + e.getMessage());
            }
        } else{
            extractor.setBookCodeName(bookCodeName);
            extractor.setChapters(chapters);
            extractor.extractBook();
            bookContent = extractor.getBookContent();
        }
        if (bookContent.isEmpty()) {
            System.err.println("Book content is empty. Please check the extraction process. %s".formatted(bookCodeName));
        }
        System.out.println(bookContent.substring(0, Math.min(100, bookContent.length())) + "...");
    }
}
