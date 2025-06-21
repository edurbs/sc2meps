package com.github.edurbs;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
//import io.github.bonigarcia.wdm.WebDriverManager;
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
            
            // Wait for the page to load (you might need to adjust the wait time)
            Thread.sleep(5000);
            
            // Get the page source
            String pageContent = driver.getPageSource();
            
            // Print the first 1000 characters to verify
            System.out.println("Page content (first 1000 chars):");
            System.out.println(pageContent.substring(0, Math.min(1000, pageContent.length())));
            
            // Print the length of the content
            System.out.println("\nTotal content length: " + pageContent.length() + " characters");
            
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
