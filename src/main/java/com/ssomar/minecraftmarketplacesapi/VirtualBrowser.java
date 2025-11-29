package com.ssomar.minecraftmarketplacesapi;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;

public class VirtualBrowser {

    protected ChromeDriver driver;

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public VirtualBrowser(String link, String dataDir, boolean forceHeadless) {
        //WebDriverManager.chromedriver().clearDriverCache().setup();
        WebDriverManager.chromedriver().setup();

        System.setProperty(ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY, "true");
        System.setProperty("webdriver.chrome.silentOutput", "true");

        ChromeOptions options = new ChromeOptions();

        options.addArguments(new String[]{"--disable-blink-features=AutomationControlled"});
        options.setExperimentalOption("useAutomationExtension", Boolean.valueOf(false));
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        if (!isWindows() && !isMac()) {
            options.addArguments("--disable-extensions");
            //options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-first-run");
            options.addArguments("--single-process");
            options.addArguments("--disable-setuid-sandbox");
        }
        if (forceHeadless) options.addArguments("--headless");

        options.addArguments("user-data-dir=" + dataDir);
        options.addArguments("--remote-allow-origins=*");

        this.driver = new ChromeDriver(options);

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
        if (driver.getPageSource().contains("Checking if the site connection is secure")) {
            System.out.println("Cloudflare detected.");
            return true;
        } else
            return false;
    }

    public void close() {
        driver.quit();
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
