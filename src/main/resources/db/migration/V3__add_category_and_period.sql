ALTER TABLE subscriptions ADD COLUMN category VARCHAR(50);
ALTER TABLE subscriptions ADD COLUMN period_days INTEGER DEFAULT 30;

ALTER TABLE bot_sessions ADD COLUMN temp_category VARCHAR(50);
ALTER TABLE bot_sessions ADD COLUMN temp_period_days INTEGER;
ALTER TABLE bot_sessions ADD COLUMN temp_sub_id BIGINT;