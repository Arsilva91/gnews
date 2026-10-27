package com.gnews.fake.service;

import com.gnews.fake.domain.Article;
import com.gnews.fake.dto.ArticleDto;
import com.gnews.fake.dto.ArticlesResponse;
import com.gnews.fake.dto.SourceDto;
import com.gnews.fake.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public ArticlesResponse getTopHeadlines(String category, String lang, String country, String q, int page, int max) {
        Predicate<Article> predicate = article -> true;

        if (category != null && !category.isBlank()) {
            predicate = predicate.and(a -> a.category().equalsIgnoreCase(category));
        }
        if (lang != null && !lang.isBlank()) {
            predicate = predicate.and(a -> a.lang().equalsIgnoreCase(lang));
        }
        if (country != null && !country.isBlank()) {
            predicate = predicate.and(a -> a.source().country().equalsIgnoreCase(country));
        }
        if (q != null && !q.isBlank()) {
            String query = q.toLowerCase();
            predicate = predicate.and(a -> a.title().toLowerCase().contains(query) ||
                    a.description().toLowerCase().contains(query));
        }

        return fetchAndMap(predicate, page, max);
    }

    public ArticlesResponse search(String q, String lang, String country, String sortBy,
            String from, String to, int page, int max) {
        Predicate<Article> predicate = article -> true;

        // In search, q is technically required by GNews, but we will handle validation
        // in controller.
        if (q != null && !q.isBlank()) {
            String query = q.toLowerCase();
            predicate = predicate.and(a -> a.title().toLowerCase().contains(query) ||
                    a.description().toLowerCase().contains(query));
        }
        if (lang != null && !lang.isBlank()) {
            predicate = predicate.and(a -> a.lang().equalsIgnoreCase(lang));
        }
        if (country != null && !country.isBlank()) {
            predicate = predicate.and(a -> a.source().country().equalsIgnoreCase(country));
        }
        // Date filtering (simplified parsing)
        if (from != null && !from.isBlank()) {
            Instant fromDate = Instant.parse(from);
            predicate = predicate.and(a -> a.publishedAt().isAfter(fromDate));
        }
        if (to != null && !to.isBlank()) {
            Instant toDate = Instant.parse(to);
            predicate = predicate.and(a -> a.publishedAt().isBefore(toDate));
        }

        // sortBy=relevance has no real score in the mock, so results stay ordered by date
        return fetchAndMap(predicate, page, max);
    }

    private ArticlesResponse fetchAndMap(Predicate<Article> predicate, int page, int max) {
        List<Article> filtered = articleRepository.findAll().stream()
                .filter(predicate)
                .toList();

        int total = filtered.size();
        // long avoids int overflow on very large page numbers
        long skip = (long) (page - 1) * max;

        List<ArticleDto> resultDtos = filtered.stream()
                .skip(skip)
                .limit(max)
                .map(this::mapToDto)
                .toList();

        return new ArticlesResponse(total, resultDtos);
    }

    private ArticleDto mapToDto(Article article) {
        return new ArticleDto(
                article.id(),
                article.title(),
                article.description(),
                article.content(),
                article.url(),
                article.image(),
                ISO_FORMATTER.format(article.publishedAt()),
                article.lang(),
                new SourceDto(
                        article.source().id(),
                        article.source().name(),
                        article.source().url(),
                        article.source().country()));
    }
}
