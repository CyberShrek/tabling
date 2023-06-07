BEGIN
UPDATE schema_a.table_a SET sales_count = sales_count + 1
FROM schema_a.table_b
WHERE accounts.name = 'Acme Corporation'
  AND schema_a.table_a.id = schema_a.table_a.sales_person;
END;