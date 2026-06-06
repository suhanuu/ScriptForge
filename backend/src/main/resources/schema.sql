CREATE TABLE IF NOT EXISTS novels (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL UNIQUE,
    file_name TEXT NOT NULL,
    raw_content TEXT NOT NULL,
    chapter_count INTEGER DEFAULT 0,
    total_chars INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS scripts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    novel_id BIGINT NOT NULL UNIQUE,
    yaml_content TEXT,
    scenes_count INTEGER DEFAULT 0,
    status TEXT DEFAULT 'CONVERTING',
    error_message TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    updated_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS chapters (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    novel_id BIGINT NOT NULL,
    chapter_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    word_count INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now'))
);
