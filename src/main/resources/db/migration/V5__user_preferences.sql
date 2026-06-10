ALTER TABLE users ADD COLUMN preferred_notification_time TIME DEFAULT '10:00:00';
ALTER TABLE users ADD COLUMN timezone_offset INTEGER DEFAULT 3;