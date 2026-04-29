-- Insertar datos de prueba
INSERT INTO members (first_name, last_name, email, active, created_at) VALUES
    ('Diego', 'Murillo', 'diego@library.com', TRUE, NOW()),
    ('Juan', 'Pérez', 'juan@library.com', TRUE, NOW()),
    ('Maria', 'García', 'maria@library.com', TRUE, NOW());

INSERT INTO books (title, author, isbn, available, created_at) VALUES
    ('Clean Code', 'Robert C. Martin', '978-0132350884', TRUE, NOW()),
    ('The Pragmatic Programmer', 'David Thomas', '978-0135957059', TRUE, NOW()),
    ('Design Patterns', 'Gang of Four', '978-0201633610', FALSE, NOW());

INSERT INTO loans (member_id, book_id, loan_date) VALUES
    (1, 1, NOW()),
    (2, 3, NOW());

-- Visualizar datos
SELECT 'MEMBERS:' AS tabla;
SELECT * FROM members;

SELECT 'BOOKS:' AS tabla;
SELECT * FROM books;

SELECT 'LOANS:' AS tabla;
SELECT * FROM loans;