package com.gnews.fake.domain;

import java.time.Instant;

public record Article(
        String id,
        String title,
        String description,
        String content,
        String url,
        String image,
        Instant publishedAt,
        String lang,
        String category, // Internal use for filtering
        Source source) {
}
