package com.ssomar.minecraftmarketplacesapi;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SpigotBrowser extends VirtualBrowser {

    public static final String BASE = "https://www.spigotmc.org";

    private final String loggedInUserId;

    private int t;

    public SpigotBrowser(String dataDir, String spigotUsername, String spigotPassword, boolean spigot2FA) throws Exception {
        super(BASE, dataDir+"Spigot", false);
        t = 0;
        System.out.println("Try to connect on spigot with the username: " + spigotUsername);
        loggedInUserId = login(spigotUsername, spigotPassword, spigot2FA);
    }

    private String login(String username, String password, boolean spigot2FA) throws Exception {
        if (t == 4) {
            throw new Exception("Error while login to : " + BASE + " with the username: " + username);
        }
        try {
            navigate("https://www.spigotmc.org/logout");
            String url = this.driver.getCurrentUrl();
            System.out.println("URL : " + url);
            if (url.contains("logout")) {
                System.out.println("Logout check");
                WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds(30));
                try {
                    // Wait up to 20 seconds for the element to appear
                    WebElement element = wait.until(
                            ExpectedConditions.elementToBeClickable(By.cssSelector("a.button.primary.LogOut"))
                    );

                    element.click();
                    System.out.println("Logout button clicked successfully!");

                } catch (TimeoutException e) {
                    System.out.println("Logout element not found after 20 seconds.");
                } catch (NoSuchElementException e) {
                    System.out.println("Logout element not found in DOM.");
                } catch (Exception e) {
                    System.out.println("Unexpected error while trying to click logout: " + e.getMessage());
                }
            }

            navigate(BASE + "/login");
            sleep(2000);

            WebElement loginDialog = driver.findElement(By.id("pageLogin"));
            WebElement usernameField = loginDialog.findElement(By.id("ctrl_pageLogin_login"));
            WebElement passwordField = loginDialog.findElement(By.id("ctrl_pageLogin_password"));

            if (usernameField == null || passwordField == null) {
                throw new IllegalStateException("Could not find a username or password field!");
            }

            // Fill in credentials
            usernameField.clear();
            usernameField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
            sleep(1000);
            passwordField.clear();
            passwordField.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
            sleep(1000);
            usernameField.sendKeys(username);
            sleep(1000);
            passwordField.sendKeys(password);
            sleep(1000);

            // Login!
            passwordField.submit();
            sleep(2000);

            if (spigot2FA) {
                System.out.println("2FA check, you have 30 seconds to enter the code");
                sleep(30000);
            }

            WebElement link = driver.findElement(By.className("sidebar"))
                    .findElement(By.className("visitorPanel"))
                    .findElement(By.className("avatar"));

            return link.getAttribute("href")
                    .split("/members/")[1]
                    .replace("/", "")
                    .split("[.]")[1];
        } catch (Exception | Error e) {
            e.printStackTrace();
            System.out.println("Error while login, try again in 15 seconds");
            sleep(15000);
            t++;
            return login(username, password, spigot2FA);
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
            WebElement titleElem = driver.findElement(By.name("title"));
            titleElem.clear();
            titleElem.sendKeys(title);
            sleep(1000);
            WebElement BBCodeElem = driver.findElement(By.className("redactor_btn_switchmode"));
            BBCodeElem.click();
            sleep(1000);
            WebElement descriptionElem = driver.findElement(By.className("bbCodeEditorContainer"));
            for (WebElement wE : descriptionElem.findElements(By.xpath(".//*"))) {
                //System.out.println(wE.getTagName());
                if (wE.getTagName().equals("textarea")) {
                    wE.clear();
                    wE.sendKeys(description);
                }
            }
            sleep(1000);
            for (WebElement wE : driver.findElements(By.tagName("input"))) {
                //System.out.println(wE.getAttribute("value"));
                if (uploadFilePath.contains("no-file")) {
                    if (wE.getAttribute("type").equals("url")) {
                        String uploadFilePathEdited = uploadFilePath.replace("no-file", "");
                        wE.clear();
                        wE.sendKeys(uploadFilePathEdited);
                    } else continue;
                } else if (wE.getAttribute("type").equals("file")) {
                    wE.sendKeys(uploadFilePath);
                }
            }
            sleep(5000);

            WebElement button = driver.findElement(By.xpath("//input[@class='button primary']"));
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
