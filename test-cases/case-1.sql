BEGIN
INSERT INTO schema_a.table_a
SELECT * FROM schema_b.table_a;
END;