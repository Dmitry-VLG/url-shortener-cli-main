package com.example.urlshortener.cli;

import com.example.urlshortener.core.service.LinkService;
import com.example.urlshortener.core.service.notification.ConsoleNotificationService;
import com.example.urlshortener.core.service.notification.NotificationService;
import com.example.urlshortener.infra.identity.UserIdentityStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.UUID;

@Command(
        name = "shorty",
        version = "1.0",
        mixinStandardHelpOptions = true,
        description = "CLI-сервис сокращения ссылок",
        subcommands = {
                CreateCommand.class,
                OpenCommand.class,
                ListCommand.class,
                DeleteCommand.class,
                UpdateCommand.class
        }
)
public class UrlShortenerCommand implements Runnable {

    @Option(names = "--uuid", description = "UUID пользователя (если не задан, берётся из локального хранилища)")
    private UUID uuid;

    @Option(names = "--reset-uuid", description = "Сгенерировать новый UUID и сохранить его локально")
    private boolean resetUuid;

    private final NotificationService notificationService = new ConsoleNotificationService();
    private final LinkService linkService = new LinkService(notificationService);

    @Override
    public void run() {
        System.out.println("Use sub-command. Run with --help for details.");
    }

    public UUID getUuid() {
        if (uuid != null) return uuid;

        if (resetUuid) {
            uuid = UUID.randomUUID();
            UserIdentityStore.save(uuid);
            System.out.println("Generated and saved new UUID: " + uuid);
            return uuid;
        }

        uuid = UserIdentityStore.load().orElseGet(() -> {
            UUID u = UUID.randomUUID();
            UserIdentityStore.save(u);
            System.out.println("Generated and saved new UUID: " + u);
            return u;
        });

        return uuid;
    }

    public LinkService getLinkService() {
        return linkService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}
