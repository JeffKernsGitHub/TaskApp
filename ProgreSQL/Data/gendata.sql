-- =============================================================================
-- Test Data Generation Script
-- Database: app_db
-- Schema: tasks
--
-- Seed Accounts & Credentials:
--   - Admin Account (for Insomnia / Milestone 3 verification):
--       Username: admin_boss | Password: AdminMasterSecret123! | Role: ADMIN
--   - Seeded ADMIN Accounts ('Johnson, Mary B.', 'Miller, Michael G.'):
--       Password: AdminMasterSecret123! | Role: ADMIN
--   - Seeded USER Accounts (50 users):
--       Password: Password123! | Role: USER
-- =============================================================================

-- Insert Initial Users (admin, alice, admin_boss + 50 sample users)
INSERT INTO tasks.users (username, email, password_hash, role) VALUES
    ('admin', 'admin@taskmanager.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'ADMIN'),
    ('alice', 'alice@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('admin_boss', 'admin.boss@example.com', '$2a$10$X.MXax/vjkj1hHsPSUk.g.j6CRAC2Iv6HF4TjPlRFL2tVwgGGCU5m', 'ADMIN'),
    ('Smith, John A.', 'john.smith@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Johnson, Mary B.', 'mary.johnson@example.com', '$2a$10$X.MXax/vjkj1hHsPSUk.g.j6CRAC2Iv6HF4TjPlRFL2tVwgGGCU5m', 'ADMIN'),
    ('Williams, James C.', 'james.williams@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Brown, Patricia D.', 'patricia.brown@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Jones, Robert E.', 'robert.jones@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Garcia, Jennifer F.', 'jennifer.garcia@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Miller, Michael G.', 'michael.miller@example.com', '$2a$10$X.MXax/vjkj1hHsPSUk.g.j6CRAC2Iv6HF4TjPlRFL2tVwgGGCU5m', 'ADMIN'),
    ('Davis, Linda H.', 'linda.davis@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Rodriguez, William I.', 'william.rodriguez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Martinez, Elizabeth J.', 'elizabeth.martinez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Hernandez, David K.', 'david.hernandez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Lopez, Barbara L.', 'barbara.lopez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Gonzalez, Richard M.', 'richard.gonzalez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Wilson, Susan N.', 'susan.wilson@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Anderson, Joseph O.', 'joseph.anderson@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Thomas, Jessica P.', 'jessica.thomas@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Taylor, Thomas Q.', 'thomas.taylor@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Moore, Sarah R.', 'sarah.moore@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Jackson, Charles S.', 'charles.jackson@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Martin, Karen T.', 'karen.martin@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Lee, Christopher U.', 'christopher.lee@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Perez, Nancy V.', 'nancy.perez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Thompson, Daniel W.', 'daniel.thompson@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('White, Lisa X.', 'lisa.white@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Harris, Matthew Y.', 'matthew.harris@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Sanchez, Betty Z.', 'betty.sanchez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Clark, Anthony A.', 'anthony.clark@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Ramirez, Margaret B.', 'margaret.ramirez@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Lewis, Mark C.', 'mark.lewis@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Robinson, Sandra D.', 'sandra.robinson@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Walker, Donald E.', 'donald.walker@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Young, Ashley F.', 'ashley.young@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Allen, Steven G.', 'steven.allen@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('King, Kimberly H.', 'kimberly.king@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Wright, Paul I.', 'paul.wright@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Scott, Emily J.', 'emily.scott@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Torres, Andrew K.', 'andrew.torres@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Nguyen, Donna L.', 'donna.nguyen@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Hill, Joshua M.', 'joshua.hill@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Flores, Michelle N.', 'michelle.flores@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Green, Kenneth O.', 'kenneth.green@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Adams, Dorothy P.', 'dorothy.adams@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Nelson, Kevin Q.', 'kevin.nelson@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Baker, Carol R.', 'carol.baker@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Hall, Brian S.', 'brian.hall@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Rivera, Amanda T.', 'amanda.rivera@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Campbell, George U.', 'george.campbell@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Mitchell, Melissa V.', 'melissa.mitchell@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Carter, Edward W.', 'edward.carter@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER'),
    ('Roberts, Deborah X.', 'deborah.roberts@example.com', '$2a$10$3AwBecmf6Em4y4hnVVDdCe/lGks0Pb8hjvmTc44QxTrha2KMkaMsW', 'USER');

-- Generate 1000 Tasks assigned to random existing users with future due dates
INSERT INTO tasks.tasks (user_id, title, description, status, priority, due_date)
SELECT
    (random() * 50 + 1)::bigint AS user_id,
    'Task Item Number ' || generate_series AS title,
    'Auto-generated description for task item number ' || generate_series AS description,
    (ARRAY['TODO'::tasks.task_status, 'IN_PROGRESS'::tasks.task_status, 'DONE'::tasks.task_status])[floor(random() * 3 + 1)] AS status,
    (ARRAY['LOW'::tasks.task_priority, 'MEDIUM'::tasks.task_priority, 'HIGH'::tasks.task_priority])[floor(random() * 3 + 1)] AS priority,
    CURRENT_DATE + (random() * 90)::integer AS due_date
FROM generate_series(1, 1000);
commit;
