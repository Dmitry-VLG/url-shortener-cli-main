package com.example.urlshortener.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "delete", description = "Удалить ссылку")
public class DeleteCommand implements Runnable {

    @ParentCommand UrlShortenerCommand parent;

    @Parameters(paramLabel = "<code>", description = "Код ссылки")
    String code;

    @Override
    public void run() {
        try {
            parent.getLinkService().delete(code, parent.getUuid());
            System.out.println("Удалено.");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
