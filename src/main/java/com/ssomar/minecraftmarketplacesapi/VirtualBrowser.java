package com.ssomar.minecraftmarketplacesapi;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;
import java.util.Map;

public class VirtualBrowser {

    protected ChromeDriver driver;

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public VirtualBrowser(String link, String dataDir, boolean forceHeadless) {
        WebDriverManager.chromedriver().setup();

        System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
        System.setProperty("webdriver.chrome.silentOutput", "true");

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        if (!isWindows() && !isMac()) {
            options.addArguments("--disable-extensions");
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-first-run");
            options.addArguments("--disable-setuid-sandbox");
            options.addArguments("--window-size=1920,1080");
        }
        if (forceHeadless) options.addArguments("--headless=new");

        options.addArguments("user-data-dir=" + dataDir);
        options.addArguments("--remote-allow-origins=*");

        this.driver = new ChromeDriver(options);

        // Anti-bot detection: override navigator properties before any page loads
        try {
            driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument",
                Map.of("source",
                    "Object.defineProperty(navigator, 'webdriver', { get: () => false }); " +
                    "Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] }); " +
                    "Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] }); " +
                    "window.chrome = { runtime: {} };"
                )
            );
        } catch (Exception e) {
            System.err.println("Warning: Could not set CDP anti-detection: " + e.getMessage());
        }

    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public void navigate(String url) {
        driver.get(url);
    }

    public boolean isCloudflare() {
        try {
            String source = driver.getPageSource();
            if (source.contains("Checking if the site connection is secure")
                || source.contains("cf-challenge")
                || source.contains("Just a moment")) {
                System.out.println("Cloudflare detected.");
                return true;
            }
        } catch (Exception e) {
            // Session might be invalid
        }
        return false;
    }

    public void close() {
        try {
            driver.quit();
        } catch (Exception e) {
            // ignore
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
