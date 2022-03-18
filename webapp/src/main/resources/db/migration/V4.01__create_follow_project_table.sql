--
-- Table structure for table follower_project
--
CREATE TABLE IF NOT EXISTS follower_project
(
    user_id        UUID NOT NULL,
    project_id     VARCHAR NOT NULL,
    is_notify      BOOLEAN,

    PRIMARY KEY (user_id, project_id),
    FOREIGN KEY (user_id) REFERENCES user_detail(user_id),
    FOREIGN KEY (project_id) REFERENCES project(project_id)
);