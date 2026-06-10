CREATE INDEX idx_subscriptions_next_payment ON subscriptions(next_payment_date);
CREATE INDEX idx_subscriptions_owner ON subscriptions(user_id);
CREATE INDEX idx_sub_family_user ON subscription_family(user_id);