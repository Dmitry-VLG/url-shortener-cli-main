package com.example.urlshortener.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.awt.Desktop;
import java.net.URI;

@Command(name = "open", description = "Открыть ссылку в браузере")
public class OpenCommand implements Runnable {

    @ParentCommand UrlShortenerCommand parent;

    @Parameters(paramLabel = "<code>", description = "Код ссылки")
    String code;

    @Override
    public void run() {
        try {
            String url = parent.getLinkService().open(code);
            openInBrowser(url);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void openInBrowser(String url) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
            System.out.println("Браузер открыт.");
        } else {
            System.out.println("Откройте вручную: " + url);
        }
    }
}
