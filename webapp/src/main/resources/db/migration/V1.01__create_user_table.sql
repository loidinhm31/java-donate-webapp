CREATE SCHEMA IF NOT EXISTS donation;

SET search_path TO donation;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA donation;

--
-- Table structure for table user_account
--
CREATE TABLE IF NOT EXISTS user_account
(
    user_id             UUID DEFAULT uuid_generate_v4() NOT NULL,
    username            VARCHAR(50) UNIQUE NOT NULL,
    password            VARCHAR(80) NOT NULL,
    is_verify           BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (user_id)
);


--
-- Table structure for table role
--
CREATE TABLE IF NOT EXISTS role
(
    role_id             UUID DEFAULT uuid_generate_v4() NOT NULL,
    role_name           VARCHAR(50) NOT NULL,
    role_code           VARCHAR(50) NOT NULL,
    created_by          VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_by          VARCHAR(50) NOT NULL,
    updated_at          TIMESTAMP NOT NULL,

    PRIMARY KEY (role_id)
);

--
-- Table structure for table user_role
--
CREATE TABLE IF NOT EXISTS user_role
(
    id          UUID DEFAULT uuid_generate_v4() NOT NULL,
    user_id     UUID NOT NULL,
    role_id     UUID NOT NULL,

    PRIMARY KEY (id),

    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
    ON DELETE NO ACTION ON UPDATE NO ACTION,

    FOREIGN KEY (role_id) REFERENCES role(role_id)
    ON DELETE NO ACTION ON UPDATE NO ACTION
);