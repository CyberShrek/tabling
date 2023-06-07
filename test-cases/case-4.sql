BEGIN
WITH upd AS (
    SELECT sales_person, ddd FROM schema_.table_c WHERE name = 'Acme Corporation'
)
INSERT INTO schema_.table_a SELECT * FROM upd;
END;