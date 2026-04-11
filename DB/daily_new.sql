USE bs_project;

DROP TABLE IF EXISTS daily;

CREATE TABLE daily (
    username    VARCHAR(20),
    status      VARCHAR(4),
    word        VARCHAR(20),
    id          NUMERIC(4, 0),
    PRIMARY KEY (username, id)
);