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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edurbs.adapter.Extractor;
import com.github.edurbs.adapter.HtmlArchiver;

public class SeleniumBookExtractor implements Extractor {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumBookExtractor.class);
    private final String chromePath;
    private final String chromeDriverPath;
    private final String url;
    private String bookCodeName;
    private Integer chapters;
    private StringBuilder allChaptersHtml = new StringBuilder();
    private final HtmlArchiver htmlHandler;

    public SeleniumBookExtractor(String chromePath, String chromeDriverPath, HtmlArchiver htmlHandler, String url) {
        this.chromePath = chromePath;
        this.chromeDriverPath = chromeDriverPath;
        this.htmlHandler = htmlHandler;
        this.url = url;
    }

    @Override
    public String extractBook(String bookCodeName, Integer chapters) {
        this.bookCodeName = bookCodeName;
        this.chapters = chapters;
        logger.info("Starting SeleniumExtractor...");
        WebDriver driver = getWebDriver();
        allChaptersHtml.setLength(0); 
        try {
            WebDriverWait wait = openPage(url, driver);
            addHeader(allChaptersHtml);
            for (int chapterNum = 1; chapterNum <= chapters; chapterNum++) {
                getChapter(driver, wait, allChaptersHtml, chapterNum);
            }
            addFooter(allChaptersHtml);
            saveToFile(allChaptersHtml);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return allChaptersHtml.toString();
    }

    private WebDriverWait openPage(String url, WebDriver driver) {
        logger.info("Navigating to: {}", url);
        driver.get(url);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        logger.info("Waiting for page to load...");
        wait.until(webDriver -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                .equals("complete"));
        return wait;
    }

    private void getChapter(WebDriver driver, WebDriverWait wait, StringBuilder allChaptersHtml, int chapterNum) {
        try {
            logger.info("\n=== Processing Chapter {} of {} ===", chapterNum, this.chapters);
            clickMenuToggle(wait);
            clickBook(wait);
            clickChapterLink(wait, chapterNum);
            String chapterContent = getChapterContent(driver, wait, chapterNum);
            allChaptersHtml.append(String.format("<div class='chapter' id='chapter-%d'>\n", chapterNum));
            allChaptersHtml.append(chapterContent);
            allChaptersHtml.append("\n</div>\n");
            logger.info("Added Chapter {}", chapterNum);
        } catch (Exception e) {
            logger.error("Error processing Chapter {}: {}", chapterNum, e.getMessage());
            e.printStackTrace();
        }
    }

    private String getChapterContent(WebDriver driver, WebDriverWait wait, int chapterNum) throws InterruptedException {
        logger.info("Waiting for content to load...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content:not(:empty)")));
        int delay = 2000 + (100 * (chapterNum / 10));
        logger.info("Waiting {}ms for content to stabilize...", delay);
        Thread.sleep(delay);
        logger.info("Extracting chapter content...");
        String chapterContent = (String) ((JavascriptExecutor) driver)
                .executeScript("return document.getElementById('content').innerHTML;");
        if (chapterContent == null || chapterContent.trim().isEmpty()) {
            logger.info("Warning: Chapter content is empty! Trying alternative method...");
            chapterContent = (String) ((JavascriptExecutor) driver).executeScript("return document.body.innerHTML;");
        }
        return chapterContent;
    }

    private void clickChapterLink(WebDriverWait wait, int chapterNum) {
        if(this.chapters == 1) {
            logger.info("Only one chapter available, skipping chapter selector click.");
            return;
        }
        logger.info("Clicking chapter selector: ");
        String chapterSelector = String.format("[id='%d']", chapterNum);
        WebElement chapterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(chapterSelector)));
        chapterLink.click();
    }

    private void clickBook(WebDriverWait wait) throws InterruptedException {
        logger.info("Clicking book {}...", this.bookCodeName);
        WebElement genBook = wait.until(ExpectedConditions.elementToBeClickable(By.id(this.bookCodeName)));
        genBook.click();
        Thread.sleep(1000);
    }

    private void clickMenuToggle(WebDriverWait wait) {
        logger.info("Clicking menu...");
        WebElement menuToggle = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".whitespace-nowrap")));
        menuToggle.click();
    }

    private void saveToFile(StringBuilder allChaptersHtml) {
        logger.info("Saving complete book to file...");
        this.htmlHandler.saveHtmlToFile(allChaptersHtml.toString(), this.bookCodeName);
        logger.info("Book saved successfully.");
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
        allChaptersHtml.append(".chapter h2 { color: #2980b9; border-bottom: 1px solid #eee; padding-bottom: 5px; }\n");
        allChaptersHtml.append("</style>\n");
        // allChaptersHtml
        //         .append("""
        //                 <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/0.BD-KWcsM.css">
        //                 <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-app.css">
        //                 <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/BookSelector.mqkoy7lk.css">
        //                 <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/_app/immutable/assets/19.oxaumFWT.css">
        //                 <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-app.css">
        //                 <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/styles/sab-bc-C01.css">
        //                 <link rel="stylesheet" href="https://www.scriptureearth.org/data/xav/sab/xav/override-sab.css">
        //                 """);
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
