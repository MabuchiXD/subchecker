CREATE TABLE users (
                       id BIGINT PRIMARY KEY, -- telegramId
                       username VARCHAR(255),
                       first_name VARCHAR(255),
                       role VARCHAR(20) DEFAULT 'USER'
);

CREATE TABLE subscriptions (
                               id BIGSERIAL PRIMARY KEY,
                               service_name VARCHAR(100) NOT NULL,
                               price DOUBLE PRECISION NOT NULL,
                               currency VARCHAR(10),
                               last_payment_date DATE,
                               next_payment_date DATE NOT NULL,
                               payment_url VARCHAR(512),
                               is_active BOOLEAN DEFAULT TRUE,
                               user_id BIGINT REFERENCES users(id) ON DELETE CASCADE
);

-- Первая версия семейного доступа (простая ManyToMany)
CREATE TABLE subscription_family (
                                     user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                     subscription_id BIGINT REFERENCES subscriptions(id) ON DELETE CASCADE,
                                     PRIMARY KEY (user_id, subscription_id)
);