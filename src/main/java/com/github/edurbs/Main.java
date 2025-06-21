package com.github.edurbs;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.JavascriptExecutor;
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
            
            // Try multiple selectors for the first click (menu toggle)
            String[] firstClickSelectors = {
                "button[aria-label='Toggle menu']",
                ".whitespace-nowrap",
                "summary",
                "details > summary",
                "[role='button']",
                "button"
            };
            
            boolean firstClickSuccess = false;
            for (String selector : firstClickSelectors) {
                try {
                    System.out.println("Trying selector: " + selector);
                    // First try regular click
                    try {
                        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", element);
                        element.click();
                        firstClickSuccess = true;
                        System.out.println("Clicked element with selector: " + selector);
                        Thread.sleep(1000); // Wait for any animations
                        break;
                    } catch (Exception e) {
                        System.out.println("Regular click failed, trying JavaScript click...");
                        // If regular click fails, try JavaScript click
                        try {
                            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));
                            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
                            firstClickSuccess = true;
                            System.out.println("Clicked element with JavaScript using selector: " + selector);
                            Thread.sleep(1000); // Wait for any animations
                            break;
                        } catch (Exception jsE) {
                            System.out.println("JavaScript click also failed for selector: " + selector);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Failed to find or click element with selector: " + selector + " - " + e.getMessage());
                }
            }
            
            if (!firstClickSuccess) {
                // Try one last time with a more generic approach
                try {
                    System.out.println("Trying last resort click...");
                    ((JavascriptExecutor)driver).executeScript(
                        "document.querySelector('button, [role=button], summary, details > div').click();");
                    firstClickSuccess = true;
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println("Last resort click failed: " + e.getMessage());
                }
                
                if (!firstClickSuccess) {
                    throw new RuntimeException("Failed to click the menu with any selector");
                }
            }
            
            // Wait for the menu to expand and GEN book to be visible
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'GEN') or contains(text(), 'Genesis')]")));
                System.out.println("Menu expanded successfully");
            } catch (Exception e) {
                System.out.println("Menu might not have expanded as expected: " + e.getMessage());
                // Continue anyway, as the element might still be clickable
            }
            
            // Try multiple selectors for the second click (GEN book)
            String[] secondClickSelectors = {
                "#GEN",
//                "[id='GEN']",
//                "span#GEN",
//                "div[role='button'] span:contains('GEN')"
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
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), 'Genesis') or contains(@id, 'GEN')]")));
                Thread.sleep(1000); // Additional wait for stability
            } catch (Exception e) {
                System.out.println("Warning: Content loading check failed: " + e.getMessage());
            }
            
            // Try multiple selectors for the third click (Chapter 1)
            String[] thirdClickSelectors = {
//                "#1",
                "[id='1']",
//                "span#1",
//                "div[role='button'] span:contains('1')",
//                ".grid-cols-6 > #\\31"  // CSS-escaped '1'
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
//                "[data-chapter='1']",  // Common pattern for chapter containers
//                "div.chapter-content",
//                "div.chapter",
//                "div[role='article']",
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

            
            // Create a string builder to hold all chapters
            StringBuilder allChaptersHtml = new StringBuilder();
            
            // Add HTML header
            allChaptersHtml.append("<!DOCTYPE html>\n");
            allChaptersHtml.append("<html><head>\n");
            allChaptersHtml.append("<meta charset=\"UTF-8\">\n");
            allChaptersHtml.append("<title>Genesis - Complete Book</title>\n");
            allChaptersHtml.append("<style>\n");
            allChaptersHtml.append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; max-width: 900px; margin: 0 auto; padding: 20px; }\n");
            allChaptersHtml.append("h1 { text-align: center; color: #2c3e50; margin-bottom: 30px; }\n");
            allChaptersHtml.append(".chapter { margin-bottom: 40px; border-bottom: 1px solid #eee; padding-bottom: 20px; }\n");
            allChaptersHtml.append(".chapter h2 { color: #2980b9; border-bottom: 1px solid #eee; padding-bottom: 5px; }\n");
            allChaptersHtml.append("</style>\n");
            allChaptersHtml.append("</head><body>\n");
            allChaptersHtml.append("<h1>The Book of Genesis</h1>\n");
            allChaptersHtml.append("<div style='text-align: center; margin-bottom: 30px; color: #7f8c8d; font-style: italic;'>Extracted from scriptureearth.org - " + 
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")) + "</div>\n");
            
            // Add the first chapter we already have
            allChaptersHtml.append("<div class='chapter' id='chapter-1'>\n");
            allChaptersHtml.append("<h2>Chapter 1</h2>\n");
            allChaptersHtml.append(chapterHtml);
            allChaptersHtml.append("\n</div>\n");
            
            // Process remaining chapters (2-50)
            for (int chapterNum = 2; chapterNum <= 50; chapterNum++) {
                try {
                    System.out.println("\n=== Processing Chapter " + chapterNum + " of 50 ===");
                    
                    // First, refresh the page to ensure clean state
                    System.out.println("Refreshing page...");
                    driver.navigate().refresh();
                    
                    // Wait for the menu to be clickable and click it
                    System.out.println("Clicking menu...");
                    WebElement menuToggle = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".whitespace-nowrap")));
                    menuToggle.click();
                    
                    // Wait for the GEN book and click it
                    System.out.println("Clicking GEN book...");
                    WebElement genBook = wait.until(ExpectedConditions.elementToBeClickable(By.id("GEN")));
                    genBook.click();
                    
                    // Wait for the chapter list to be visible
                    Thread.sleep(1000);
                    
                    // Click on the chapter number
                    String chapterSelector = String.format("[id='%d']", chapterNum);
                    System.out.println("Clicking chapter selector: " + chapterSelector);
                    
                    // Wait for the chapter link to be clickable and click it
                    WebElement chapterLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(chapterSelector)));
                    chapterLink.click();
                    
                    // Wait for the content to be visible and not empty
                    System.out.println("Waiting for content to load...");
                    WebElement contentDiv = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("#content:not(:empty)")));
                    
                    // Wait a bit more for dynamic content to fully load
                    // Add progressive delay to avoid rate limiting (longer wait for later chapters)
                    int delay = 2000 + (100 * (chapterNum / 10)); // Increase delay as we progress
                    System.out.println("Waiting " + delay + "ms for content to stabilize...");
                    Thread.sleep(delay);
                    
                    // Use JavaScript to get the complete content
                    System.out.println("Extracting chapter content...");
                    String chapterContent = (String) ((JavascriptExecutor) driver).executeScript(
                        "return document.getElementById('content').innerHTML;");
                    
                    if (chapterContent == null || chapterContent.trim().isEmpty()) {
                        System.out.println("Warning: Chapter content is empty! Trying alternative method...");
                        // Try getting the content directly from the body
                        chapterContent = (String) ((JavascriptExecutor) driver).executeScript(
                            "return document.body.innerHTML;");
                    }
                    
                    // Add the chapter to our collection
                    allChaptersHtml.append(String.format("<div class='chapter' id='chapter-%d'>\n", chapterNum));
                    allChaptersHtml.append(String.format("<h2>Chapter %d</h2>\n", chapterNum));
                    allChaptersHtml.append(chapterContent);
                    allChaptersHtml.append("\n</div>\n");
                    
                    System.out.println("Added Chapter " + chapterNum);
                    
                } catch (Exception e) {
                    System.err.println("Error processing Chapter " + chapterNum + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Close the HTML document
            allChaptersHtml.append("</body></html>");
            
            // Save the complete HTML to a file
            try {
                String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = String.format("genesis_complete_%s.html", timestamp);
                
                java.nio.file.Files.writeString(
                    java.nio.file.Path.of(filename), 
                    allChaptersHtml.toString());
                
                System.out.println("\nAll 50 chapters saved to: " + filename);
                
                // Also save a raw version for reference
                java.nio.file.Files.writeString(
                    java.nio.file.Path.of("genesis_complete_raw.html"), 
                    allChaptersHtml.toString());
                
            } catch (Exception e) {
                System.err.println("Failed to save complete book: " + e.getMessage());
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
