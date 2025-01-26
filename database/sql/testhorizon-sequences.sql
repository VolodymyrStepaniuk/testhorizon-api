-- SEQUENCE: users_id_seq

DROP SEQUENCE IF EXISTS users_id_seq;

CREATE SEQUENCE IF NOT EXISTS users_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE users_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: email_codes_id_seq

DROP SEQUENCE IF EXISTS email_codes_id_seq;

CREATE SEQUENCE IF NOT EXISTS email_codes_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE email_codes_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: authorities_id_seq

DROP SEQUENCE IF EXISTS authorities_id_seq;

CREATE SEQUENCE IF NOT EXISTS authorities_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE authorities_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: test_cases_id_seq

DROP SEQUENCE IF EXISTS test_cases_id_seq;

CREATE SEQUENCE IF NOT EXISTS test_cases_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE test_cases_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: test_case_priorities_id_seq

DROP SEQUENCE IF EXISTS test_case_priorities_id_seq;

CREATE SEQUENCE IF NOT EXISTS test_case_priorities_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE test_case_priorities_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: tests_id_seq

DROP SEQUENCE IF EXISTS tests_id_seq;

CREATE SEQUENCE IF NOT EXISTS tests_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE tests_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: test_types_id_seq

DROP SEQUENCE IF EXISTS test_types_id_seq;

CREATE SEQUENCE IF NOT EXISTS test_types_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE test_types_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: password_reset_tokens_id_seq

DROP SEQUENCE IF EXISTS password_reset_tokens_id_seq;

CREATE SEQUENCE IF NOT EXISTS password_reset_tokens_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE password_reset_tokens_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: ratings_id_seq

DROP SEQUENCE IF EXISTS ratings_id_seq;

CREATE SEQUENCE IF NOT EXISTS ratings_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE ratings_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: projects_id_seq

DROP SEQUENCE IF EXISTS projects_id_seq;

CREATE SEQUENCE IF NOT EXISTS projects_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE projects_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: project_statuses_id_seq

DROP SEQUENCE IF EXISTS project_statuses_id_seq;

CREATE SEQUENCE IF NOT EXISTS project_statuses_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE project_statuses_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: comments_id_seq

DROP SEQUENCE IF EXISTS comments_id_seq;

CREATE SEQUENCE IF NOT EXISTS comments_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE comments_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: bug_reports_id_seq

DROP SEQUENCE IF EXISTS bug_reports_id_seq;

CREATE SEQUENCE IF NOT EXISTS bug_reports_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE bug_reports_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: bug_report_statuses_id_seq

DROP SEQUENCE IF EXISTS bug_report_statuses_id_seq;

CREATE SEQUENCE IF NOT EXISTS bug_report_statuses_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE bug_report_statuses_id_seq
    OWNER TO postgres_container;

-- SEQUENCE: bug_report_severities_id_seq

DROP SEQUENCE IF EXISTS bug_report_severities_id_seq;

CREATE SEQUENCE IF NOT EXISTS bug_report_severities_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE bug_report_severities_id_seq
    OWNER TO postgres_container;