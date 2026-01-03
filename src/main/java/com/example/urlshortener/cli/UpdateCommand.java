package com.example.urlshortener.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "update", description = "Изменить параметры ссылки (только владелец)")
public class UpdateCommand implements Runnable {

    @ParentCommand UrlShortenerCommand parent;

    @Parameters(paramLabel = "<code>", description = "Код ссылки")
    String code;

    @Option(names = "--max", required = true, description = "Новый максимум кликов")
    long max;

    @Override
    public void run() {
        try {
            parent.getLinkService().updateMaxClicks(code, parent.getUuid(), max);
            System.out.println("Лимит обновлён.");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
