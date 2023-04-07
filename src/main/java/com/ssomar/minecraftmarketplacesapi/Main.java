package com.ssomar.minecraftmarketplacesapi;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, world!");
        /* Add test user on polymart and spigot */
        /* SpigotBrowser spigotBrowser = new SpigotBrowser();
        spigotBrowser.addBuyer("83070", "test3");
        spigotBrowser.close();

        PolymartBrowser polymartBrowser = new PolymartBrowser();
        polymartBrowser.addBuyer("2858", "test3");
        polymartBrowser.close();*/

        BuiltByBitBrowser builtByBitBrowser = new BuiltByBitBrowser();
        builtByBitBrowser.postAnUpdate("/resources/executable-items.18673/post-update", "1.0.0", "test", "test", "C:\\Users\\octav\\Documents\\GitHub\\ExecutableItems\\target\\ExecutableItems.jar");
    }

}
