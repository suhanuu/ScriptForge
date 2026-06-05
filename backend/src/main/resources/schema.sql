CREATE TABLE IF NOT EXISTS novels (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid TEXT NOT NULL UNIQUE,
    file_name TEXT NOT NULL,
    raw_content TEXT NOT NULL,
    chapter_count INTEGER DEFAULT 0,
    total_chars INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now'))
);
