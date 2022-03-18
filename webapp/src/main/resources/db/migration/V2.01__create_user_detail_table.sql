--
-- Table structure for table user_details
--
CREATE TABLE IF NOT EXISTS user_detail
(
    user_id             UUID NOT NULL,
    email               VARCHAR(50) UNIQUE,
    first_name          VARCHAR(50) NOT NULL,
    last_name           VARCHAR(50) NOT NULL,
    birthdate           TIMESTAMP NOT NULL,
    phone_number        VARCHAR(50),
    authentication_type VARCHAR(50) ,
    verification_code   VARCHAR(64),
    reset_token         VARCHAR(30),
    lasted_login_time   TIMESTAMP,
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_by          VARCHAR(50) NOT NULL,
    updated_at          TIMESTAMP NOT NULL,

    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
);