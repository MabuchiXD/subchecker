ALTER TABLE bot_sessions ADD COLUMN temp_notes TEXT;
ALTER TABLE subscriptions ADD COLUMN notes TEXT;
ALTER TABLE subscriptions ADD COLUMN is_acknowledged BOOLEAN DEFAULT FALSE;
ALTER TABLE subscriptions ADD COLUMN invite_code VARCHAR(20) UNIQUE;