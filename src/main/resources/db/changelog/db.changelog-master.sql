--liquibase formatted sql

--changeset andrey:001-create-notification-task
CREATE TABLE IF NOT EXISTS notification_task (
  id           BIGSERIAL PRIMARY KEY,
  chat_id      BIGINT      NOT NULL,
  message      TEXT        NOT NULL,
  scheduled_at TIMESTAMP   NOT NULL,
  sent         BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_task_due
  ON notification_task (sent, scheduled_at);

--rollback DROP TABLE IF EXISTS notification_task;