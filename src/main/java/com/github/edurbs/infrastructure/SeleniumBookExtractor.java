package com.github.edurbs.infrastructure;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.edurbs.application.ExtractBookUseCase;

public class SeleniumBookExtractor implements ExtractBookUseCase {
    private final String chromePath;
    private final String chromeDriverPath;
    private final String bookCodeName;
    private final Integer chapters;

    public SeleniumBookExtractor(String chromePath, String chromeDriverPath, String bookCodeName, Integer chapters) {
        this.chromePath = chromePath;
        this.chromeDriverPath = chromeDriverPath;
        this.bookCodeName = bookCodeName;
        this.chapters = chapters;
    }

    @Override
    public void extractBook() {
        System.out.println("Starting SeleniumGenesisExtractor...");
        WebDriver driver = getWebDriver();
        try {
            WebDriverWait wait = openPage(driver);
            StringBuilder allChaptersHtml = new StringBuilder();
            addHeader(allChaptersHtml);
            for (int chapterNum = 1; chapterNum <= this.chapters; chapterNum++) {
                getChapter(driver, wait, allChaptersHtml, chapterNum);
            }
            addFooter(allChaptersHtml);
            saveToFile(allChaptersHtml);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    

    private WebDriverWait openPage(WebDriver driver) {
        String url = "https://scriptureearth.org/data/xav/sab/xav/#/text";
        System.out.println("Navigating to: " + url);
        driver.get(url);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        System.out.println("Waiting for page to load...");
        wait.until(webDriver -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                .equals("complete"));
        return wait;
    }

    private void getChapter(WebDriver driver, WebDriverWait wait, StringBuilder allChaptersHtml, int chapterNum) {
        try {
            System.out.println("\n=== Processing Chapter %s of %s ===".formatted(chapterNum, this.chapters));
            clickMenuToggle(wait);
            clickBook(wait);
            clickChapterLink(wait, chapterNum);
            String chapterContent = getChapterContent(driver, wait, chapterNum);
            allChaptersHtml.append(String.format("<div class='chapter' id='chapter-%d'>\n", chapterNum));
            allChaptersHtml.append(chapterContent);
            allChaptersHtml.append("\n</div>\n");
            System.out.println("Added Chapter " + chapterNum);
        } catch (Exception e) {
            System.err.println("Error processing Chapter " + chapterNum + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getChapterContent(WebDriver driver, WebDriverWait wait, int chapterNum) throws InterruptedException {
        System.out.println("Waiting for content to load...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content:not(:empty)")));
        int delay = 2000 + (100 * (chapterNum / 10));
        System.out.println("Waiting " + delay + "ms for content to stabilize...");
        Thread.sleep(delay);
        System.out.println("Extracting chapter content...");
        String chapterContent = (String) ((JavascriptExecutor) driver)
                .executeScript("return document.getElementById('content').innerHTML;");
        if (chapterContent == null || chapterContent.trim().isEmpty()) {
            System.out.println("Warning: Chapter content is empty! Trying alternative method...");
            chapterContent = (String) ((JavascriptExecutor) driver)
                    .executeScript("return document.body.innerHTML;");
        }
        return chapterContent;
    }

    private void clickChapterLink(WebDriverWait wait, int chapterNum) {
        System.out.println("Clicking chapter selector: ");
        String chapterSelector = String.format("[id='%d']", chapterNum);
        WebElement chapterLink = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(chapterSelector)));
        chapterLink.click();
    }

    private void clickBook(WebDriverWait wait) throws InterruptedException {
        System.out.println("Clicking book...");
        WebElement genBook = wait.until(ExpectedConditions.elementToBeClickable(By.id(this.bookCodeName.substring(1))));
        genBook.click();
        Thread.sleep(1000);
    }

    private void clickMenuToggle(WebDriverWait wait) {
        System.out.println("Clicking menu...");
        WebElement menuToggle = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".whitespace-nowrap")));
        menuToggle.click();
    }

    private void saveToFile(StringBuilder allChaptersHtml) {
        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("genesis_complete_%s.html", timestamp);
            java.nio.file.Files.writeString(java.nio.file.Path.of(filename), allChaptersHtml.toString());
            System.out.println("\nAll 50 chapters saved to: " + filename);
            java.nio.file.Files.writeString(java.nio.file.Path.of("genesis_complete_raw.html"),
                    allChaptersHtml.toString());
        } catch (Exception e) {
            System.err.println("Failed to save complete book: " + e.getMessage());
        }
    }

    private void addFooter(StringBuilder allChaptersHtml) {
        allChaptersHtml.append("</body></html>");
    }

    private void addHeader(StringBuilder allChaptersHtml) {
        allChaptersHtml.append("<!DOCTYPE html>\n");
        allChaptersHtml.append("<html><head>\n");
        allChaptersHtml.append("<meta charset=\"UTF-8\">\n");
        allChaptersHtml.append("<style>\n");
        allChaptersHtml.append(
                "body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; max-width: 900px; margin: 0 auto; padding: 20px; }\n");
        allChaptersHtml.append("h1 { text-align: center; color: #2c3e50; margin-bottom: 30px; }\n");
        allChaptersHtml
                .append(".chapter { margin-bottom: 40px; border-bottom: 1px solid #eee; padding-bottom: 20px; }\n");
        allChaptersHtml
                .append(".chapter h2 { color: #2980b9; border-bottom: 1px solid #eee; padding-bottom: 5px; }\n");
        allChaptersHtml.append("</style>\n");
        allChaptersHtml.append("""                    
                <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/0.BD-KWcsM.css">
                <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-app.css">
                <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/BookSelector.mqkoy7lk.css">
                <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/19.oxaumFWT.css">
                <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-app.css">
                <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-bc-C01.css">
                <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/override-sab.css">
                """);
        allChaptersHtml.append("</head><body>\n");
    }


    private WebDriver getWebDriver() {
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        System.setProperty("webdriver.chrome.binary", chromePath);
        ChromeOptions options = new ChromeOptions();
        options.setBinary(chromePath);
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }
}
