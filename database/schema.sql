-- ============================================
-- Game 8-Ball Pool Multiplayer - Database Schema
-- Chạy lệnh: psql -U postgres -f schema.sql
-- ============================================

-- Tạo database (chạy với superuser)
CREATE DATABASE billiards_db;
\c billiards_db;

-- Bảng người chơi
CREATE TABLE IF NOT EXISTS players (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    exp INT DEFAULT 0,
    level INT DEFAULT 1,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    avatar_id INT DEFAULT 0,
    cue_color VARCHAR(20) DEFAULT '#DEB887',
    created_at TIMESTAMP DEFAULT NOW()
);

-- Lịch sử trận đấu
CREATE TABLE IF NOT EXISTS match_history (
    id SERIAL PRIMARY KEY,
    winner_id INT REFERENCES players(id),
    loser_id INT REFERENCES players(id),
    exp_gained INT DEFAULT 100,
    played_at TIMESTAMP DEFAULT NOW()
);

-- Index để tăng tốc query leaderboard
CREATE INDEX IF NOT EXISTS idx_players_exp ON players(exp DESC);
CREATE INDEX IF NOT EXISTS idx_players_username ON players(username);

-- Bảng bạn bè (Friendships)
CREATE TABLE IF NOT EXISTS friendships (
    user_id INT REFERENCES players(id),
    friend_id INT REFERENCES players(id),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ACCEPTED, BLOCKED
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, friend_id)
);

-- Bảng tin nhắn (Messages)
CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    sender_id INT REFERENCES players(id),
    receiver_id INT REFERENCES players(id),
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT NOW()
);

-- Dữ liệu mẫu để test (tuỳ chọn)
-- INSERT INTO players (email, username, password_hash, exp, level, wins, losses)
-- VALUES ('test@test.com', 'testuser', '$2a$10$xxx', 500, 5, 10, 5);
