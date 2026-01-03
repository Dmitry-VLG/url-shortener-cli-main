package com.example.urlshortener.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "list", description = "Список ваших ссылок")
public class ListCommand implements Runnable {

    @ParentCommand
    UrlShortenerCommand parent;

    @Override
    public void run() {
        parent.getLinkService()
                .listByOwner(parent.getUuid())
                .forEach(System.out::println);
    }
}
