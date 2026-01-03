package com.example.urlshortener.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Link {

    private final String code;           // сокращённый код
    private final String originalUrl;
    private final UUID ownerUuid;
    private final Instant createdAt;
    private final long ttlSeconds;       // время жизни (задаётся системой)

    private long maxClicks;              // лимит может быть изменён владельцем (расширение)
    private long clicks;                 // счётчик переходов

    public Link(String code, String originalUrl, UUID ownerUuid,
                long ttlSeconds, long maxClicks) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.ownerUuid = ownerUuid;
        this.ttlSeconds = ttlSeconds;
        this.maxClicks = maxClicks;
        this.createdAt = Instant.now();
    }

    /* --- геттеры --- */
    public String getCode()         { return code; }
    public String getOriginalUrl()  { return originalUrl; }
    public UUID getOwnerUuid()      { return ownerUuid; }
    public Instant getCreatedAt()   { return createdAt; }
    public long getTtlSeconds()     { return ttlSeconds; }
    public long getMaxClicks()      { return maxClicks; }
    public long getClicks()         { return clicks; }

    /* --- бизнес-методы --- */
    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plusSeconds(ttlSeconds));
    }

    public boolean isLimitReached() {
        return clicks >= maxClicks;
    }

    public void registerClick() {
        if (isExpired() || isLimitReached()) {
            throw new IllegalStateException("Link is inactive");
        }
        clicks++;
    }

    public void updateMaxClicks(long newMaxClicks) {
        if (newMaxClicks <= 0) {
            throw new IllegalArgumentException("maxClicks must be > 0");
        }
        if (newMaxClicks < clicks) {
            throw new IllegalArgumentException("new maxClicks must be >= current clicks");
        }
        this.maxClicks = newMaxClicks;
    }

    /* equals / hashCode / toString */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link link)) return false;
        return Objects.equals(code, link.code);
    }

    @Override public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() {
        return String.format("Link{%s -> %s, ttl=%ds, clicks %d/%d}",
                code, originalUrl, ttlSeconds, clicks, maxClicks);
    }
}
