package com.example.urlshortener;

import com.example.urlshortener.cli.UrlShortenerCommand;
import com.example.urlshortener.infra.cleanup.ExpiredLinkCleaner;
import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
        UrlShortenerCommand root = new UrlShortenerCommand();

        // фоновая очистка + уведомления владельца по TTL
        ExpiredLinkCleaner.start(root.getNotificationService());

        int exit = new CommandLine(root).execute(args);
        System.exit(exit);
    }
}
