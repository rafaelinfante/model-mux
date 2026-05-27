CREATE TABLE usage_record (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    correlation_id  VARCHAR(36)  NOT NULL,
    client_id       VARCHAR(120) NOT NULL,
    route_mode      VARCHAR(16)  NOT NULL,
    provider_id     VARCHAR(64)  NOT NULL,
    provider_type   VARCHAR(16)  NOT NULL,
    model           VARCHAR(120) NOT NULL,
    tier            VARCHAR(16)  NOT NULL,
    prompt_tokens   BIGINT       NOT NULL,
    completion_tokens BIGINT     NOT NULL,
    total_tokens    BIGINT       NOT NULL,
    cost_usd        DECIMAL(12,6) NOT NULL,
    latency_ms      BIGINT       NOT NULL,
    cache_hit       BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_over     BOOLEAN      NOT NULL DEFAULT FALSE,
    finish_reason   VARCHAR(20)  NOT NULL,
    created_at      DATETIME(3)  NOT NULL,
    CONSTRAINT pk_usage_record PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX ix_usage_client_time ON usage_record (client_id, created_at);
CREATE INDEX ix_usage_provider ON usage_record (provider_id);

CREATE TABLE budget (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    client_id        VARCHAR(120)  NOT NULL,
    window_date      DATE          NOT NULL,
    daily_limit_usd  DECIMAL(12,6) NOT NULL,
    spent_usd        DECIMAL(12,6) NOT NULL DEFAULT 0,
    version          BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_budget PRIMARY KEY (id),
    CONSTRAINT uq_budget_client_window UNIQUE (client_id, window_date)
) ENGINE = InnoDB;
