package com.ssomar.minecraftmarketplacesapi;

import com.ssomar.minecraftmarketplacesapi.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BuiltByBitBrowser extends VirtualBrowser {

    public static final String BASE = "https://builtbybit.com";

    private final String loggedInUserId;

    public BuiltByBitBrowser() {
        super(BASE, "4", false);
        System.out.println("Try to connect on spigot with the username: " + Config.getInstance().getSpigotUsername());
        loggedInUserId = login(Config.getInstance().getBuiltByBitUsername(), Config.getInstance().getBuiltByBitPassword());
    }

    private String login(String username, String password){
        navigate("https://builtbybit.com/account/");

        try {
            sleep(5000);
            WebElement logout = driver.findElement(By.xpath("//a[@data-nav-id='defaultLogOut']"));
            String logoutlink = logout.getAttribute("href");
            driver.executeScript("popup_window = window.open('"+logoutlink+"')");
            driver.executeScript("popup_window.close()");
            sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        return  driver.findElement(By.className("p-navgroup-linkText")).getText();
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
            WebElement BBCodeElem = driver.findElement(By.id("xfBbCode-1"));
            BBCodeElem.click();
            sleep(1000);
            WebElement descriptionElem = driver.findElement(By.name("update_message"));
            descriptionElem.clear();
            descriptionElem.sendKeys(description);

            sleep(1000);
            for (WebElement wE : driver.findElements(By.tagName("input"))) {
                //System.out.println(wE.getAttribute("value"));
                if (wE.getAttribute("type").equals("file")) {
                    wE.sendKeys(uploadFilePath);
                    break;
                }
            }
            /* Required update quite long */
            sleep(4000);
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
        }
        catch (Exception e){
            return BuyerResp.SYSTEM_ERROR;
        }
    }


    private String parseAvatarUrl(String url) {
        if (url.startsWith("data")) return BASE + "/" + url;
        if (url.startsWith("//static")) return "https:" + url;
        return url;
    }
}
