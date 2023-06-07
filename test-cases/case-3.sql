BEGIN
WITH with_c AS (
    SELECT id, crap
    FROM schema_.table_c
)
UPDATE schema_.table_a SET (a, b, c) =
                               (SELECT first_name, last_name, crap FROM schema_.table_b
                                                                            JOIN with_c
                                                                                 USING (id)
                               );
END;