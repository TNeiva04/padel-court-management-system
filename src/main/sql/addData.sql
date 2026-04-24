-- Delete all data from the tables
DELETE FROM Rental;
DELETE FROM Court;
DELETE FROM Club;
DELETE FROM User_Table;

-- Reset serial ids
ALTER SEQUENCE user_table_uid_seq RESTART WITH 1;
ALTER SEQUENCE club_cid_seq RESTART WITH 1;
ALTER SEQUENCE court_crid_seq RESTART WITH 1;
ALTER SEQUENCE rental_rid_seq RESTART WITH 1;

INSERT INTO User_Table (name, email, password, token)
VALUES
    ('João Silva', 'joao.silva@email.com', '1234', 'token1234'),
    ('Maria Oliveira', 'maria.oliveira@email.com', '1234', 'token5678'),
    ('Carlos Santos', 'carlos.santos@email.com', '1234', 'token91011');

INSERT INTO Club (name, owner)
VALUES
    ('Club A', 1),
    ('Club B', 2);

INSERT INTO Court (name, cid)
VALUES
    ('Court 1', 1),
    ('Court 2', 1),
    ('Court 3', 2);

INSERT INTO Rental (crid, uid, rental_date, rental_enddate, duration_hours)
VALUES
    (1, 1, '2025-03-26 10:00:00', '2025-03-26 12:00:00', 2),
    (2, 2, '2025-03-27 15:00:00', '2025-03-27 17:00:00', 2),
    (3, 3, '2025-03-28 09:00:00', '2025-03-28 11:00:00', 2);