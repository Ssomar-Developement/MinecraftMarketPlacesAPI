package com.ssomar.minecraftmarketplacesapi;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BuiltByBitBrowser extends VirtualBrowser {

    public static final String BASE = "https://builtbybit.com";

    private final String loggedInUserId;

    private int t;

    public BuiltByBitBrowser(String dataDir, String builtByBitUsername, String builtByBitPassword) throws Exception{
        super(BASE, dataDir + "4", false);
        t = 0;
        System.out.println("Try to connect on spigot with the username: " + builtByBitUsername);
        loggedInUserId = login(builtByBitUsername, builtByBitPassword);
    }

    private String login(String username, String password) throws Exception {

        if (t == 4) {
            throw new Exception("Error while login to : " + BASE + " with the username: " + username);
        }
        try {
            navigate("https://builtbybit.com/account/");

            try {
                sleep(5000);
                WebElement logout = driver.findElement(By.xpath("//a[@data-nav-id='defaultLogOut']"));
                String logoutlink = logout.getAttribute("href");
                driver.executeScript("popup_window = window.open('" + logoutlink + "')");
                driver.executeScript("popup_window.close()");
                sleep(5000);
            } catch (Exception e) {}

            navigate(BASE + "/login");

            WebElement usernameField = driver.findElement(By.name("login"));
            WebElement passwordField = driver.findElement(By.name("password"));

            if (usernameField == null || passwordField == null) {
                throw new IllegalStateException("Could not find a username or password field!");
            }

            // Fill in credentials
            usernameField.clear();
            passwordField.clear();
            usernameField.sendKeys(username);
            passwordField.sendKeys(password);

            // Login!
            passwordField.submit();

            sleep(2000);

            return driver.findElement(By.className("p-navgroup-linkText")).getText();
        } catch (Exception | Error e) {
            e.printStackTrace();
            System.out.println("Error while login on BuiltbyBit, try again in 15 seconds");
            sleep(15000);
            t++;
            return login(username, password);
        }
    }

    public void postAnUpdate(String link, String version, String title, String description, String uploadFilePath) {
        try {
            navigate(BASE + link);
            sleep(2000);
            WebElement versionElem = driver.findElement(By.name("version_string"));
            versionElem.clear();
            versionElem.sendKeys(version);
            sleep(1000);
            WebElement titleElem = driver.findElement(By.name("update_title"));
            titleElem.clear();
            titleElem.sendKeys(title);
            sleep(1000);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.id("moreMisc-1")));
                button.click();
            } catch (TimeoutException e) {
                System.out.println("Button not found or not clickable.");
            }

            List<WebElement> buttons = driver.findElements(By.id("xfBbCode-1"));

            if (!buttons.isEmpty()) {
                WebElement button = buttons.get(0);
                String classes = button.getAttribute("class");
                boolean isActive = classes.contains("fr-active");
                if (!isActive) {
                    button.click();
                }
            }

            sleep(1000);
            WebElement descriptionElem = driver.findElement(By.name("update_message"));
            try {
                descriptionElem.clear();
                descriptionElem.sendKeys(description);
            } catch (Exception e) {}
            sleep(1000);

            for (WebElement wE : driver.findElements(By.tagName("input"))) {
                //System.out.println(wE.getAttribute("title"));
                if (wE.getAttribute("type").equals("file")) {
                    //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>"+wE.getAttribute("title"));
                    wE.sendKeys(uploadFilePath);
                    break;
                }
            }
            /* Required update quite long */
            sleep(8000);
            WebElement button = driver.findElement(By.xpath("//button[contains(@class, 'button--primary')]"));
            button.submit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BuyerResp addBuyer(String ressourceID, String buyerUsername) {

        try {
            navigate(BASE + "/resources/" + ressourceID + "/buyers/add");
            sleep(2000);
            WebElement newBuyer = driver.findElement(By.name("usernames"));
            newBuyer.clear();
            newBuyer.sendKeys(buyerUsername);
            sleep(1000);
            WebElement button = driver.findElement(By.name("save"));
            button.submit();
            sleep(1000);
            if (driver.getPageSource().contains("The requested user could not be found.")) {
                System.out.println("[INFO] USER: " + buyerUsername + " NOT FOUND and can't be added in the buyer list of " + ressourceID);
                return BuyerResp.INVALID_SPIGOT_USERNAME;
            } else {
                System.out.println("[INFO] USER: " + buyerUsername + " added in the buyer list of the resource: " + ressourceID);
                return BuyerResp.VALID;
            }
        } catch (Exception e) {
            return BuyerResp.SYSTEM_ERROR;
        }
    }


    private String parseAvatarUrl(String url) {
        if (url.startsWith("data")) return BASE + "/" + url;
        if (url.startsWith("//static")) return "https:" + url;
        return url;
    }
}
