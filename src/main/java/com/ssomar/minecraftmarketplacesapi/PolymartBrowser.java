package com.ssomar.minecraftmarketplacesapi;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PolymartBrowser extends VirtualBrowser {

    public static final String BASE = "https://polymart.org/";

    private final String loggedInUserId;

    public PolymartBrowser(String polymardUsername, String polymartPassword) {
        super(BASE, "2", false);
        System.out.println("Try to connect on polymart with the username: " + polymardUsername);
        loggedInUserId = login(polymardUsername, polymartPassword);
    }

    private String login(String username, String password) {
        navigate(BASE + "/login");

        WebElement usernameField = new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.name("email"))));
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
        WebElement loginButton = driver.findElement(By.id("login-4"));
        loginButton.click();

        sleep(5000);

        WebElement link = driver.findElement(By.xpath("//a[img[contains(@alt, 'profile photo')]]"));
        ;

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
            WebElement updateElem = driver.findElement(
                    By.xpath("//a[text()='Post an update']")
            );
            updateElem.click();
            sleep(1000);

            WebElement versionElem = driver.findElement(By.name("version"));
            versionElem.clear();
            versionElem.sendKeys(version);
            sleep(1000);
            WebElement titleElem = driver.findElement(By.name("update_title"));
            titleElem.clear();
            titleElem.sendKeys(title);
            sleep(1000);
            //WebElement bbElem = driver.findElement(By.xpath("//a[@onclick=\"clicksImportFromBBCode('update-description-tinymce')\"]"));
            //bbElem.click();

            sleep(1000);
            /*WebElement descriptionElem = driver.findElement(By.id("import-bbcode__update-description-tinymce"));
            for (WebElement wE : descriptionElem.findElements(By.xpath(".//*"))) {
                //System.out.println(wE.getTagName());
                if (wE.getTagName().equals("textarea")) {
                    wE.clear();
                    wE.sendKeys(description);
                }
            }*/

            driver.switchTo().frame("_general-form-1-3_ifr");
            // 2. Find the editable <body> inside the iframe
            WebElement body = driver.findElement(By.tagName("body"));
            // 3. Clear any existing content and write new text
            body.clear();
            body.sendKeys(stripBBCode(description));
            // 4. Switch back to main content
            driver.switchTo().defaultContent();

            sleep(1000);
            //WebElement okayElem = driver.findElement(By.id("import-bbcode__update-description-tinymce-2"));
            //okayElem.click();
            sleep(2000);

            WebElement uploadElem = driver.findElement(By.name("update_file"));
            uploadElem.sendKeys(uploadFilePath);
            sleep(7000);

            WebElement button = driver.findElement(By.id("_general-form-1-9"));
            button.submit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String stripBBCode(String input) {
        String result = input;

        // Remove all [TAG] and [/TAG] patterns including ones with attributes like [URL='...']
        result = result.replaceAll("(?i)\\[(\\/)?[a-z]+(=[^\\]]+)?\\]", "");

        // Replace multiple blank lines with just 2 newlines
        result = result.replaceAll("\\n{3,}", "\n\n");

        // Trim whitespace
        return result.trim();
    }

    public static String convertToMarkdown(String input) {
        String result = input;

        // Convert [IMG]...[/IMG] to Markdown image
        result = result.replaceAll("(?i)\\[IMG\\](.*?)\\[/IMG\\]", "![]($1)");

        // Convert [URL='...']...[/URL] to Markdown link
        result = result.replaceAll("(?i)\\[URL='(.*?)'\\](.*?)\\[/URL\\]", "[$2]($1)");

        // Remove formatting tags: [B], [I], [SIZE=...], [COLOR=...], [CENTER], etc.
        result = result.replaceAll("(?i)\\[/?(B|I|U|CENTER|SIZE=\\d+|SIZE|COLOR=#[0-9A-Fa-f]{6}|COLOR=rgb\\([^\\]]+\\)|COLOR)\\]", "");

        // Convert multiple newlines into maximum 2 (to avoid too many line breaks)
        result = result.replaceAll("\\n{3,}", "\n\n");

        // Trim leading/trailing whitespace
        return result.trim();
    }


    private String parseAvatarUrl(String url) {
        if (url.startsWith("data")) return BASE + "/" + url;
        if (url.startsWith("//static")) return "https:" + url;
        return url;
    }
}
