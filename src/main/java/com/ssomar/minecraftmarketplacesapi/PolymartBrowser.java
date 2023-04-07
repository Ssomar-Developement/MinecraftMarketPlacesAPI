package com.ssomar.minecraftmarketplacesapi;

import com.ssomar.minecraftmarketplacesapi.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PolymartBrowser extends VirtualBrowser {

    public static final String BASE = "https://polymart.org/";

    private final String loggedInUserId;

    public PolymartBrowser(){
        super(BASE, "2", false);
        System.out.println("Try to connect on polymart with the username: " + Config.getInstance().getPolymartUsername() + " and the password: " + Config.getInstance().getPolymartPassword());
        loggedInUserId = login(Config.getInstance().getPolymartUsername(), Config.getInstance().getPolymartPassword());
    }

    private String login(String username, String password) {
        navigate(BASE + "/login");

        WebElement usernameField = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.name("loginCredential"))));
        WebElement passwordField = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.name("password"))));

        if (usernameField == null || passwordField == null) {
            throw new IllegalStateException("Could not find a username or password field!");
        }

        // Fill in credentials
        usernameField.clear();
        passwordField.clear();
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        sleep(1000);
        // Login!
        WebElement loginButton = driver.findElement(By.name("submit"));
        loginButton.click();

        sleep(2000);

        WebElement link = driver.findElement(By.xpath("//a[@class='main-header-account-button dark']"));

        return link.getAttribute("href")
                .split("/user/")[1]
                .replace("/", "");
    }

    public BuyerResp addBuyer(String ressourceID, String buyerUsername) {

        try {
            navigate(BASE + "/resource/" + ressourceID + "/buyers");
            if (isCloudflare()) return BuyerResp.SYSTEM_ERROR;
            sleep(2000);
            WebElement addBuyer = driver.findElement(By.id("product-buyers"));
            WebElement addBuyerButton = addBuyer.findElement(By.id("buyer-option-buttons"));
            WebElement addBuyerButton2 = addBuyerButton.findElements(By.tagName("a")).get(2);
            addBuyerButton2.click();
            sleep(1000);
            WebElement buyerName = driver.findElement(By.name("buyer_user"));
            buyerName.sendKeys(buyerUsername);
            sleep(1000);
            WebElement addBuyer2 = driver.findElement(By.xpath("//input[contains(@value, 'Add Buyer')]"));
            addBuyer2.submit();
            sleep(1000);
            if (driver.getPageSource().contains("The requested user could not be found.")) {
                System.out.println("[INFO] USER: " + buyerUsername + " NOT FOUND and can't be added in the buyer list of " + ressourceID);
                return BuyerResp.INVALID_SPIGOT_USERNAME;
            } else {
                System.out.println("[INFO] USER: " + buyerUsername + " added in the buyer list of the resource: " + ressourceID);
                return BuyerResp.VALID;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BuyerResp.SYSTEM_ERROR;
        }
    }

    public String getUsername(String polymartID) {

        try {
            navigate(BASE + "/user/" + polymartID);
            sleep(1000);
            String link = driver.getCurrentUrl();

            return link.split("\\/")[4].split("\\.")[0];
        } catch (Exception e) {
            e.printStackTrace();
            return "Ssomar";
        }
    }

    public void postAnUpdate(String link, String version, String title, String description, String uploadFilePath) {
        try {
            navigate(BASE + link);
            sleep(2000);
            WebElement updateElem = driver.findElement(By.id("update-button"));
            updateElem.click();
            sleep(1000);

            WebElement versionElem = driver.findElement(By.name("update_version"));
            versionElem.clear();
            versionElem.sendKeys(version);
            sleep(1000);
            WebElement titleElem = driver.findElement(By.name("update_title"));
            titleElem.clear();
            titleElem.sendKeys(title);
            sleep(1000);
            WebElement bbElem = driver.findElement(By.xpath("//a[@onclick=\"clicksImportFromBBCode('update-description-tinymce')\"]"));
            bbElem.click();

            sleep(1000);
            WebElement descriptionElem = driver.findElement(By.id("import-bbcode__update-description-tinymce"));
            for (WebElement wE : descriptionElem.findElements(By.xpath(".//*"))) {
                //System.out.println(wE.getTagName());
                if (wE.getTagName().equals("textarea")) {
                    wE.clear();
                    wE.sendKeys(description);
                }
            }
            sleep(1000);
            WebElement okayElem = driver.findElement(By.id("import-bbcode__update-description-tinymce-2"));
            okayElem.click();
            sleep(2000);

            WebElement uploadElem = driver.findElement(By.name("update_file"));
            uploadElem.sendKeys(uploadFilePath);
            sleep(7000);

            WebElement button = driver.findElement(By.name("submit"));
            button.submit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String parseAvatarUrl(String url) {
        if (url.startsWith("data")) return BASE + "/" + url;
        if (url.startsWith("//static")) return "https:" + url;
        return url;
    }
}
