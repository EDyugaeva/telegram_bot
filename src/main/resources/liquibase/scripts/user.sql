--liquibase formatted sql
--changeset eradomskaya:1
CREATE TABLE notification_task (
                      id SERIAL PRIMARY KEY,
                      chat_id BIGINT,
                      text_message TEXT,
                      time timestamp
);



