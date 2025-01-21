-- Insert data into authorities table
INSERT INTO authorities (id, name)
VALUES
    (1, 'DEVELOPER'),
    (2, 'TESTER'),
    (3, 'ADMIN');

-- Insert data into project_statuses table
INSERT INTO project_statuses (id, name)
VALUES
    (1, 'ACTIVE'),
    (2, 'INACTIVE'),
    (3, 'PAUSED');

-- Insert data into test_case_priorities table
INSERT INTO test_case_priorities (id, name)
VALUES
    (1, 'LOW'),
    (2, 'MEDIUM'),
    (3, 'HIGH');

-- Insert data into test_types table
INSERT INTO test_types (id, name)
VALUES
    (1, 'UNIT'),
    (2, 'INTEGRATION'),
    (3, 'FUNCTIONAL'),
    (4, 'END_TO_END'),
    (5, 'ACCEPTANCE'),
    (6, 'PERFORMANCE'),
    (7, 'SMOKE');

-- Insert data into bug_report_severities table
INSERT INTO bug_report_severities (id, name)
VALUES
    (1, 'LOW'),
    (2, 'MEDIUM'),
    (3, 'HIGH'),
    (4, 'CRITICAL');

-- Insert data into bug_report_statuses table
INSERT INTO bug_report_statuses (id, name)
VALUES
    (1, 'OPENED'),
    (2, 'IN_PROGRESS'),
    (3, 'RESOLVED'),
    (4, 'CLOSED');

-- Insert data into users table
INSERT INTO users (id, created_at, email, first_name, is_account_non_expired, is_account_non_locked, is_credentials_non_expired, is_enabled, updated_at, last_name, password, total_rating)
VALUES
    (1, NOW(), 'john.doe@example.com', 'John', TRUE, TRUE, TRUE, TRUE, NOW(), 'Doe', 'Password@123', 100),
    (2, NOW(), 'jane.smith@example.com', 'Jane', TRUE, TRUE, TRUE, TRUE, NOW(), 'Smith', 'Password@123', 200);

-- Insert data into users_has_authorities table
INSERT INTO users_has_authorities (user_id, authority_id)
VALUES
    (1, 1),
    (2, 2);

-- Insert data into email_codes table
INSERT INTO email_codes (id, code, created_at, expires_at, user_id)
VALUES
    (1, 'ABC123', NOW(), NOW() + INTERVAL '1 day', 1),
    (2, 'XYZ789', NOW(), NOW() + INTERVAL '1 day', 2);

-- Insert data into projects table
INSERT INTO projects (id, created_at, description, github_url, image_urls, instructions, title, owner_id, updated_at, status_id)
VALUES
    (1, NOW(), 'Project 1 Description', 'https://github.com/project1', ARRAY['https://image1.com', 'https://image2.com'], 'Instructions 1', 'Project 1', 1, NOW(), 1),
    (2, NOW(), 'Project 2 Description', 'https://github.com/project2', ARRAY['https://image3.com', 'https://image4.com'], 'Instructions 2', 'Project 2', 2, NOW(), 2);

-- Insert data into test_cases table
INSERT INTO test_cases (id, author_id, created_at, description, input_data, preconditions, project_id, steps, title, updated_at, priority_id)
VALUES
    (1, 1, NOW(), 'Test Case 1 Description', 'Input Data 1', 'Preconditions 1', 1, ARRAY['Step 1', 'Step 2'], 'Test Case 1', NOW(), 1),
    (2, 2, NOW(), 'Test Case 2 Description', 'Input Data 2', 'Preconditions 2', 2, ARRAY['Step 3', 'Step 4'], 'Test Case 2', NOW(), 2);

-- Insert data into tests table
INSERT INTO tests (id, author_id, created_at, description, github_url, instructions, project_id, test_case_id, title, updated_at, type_id)
VALUES
    (1, 1, NOW(), 'Test 1 Description', 'https://github.com/test1', 'Instructions 1', 1, 1, 'Test 1', NOW(), 1),
    (2, 2, NOW(), 'Test 2 Description', 'https://github.com/test2', 'Instructions 2', 2, 2, 'Test 2', NOW(), 2);

-- Insert data into bug_reports table
INSERT INTO bug_reports (id, created_at, description, environment, image_urls, project_id, reporter_id, title, updated_at, severity_id, status_id, video_urls)
VALUES
    (1, NOW(), 'Bug Report 1 Description', 'Environment 1', ARRAY['https://image5.com', 'https://image6.com'], 1, 1, 'Bug Report 1', NOW(), 1, 1, ARRAY['https://video1.com', 'https://video2.com']),
    (2, NOW(), 'Bug Report 2 Description', 'Environment 2', ARRAY['https://image7.com', 'https://image8.com'], 2, 2, 'Bug Report 2', NOW(), 2, 2, ARRAY['https://video3.com', 'https://video4.com']);

-- Insert data into ratings table
INSERT INTO ratings (id, comment, created_at, rated_by_user_id, rating_points, user_id)
VALUES
    (1, 'Great work!', NOW(), 2, 5, 1),
    (2, 'Needs improvement.', NOW(), 1, 3, 2);

-- Insert data into comments table
INSERT INTO comments (id, author_id, content, created_at, updated_at, entity_type, entity_id)
VALUES
    (1, 1, 'Comment 1', NOW(), NOW(), 'PROJECT', 1),
    (2, 2, 'Comment 2', NOW(), NOW(), 'TEST_CASE', 1);