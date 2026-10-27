package com.gnews.fake.repository;

import com.gnews.fake.domain.Article;
import com.gnews.fake.domain.Source;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ArticleRepository {

    private final JdbcTemplate jdbcTemplate;

    public ArticleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(List<Article> newArticles) {
        if (newArticles == null || newArticles.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO articles (
                id, title, description, content, url, image,
                published_at, lang, category,
                source_id, source_name, source_url, source_country
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.batchUpdate(
            sql,
            newArticles,
            newArticles.size(),
            (ps, article) -> {
                ps.setString(1, article.id());
                ps.setString(2, article.title());
                ps.setString(3, article.description());
                ps.setString(4, article.content());
                ps.setString(5, article.url());
                ps.setString(6, article.image());
                ps.setTimestamp(7, article.publishedAt() != null ? Timestamp.from(article.publishedAt()) : null);
                ps.setString(8, article.lang());
                ps.setString(9, article.category());

                Source source = article.source();
                if (source != null) {
                    ps.setString(10, source.id());
                    ps.setString(11, source.name());
                    ps.setString(12, source.url());
                    ps.setString(13, source.country());
                } else {
                    ps.setString(10, null);
                    ps.setString(11, null);
                    ps.setString(12, null);
                    ps.setString(13, null);
                }
            }
        );
    }

    public List<Article> findAll() {
        String sql = "SELECT * FROM articles ORDER BY published_at DESC";
        return jdbcTemplate.query(sql, this::mapRowToArticle);
    }

    public List<Article> findByTitle(String title) {
        String sql = "SELECT * FROM articles WHERE title = ?";
        return jdbcTemplate.query(sql, this::mapRowToArticle, title);
    }

    private Article mapRowToArticle(ResultSet rs, int rowNum) throws SQLException {
        Source source = new Source(
            rs.getString("source_id"),
            rs.getString("source_name"),
            rs.getString("source_url"),
            rs.getString("source_country")
        );

        Timestamp publishedAtTimestamp = rs.getTimestamp("published_at");

        return new Article(
            rs.getString("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("content"),
            rs.getString("url"),
            rs.getString("image"),
            publishedAtTimestamp != null ? publishedAtTimestamp.toInstant() : null,
            rs.getString("lang"),
            rs.getString("category"),
            source
        );
    }
}