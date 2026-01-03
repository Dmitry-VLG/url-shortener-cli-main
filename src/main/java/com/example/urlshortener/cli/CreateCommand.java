package com.example.urlshortener.cli;

import com.example.urlshortener.core.model.Link;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "create", description = "Создать короткую ссылку")
public class CreateCommand implements Runnable {

    @ParentCommand UrlShortenerCommand parent;

    @Parameters(paramLabel = "<url>", description = "Исходный URL")
    String url;

    @Option(names = "--max", description = "Максимум кликов (по умолчанию из конфигурации)")
    Long max;

    @Override
    public void run() {
        try {
            Link link = parent.getLinkService().create(url, parent.getUuid(), max);
            System.out.printf("Короткий код: %s  (полный: clck.ru/%s)%n", link.getCode(), link.getCode());
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
