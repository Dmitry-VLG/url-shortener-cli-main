package com.example.shortener;

import com.example.urlshortener.cli.UrlShortenerCommand;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class CliIntegrationTest {

    @Test void helpDoesNotFail() {
        int code = new CommandLine(new UrlShortenerCommand())
                .execute("--help");
        assert code == 0;
    }
}