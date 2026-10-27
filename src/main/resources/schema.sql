CREATE TABLE IF NOT EXISTS articles (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description CLOB,
    content CLOB,
    url VARCHAR(500),
    image VARCHAR(500),
    published_at TIMESTAMP,
    lang VARCHAR(10),
    category VARCHAR(50),
    source_id VARCHAR(64),
    source_name VARCHAR(255),
    source_url VARCHAR(500),
    source_country VARCHAR(10)
);
