package com.github.edurbs;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // Set the paths for Chrome and ChromeDriver
        String chromePath = "/home/eduardo/chrome-linux64/chrome";
        String chromeDriverPath = "/home/eduardo/chromedriver-linux64/chromedriver";
        
        // Set system properties
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        System.setProperty("webdriver.chrome.binary", chromePath);
        
        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.setBinary(chromePath);
        options.addArguments("--headless=new"); // Run in headless mode
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        // Initialize the WebDriver
        WebDriver driver = new ChromeDriver(options);
        
        try {
            // Navigate to the website
            String url = "https://scriptureearth.org/data/xav/sab/xav/#/text";
            driver.get(url);
            
            // Initialize WebDriverWait with a longer timeout
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            
            // Wait for the page to be fully loaded
            wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Try multiple selectors for the first click
            String[] firstClickSelectors = {
                ".whitespace-nowrap",
                "summary div",
                "details > summary > div"
            };
            
            boolean firstClickSuccess = false;
            for (String selector : firstClickSelectors) {
                try {
                    WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                    element.click();
                    firstClickSuccess = true;
                    System.out.println("Clicked element with selector: " + selector);
                    break;
                } catch (Exception e) {
                    System.out.println("Failed to click element with selector: " + selector);
                }
            }
            
            if (!firstClickSuccess) {
                throw new RuntimeException("Failed to click the first element with any selector");
            }
            
            // Wait for the menu to expand
            Thread.sleep(2000);
            
            // Try multiple selectors for the second click (GEN book)
            String[] secondClickSelectors = {
                "#GEN",
                "[id='GEN']",
                "span#GEN",
                "div[role='button'] span:contains('GEN')"
            };
            
            boolean secondClickSuccess = false;
            for (String selector : secondClickSelectors) {
                try {
                    WebElement element = wait.until(ExpectedConditions.elementToBeClickable(
                        selector.startsWith("//") ? 
                            By.xpath(selector) : 
                            By.cssSelector(selector)));
                    element.click();
                    secondClickSuccess = true;
                    System.out.println("Clicked GEN book with selector: " + selector);
                    break;
                } catch (Exception e) {
                    System.out.println("Failed to click GEN book with selector: " + selector);
                }
            }
            
            if (!secondClickSuccess) {
                throw new RuntimeException("Failed to click the GEN book with any selector");
            }
            
            // Wait for the content to load after second click
            Thread.sleep(3000);
            
            // Try multiple selectors for the third click (Chapter 1)
            String[] thirdClickSelectors = {
                "#1",
                "[id='1']",
                "span#1",
                "div[role='button'] span:contains('1')",
                ".grid-cols-6 > #\\31"  // CSS-escaped '1'
            };
            
            boolean thirdClickSuccess = false;
            for (String selector : thirdClickSelectors) {
                try {
                    WebElement element = wait.until(ExpectedConditions.elementToBeClickable(
                        selector.startsWith("//") ? 
                            By.xpath(selector) : 
                            By.cssSelector(selector)));
                    element.click();
                    thirdClickSuccess = true;
                    System.out.println("Clicked chapter 1 with selector: " + selector);
                    break;
                } catch (Exception e) {
                    System.out.println("Failed to click chapter 1 with selector: " + selector);
                }
            }
            
            if (!thirdClickSuccess) {
                throw new RuntimeException("Failed to click chapter 1 with any selector");
            }
            
            // Wait for the chapter content to be present
            WebDriverWait waitForContent = new WebDriverWait(driver, Duration.ofSeconds(20));
            
            // Try different possible selectors for the chapter content
            String[] chapterSelectors = {
                "[data-chapter='1']",  // Common pattern for chapter containers
                "div.chapter-content",
                "div.chapter",
                "div[role='article']",
                "article"
            };
            
            WebElement chapterElement = null;
            String usedSelector = "";
            
            for (String selector : chapterSelectors) {
                try {
                    chapterElement = waitForContent.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                    usedSelector = selector;
                    System.out.println("Found chapter element with selector: " + selector);
                    break;
                } catch (Exception e) {
                    System.out.println("Chapter element not found with selector: " + selector);
                }
            }
            
            if (chapterElement == null) {
                // If no specific chapter container found, try to get the main content area
                try {
                    chapterElement = driver.findElement(By.cssSelector("main, .main, #main, .content, #content"));
                    usedSelector = "main/content fallback";
                    System.out.println("Using main/content area as fallback");
                } catch (Exception e) {
                    throw new RuntimeException("Could not find chapter content with any selector");
                }
            }
            
            // Get the inner HTML of the chapter element
            String chapterHtml = chapterElement.getAttribute("innerHTML");
            
            // Also try to get it via JavaScript as a fallback
            if (chapterHtml == null || chapterHtml.trim().isEmpty()) {
                try {
                    String script = String.format("return document.querySelector('%s')?.innerHTML || '';", 
                        usedSelector.replace("'", "\\'"));
                    chapterHtml = (String) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script);
                } catch (Exception e) {
                    System.out.println("JavaScript fallback failed: " + e.getMessage());
                }
            }
            
            // Print the chapter HTML
            System.out.println("\nChapter HTML content (first 1000 chars):");
            System.out.println(chapterHtml.substring(0, Math.min(1000, chapterHtml.length())));
            System.out.println("\n... (content truncated) ...\n");
            
            // Save the full chapter HTML to a file
            try {
                String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = String.format("chapter_%s.html", timestamp);
                
                // Create a complete HTML document with the chapter content
                String fullHtml = String.join("\n",
                    "<!DOCTYPE html>",
                    "<html><head>",
                    "<meta charset=\"UTF-8\">",
                    "<title>Chapter Content</title>",
                    "<style>body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; }</style>",
                    "</head><body>",
                    chapterHtml,
                    "</body></html>"
                );
                
                java.nio.file.Files.writeString(
                    java.nio.file.Path.of(filename), 
                    fullHtml);
                System.out.println("\nFull chapter HTML saved to: " + filename);
                
                // Also save the raw chapter HTML
                java.nio.file.Files.writeString(
                    java.nio.file.Path.of("chapter_raw.html"), 
                    chapterHtml);
                System.out.println("Raw chapter HTML saved to: chapter_raw.html");
                
            } catch (Exception e) {
                System.err.println("Failed to save chapter content: " + e.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the browser
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
