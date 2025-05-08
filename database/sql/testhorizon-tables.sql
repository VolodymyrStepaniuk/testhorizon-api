-- Table: authorities

DROP TABLE IF EXISTS authorities;

CREATE TABLE IF NOT EXISTS authorities
(
    id   BIGINT       NOT NULL
        PRIMARY KEY,
    name VARCHAR(255) NOT NULL
        CONSTRAINT authorities_name_check
            CHECK ((name)::TEXT = ANY
                   ((ARRAY ['MENTOR'::CHARACTER VARYING, 'TESTER'::CHARACTER VARYING, 'ADMIN'::CHARACTER VARYING])::TEXT[]))
);

ALTER SEQUENCE authorities_id_seq
    OWNED BY authorities.id;

ALTER TABLE IF EXISTS authorities
    OWNER TO postgres_container;

-- Table: users

DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users
(
    id                         BIGINT                      NOT NULL
        PRIMARY KEY,
    created_at                 TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    email                      VARCHAR(255)                NOT NULL,
    first_name                 VARCHAR(255)                NOT NULL,
    is_account_non_expired     BOOLEAN                     NOT NULL,
    is_account_non_locked      BOOLEAN                     NOT NULL,
    is_credentials_non_expired BOOLEAN                     NOT NULL,
    is_enabled                 BOOLEAN                     NOT NULL,
    updated_at                 TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    last_name                  VARCHAR(255)                NOT NULL,
    password                   VARCHAR(255)                NOT NULL,
    total_rating               INTEGER
);

ALTER SEQUENCE users_id_seq
    OWNED BY users.id;

ALTER TABLE users
    OWNER TO postgres_container;

-- Table: user_has_authorities

DROP TABLE IF EXISTS user_has_authorities;

CREATE TABLE IF NOT EXISTS users_has_authorities
(
    user_id      BIGINT NOT NULL
        CONSTRAINT fk2v3psdmus5xumckbsqchqasf8
            REFERENCES users,
    authority_id BIGINT NOT NULL
        CONSTRAINT fkana7dbfsf64vnsr5cuf8tod64
            REFERENCES authorities,
    PRIMARY KEY (user_id, authority_id)
);

ALTER TABLE users_has_authorities
    OWNER TO postgres_container;

-- Table: email_codes

DROP TABLE IF EXISTS email_codes;

CREATE TABLE IF NOT EXISTS email_codes
(
    id         BIGINT                      NOT NULL
        PRIMARY KEY,
    code       VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    user_id    BIGINT
        CONSTRAINT ukeocsp7dt09ew4l3xtae3pibkg
            UNIQUE
        CONSTRAINT fkhp2gjsjfh54slejknoefitleo
            REFERENCES users
);

ALTER SEQUENCE email_codes_id_seq
    OWNED BY email_codes.id;

ALTER TABLE email_codes
    OWNER TO postgres_container;

-- Table: bug_report_severities

DROP TABLE IF EXISTS bug_report_severities;

CREATE TABLE IF NOT EXISTS bug_report_severities
(
    id   BIGINT       NOT NULL
        PRIMARY KEY,
    name VARCHAR(255) NOT NULL
        CONSTRAINT bug_report_severities_name_check
            CHECK ((name)::TEXT = ANY
                   ((ARRAY ['LOW'::CHARACTER VARYING, 'MEDIUM'::CHARACTER VARYING, 'HIGH'::CHARACTER VARYING, 'CRITICAL'::CHARACTER VARYING])::TEXT[]))
);

ALTER SEQUENCE bug_report_severities_id_seq
    OWNED BY bug_report_severities.id;

ALTER TABLE bug_report_severities
    OWNER TO postgres_container;

-- Table: bug_report_statuses

DROP TABLE IF EXISTS bug_report_statuses;

CREATE TABLE IF NOT EXISTS bug_report_statuses
(
    id   BIGINT       NOT NULL
        PRIMARY KEY,
    name VARCHAR(255) NOT NULL
        CONSTRAINT bug_report_statuses_name_check
            CHECK ((name)::TEXT = ANY
                   (ARRAY [('OPENED'::CHARACTER VARYING)::TEXT, ('IN_PROGRESS'::CHARACTER VARYING)::TEXT, ('RESOLVED'::CHARACTER VARYING)::TEXT, ('CLOSED'::CHARACTER VARYING)::TEXT]))
);

ALTER SEQUENCE bug_report_statuses_id_seq
    OWNED BY bug_report_statuses.id;

ALTER TABLE bug_report_statuses
    OWNER TO postgres_container;

-- Table: bug_reports

DROP TABLE IF EXISTS bug_reports;

CREATE TABLE IF NOT EXISTS bug_reports
(
    id          BIGINT                      NOT NULL
        PRIMARY KEY,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    description VARCHAR(255)                NOT NULL,
    environment VARCHAR(255)                NOT NULL,
    project_id  BIGINT                      NOT NULL,
    reporter_id BIGINT                      NOT NULL,
    title       VARCHAR(255)                NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    severity_id BIGINT
        CONSTRAINT fkt1c9wvptheiyc37x7bpgm5787
            REFERENCES bug_report_severities,
    status_id   BIGINT
        CONSTRAINT fkou3wbooo529qhiofjax9v9imq
            REFERENCES bug_report_statuses
);

ALTER SEQUENCE bug_reports_id_seq
    OWNED BY bug_reports.id;

ALTER TABLE bug_reports
    OWNER TO postgres_container;

-- Table: project_statuses

DROP TABLE IF EXISTS project_statuses;

CREATE TABLE IF NOT EXISTS project_statuses
(
    id   BIGINT       NOT NULL
        PRIMARY KEY,
    name VARCHAR(255) NOT NULL
        CONSTRAINT project_statuses_name_check
            CHECK ((name)::TEXT = ANY
                   ((ARRAY ['ACTIVE'::CHARACTER VARYING, 'INACTIVE'::CHARACTER VARYING, 'PAUSED'::CHARACTER VARYING])::TEXT[]))
);

ALTER SEQUENCE project_statuses_id_seq
    OWNED BY project_statuses.id;

ALTER TABLE project_statuses
    OWNER TO postgres_container;

-- Table: projects

DROP TABLE IF EXISTS projects;

CREATE TABLE IF NOT EXISTS projects
(
    id           BIGINT                      NOT NULL
        PRIMARY KEY,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    description  VARCHAR(255)                NOT NULL,
    github_url   VARCHAR(255)                NOT NULL,
    instructions VARCHAR(255),
    title        VARCHAR(255)                NOT NULL,
    owner_id     BIGINT                      NOT NULL,
    updated_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status_id    BIGINT
        CONSTRAINT fk53o7v6gmlvrgoqnmjlh8v4pwm
            REFERENCES project_statuses
);

ALTER SEQUENCE projects_id_seq
    OWNED BY projects.id;

ALTER TABLE projects
    OWNER TO postgres_container;

-- Table: test_case_priorities

DROP TABLE IF EXISTS test_case_priorities;

CREATE TABLE IF NOT EXISTS test_case_priorities
(
    id   BIGINT       NOT NULL
        PRIMARY KEY,
    name VARCHAR(255) NOT NULL
        CONSTRAINT test_case_priorities_name_check
            CHECK ((name)::TEXT = ANY
                   ((ARRAY ['LOW'::CHARACTER VARYING, 'MEDIUM'::CHARACTER VARYING, 'HIGH'::CHARACTER VARYING])::TEXT[]))
);

ALTER SEQUENCE test_case_priorities_id_seq
    OWNED BY test_case_priorities.id;

ALTER TABLE test_case_priorities
    OWNER TO postgres_container;

-- Table: test_cases

DROP TABLE IF EXISTS test_cases;

CREATE TABLE IF NOT EXISTS test_cases
(
    id            BIGINT                      NOT NULL
        PRIMARY KEY,
    author_id     BIGINT                      NOT NULL,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    description   VARCHAR(255),
    input_data    VARCHAR(255),
    preconditions VARCHAR(255),
    project_id    BIGINT                      NOT NULL,
    steps         TEXT[]                      NOT NULL,
    title         VARCHAR(255)                NOT NULL,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    priority_id   BIGINT
        CONSTRAINT fk2kwolxfdnhfidxcs3xsm94s51
            REFERENCES test_case_priorities
);

ALTER SEQUENCE test_cases_id_seq
    OWNED BY test_cases.id;

ALTER TABLE test_cases
    OWNER TO postgres_container;

-- Table: test_types

DROP TABLE IF EXISTS test_types;

CREATE TABLE IF NOT EXISTS test_types
(
    id   BIGINT       NOT NULL
        PRIMARY KEY,
    name VARCHAR(255) NOT NULL
        CONSTRAINT test_types_name_check
            CHECK ((name)::TEXT = ANY
                   ((ARRAY ['UNIT'::CHARACTER VARYING, 'INTEGRATION'::CHARACTER VARYING, 'FUNCTIONAL'::CHARACTER VARYING, 'END_TO_END'::CHARACTER VARYING, 'ACCEPTANCE'::CHARACTER VARYING, 'PERFORMANCE'::CHARACTER VARYING, 'SMOKE'::CHARACTER VARYING])::TEXT[]))
);

ALTER SEQUENCE test_types_id_seq
    OWNED BY test_types.id;

ALTER TABLE test_types
    OWNER TO postgres_container;

-- Table: tests

DROP TABLE IF EXISTS tests;

CREATE TABLE IF NOT EXISTS tests
(
    id           BIGINT                      NOT NULL
        PRIMARY KEY,
    author_id    BIGINT                      NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    description  VARCHAR(255),
    github_url   VARCHAR(255)                NOT NULL,
    instructions VARCHAR(255),
    project_id   BIGINT                      NOT NULL,
    test_case_id BIGINT,
    title        VARCHAR(255)                NOT NULL,
    updated_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    type_id      BIGINT
        CONSTRAINT fkn1rj1wqb0gktbdmjl1c6jr42e
            REFERENCES test_types
);

ALTER SEQUENCE tests_id_seq
    OWNED BY tests.id;

ALTER TABLE tests
    OWNER TO postgres_container;

-- Table: comments

DROP TABLE IF EXISTS comments;

CREATE TABLE IF NOT EXISTS comments
(
    id          BIGINT                      NOT NULL
        PRIMARY KEY,
    author_id   BIGINT                      NOT NULL,
    content     VARCHAR(255)                NOT NULL,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    entity_type VARCHAR(255)                NOT NULL,
    entity_id   BIGINT                      NOT NULL
);

ALTER SEQUENCE comments_id_seq
    OWNED BY comments.id;

ALTER TABLE comments
    OWNER TO postgres_container;

-- Table: ratings

DROP TABLE IF EXISTS ratings;

CREATE TABLE IF NOT EXISTS ratings
(
    id               BIGINT                      NOT NULL
        PRIMARY KEY,
    comment          VARCHAR(255),
    created_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    rated_by_user_id BIGINT                      NOT NULL,
    rating_points    INTEGER                     NOT NULL,
    user_id          BIGINT                      NOT NULL
);

ALTER SEQUENCE ratings_id_seq
    OWNED BY ratings.id;

ALTER TABLE ratings
    OWNER TO postgres_container;

-- Table: password_reset_tokens

DROP TABLE IF EXISTS password_reset_tokens;

CREATE TABLE IF NOT EXISTS password_reset_tokens
(
    id         BIGINT                      NOT NULL
        PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    token      VARCHAR(255)                NOT NULL,
    user_id    BIGINT
        CONSTRAINT fkk3ndxg5xp6v7wd4gjyusp15gq
            REFERENCES users
);

ALTER SEQUENCE password_reset_tokens_id_seq
    OWNED BY password_reset_tokens.id;

ALTER TABLE password_reset_tokens
    OWNER TO postgres_container;

-- Table: files

DROP TABLE IF EXISTS files;

CREATE TABLE IF NOT EXISTS files
(
    id            UUID                        NOT NULL
        PRIMARY KEY,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    entity_id     BIGINT                      NOT NULL,
    entity_type   VARCHAR(255)                NOT NULL
        CONSTRAINT files_entity_type_check
            CHECK ((entity_type)::TEXT = ANY
                   ((ARRAY ['USER'::CHARACTER VARYING, 'TEST_CASE'::CHARACTER VARYING, 'TEST'::CHARACTER VARYING, 'PROJECT'::CHARACTER VARYING, 'COMMENT'::CHARACTER VARYING, 'BUG_REPORT'::CHARACTER VARYING])::TEXT[])),
    original_name VARCHAR(255)                NOT NULL
);

ALTER TABLE files
    OWNER TO postgres_container;

-- Table: feedbacks
DROP TABLE IF EXISTS feedbacks;

CREATE TABLE IF NOT EXISTS feedbacks
(
    id         BIGINT                      NOT NULL
        PRIMARY KEY,
    comment    VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    owner_id   BIGINT                      NOT NULL,
    rating     INTEGER                     NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

ALTER SEQUENCE feedbacks_id_seq
    OWNED BY feedbacks.id;

ALTER TABLE feedbacks
    OWNER TO postgres_container;

-- Table: notebooks

DROP TABLE IF EXISTS notebooks;

CREATE TABLE IF NOT EXISTS notebooks
(
    id          BIGINT                      NOT NULL
        PRIMARY KEY ,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    description VARCHAR(255),
    owner_id    BIGINT                      NOT NULL,
    title       VARCHAR(255)                NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

ALTER SEQUENCE notebooks_id_seq
    OWNED BY notebooks.id;

ALTER TABLE notebooks
    OWNER TO postgres_container;

-- Table: notes

DROP TABLE IF EXISTS notes;

CREATE TABLE IF NOT EXISTS notes
(
    id          BIGINT                      NOT NULL
        PRIMARY KEY,
    content     VARCHAR(255)                NOT NULL,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    notebook_id BIGINT                      NOT NULL,
    title       VARCHAR(255)                NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

ALTER SEQUENCE notes_id_seq
    OWNED BY notes.id;

ALTER TABLE notes
    OWNER TO postgres_container;

-- Table: post_categories
DROP TABLE IF EXISTS post_categories;

CREATE TABLE IF NOT EXISTS post_categories
(
    id   BIGINT       NOT NULL
        primary key,
    name VARCHAR(255) NOT NULL
        CONSTRAINT post_categories_name_check
            CHECK ((name)::TEXT = ANY
                   ((ARRAY ['QUALITY_ASSURANCE'::CHARACTER VARYING, 'AUTOMATION_QA'::CHARACTER VARYING, 'MANUAL_TESTING'::CHARACTER VARYING, 'PERFORMANCE_TESTING'::CHARACTER VARYING, 'SECURITY_TESTING'::CHARACTER VARYING, 'TEST_MANAGEMENT'::CHARACTER VARYING])::TEXT[]))
);

ALTER SEQUENCE post_categories_id_seq
    OWNED BY post_categories.id;

ALTER TABLE post_categories
    OWNER TO postgres_container;

-- Table: posts
DROP TABLE IF EXISTS posts;

CREATE TABLE IF NOT EXISTS posts
(
    id          BIGINT                      NOT NULL
        PRIMARY KEY ,
    content     TEXT                        NOT NULL,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    description VARCHAR(255)                NOT NULL,
    owner_id    BIGINT                      NOT NULL,
    title       VARCHAR(255)                NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    category_id BIGINT
        constraint fkgj3uskfyllrt5ip3ag8xc8lfx
            references post_categories
);

ALTER SEQUENCE posts_id_seq
    OWNED BY posts.id;

ALTER TABLE posts
    OWNER TO postgres_container;
