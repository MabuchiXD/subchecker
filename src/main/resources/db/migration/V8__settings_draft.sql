ALTER TABLE bot_sessions ADD COLUMN temp_pref_time TIME;
ALTER TABLE bot_sessions ADD COLUMN temp_timezone_offset INTEGER;
ALTER TABLE bot_sessions ADD COLUMN temp_default_currency VARCHAR(10);