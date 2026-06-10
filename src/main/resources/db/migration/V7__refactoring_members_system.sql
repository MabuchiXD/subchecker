DROP TABLE IF EXISTS subscription_family CASCADE;

CREATE TABLE subscription_members (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                      subscription_id BIGINT REFERENCES subscriptions(id) ON DELETE CASCADE,
                                      is_hardcore BOOLEAN DEFAULT FALSE,
                                      bomber_interval_minutes INTEGER DEFAULT 60,
                                      last_bomber_notify_sent TIMESTAMP,
                                      UNIQUE(user_id, subscription_id)
);

ALTER TABLE users ADD COLUMN default_currency VARCHAR(10) DEFAULT 'RUB';
ALTER TABLE bot_sessions ADD COLUMN temp_currency VARCHAR(10);