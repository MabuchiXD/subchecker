CREATE TABLE bot_sessions (
                              user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                              state VARCHAR(50) DEFAULT 'IDLE',
                              temp_name TEXT,
                              temp_price DOUBLE PRECISION,
                              temp_url TEXT
);