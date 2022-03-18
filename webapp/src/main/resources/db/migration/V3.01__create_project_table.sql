CREATE TABLE IF NOT EXISTS PROJECT_INCREMENT_ID
(
    project_id SERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS BENEFICIARY_INCREMENT_ID
(
    beneficiary_id SERIAL PRIMARY KEY
);

--
-- Table structure for table project
--
CREATE TABLE IF NOT EXISTS project
(
    project_id          VARCHAR NOT NULL,
    creator_id          UUID NOT NULL,
    beneficiary_id      VARCHAR NOT NULL,
    project_name        VARCHAR NOT NULL,
    project_summary     VARCHAR(256) NOT NULL,
    project_content     TEXT NOT NULL,
    status              VARCHAR NOT NULL,
    start_time          TIMESTAMP NOT NULL,
    target_time         TIMESTAMP NOT NULL,
    target_money        FLOAT NOT NULL,
    current_money       FlOAT NOT NULL DEFAULT 0,
    count_extend        INT,
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_by          VARCHAR(50) NOT NULL,
    updated_at          TIMESTAMP NOT NULL,

    PRIMARY KEY (project_id),
    FOREIGN KEY (creator_id) REFERENCES user_detail(user_id)
);

--
-- Table structure for table project_doc
--
CREATE TABLE IF NOT EXISTS project_doc
(
    id                  SERIAL NOT NULL,
    project_id          VARCHAR NOT NULL,
    file_path           VARCHAR NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (project_id) REFERENCES project(project_id)
);



--
-- Table structure for table project_supporter
--
CREATE TABLE IF NOT EXISTS donation_time
(
    id                                  VARCHAR NOT NULL,
    project_id                          VARCHAR NOT NULL,
    user_id                             UUID,
    supporter_name                      VARCHAR NOT NULL,
    supporter_email                               VARCHAR,
    is_display_name                     BOOLEAN DEFAULT TRUE NOT NULL,
    transaction_code                    VARCHAR UNIQUE,
    payment_method                      VARCHAR NOT NULL,
    message                             VARCHAR(255),
    amount                              FLOAT,
    status                              VARCHAR NOT NULL,
    created_by                          VARCHAR(50) NOT NULL,
    created_at                          TIMESTAMP NOT NULL,
    updated_by                          VARCHAR(50) NOT NULL,
    updated_at                          TIMESTAMP NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (project_id) REFERENCES project(project_id),
    FOREIGN KEY (user_id) REFERENCES user_detail(user_id)
);


--
-- Table structure for table beneficiary
--
CREATE TABLE IF NOT EXISTS beneficiary
(
    beneficiary_id                      VARCHAR NOT NULL,
    beneficiary_name                    VARCHAR NOT NULL,
    beneficiary_phone_number            VARCHAR NOT NULL,
    beneficiary_type                    VARCHAR NOT NULL,
    is_receive                          BOOLEAN NOT NULL DEFAULT FALSE,
    created_by                          VARCHAR(50) NOT NULL,
    created_at                          TIMESTAMP NOT NULL,
    updated_by                          VARCHAR(50) NOT NULL,
    updated_at                          TIMESTAMP NOT NULL,

    PRIMARY KEY (beneficiary_id)
);
ALTER TABLE donation.project
    ADD FOREIGN KEY (beneficiary_id) REFERENCES beneficiary(beneficiary_id);