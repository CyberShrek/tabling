DECLARE
    i int = 0;
rec_a RECORD;
rec_b RECORD;
BEGIN

FOR i in 0..3 LOOP
	FOR rec_a IN
SELECT aa.a, bb.b, cc.c
FROM       schema_a.table_a aa
               LEFT  JOIN schema_b.table_b bb
               RIGHT JOIN schema_c.table_c cc
                          USING (id)
    LOOP
    FOR j IN 0..3 LOOP
    RAISE NOTICE 'iteration %', i + j;
END LOOP;
INSERT INTO schema_.table_
VALUES(rec_a.a, rec_a.b, rec_a.c);
END LOOP;

FOR rec_b IN
SELECT aa.sales_count
FROM   schema_a.table_a aa
    LOOP
		FOR j IN 0..3 LOOP
			RAISE NOTICE 'iteration %', i + j;
END LOOP;
UPDATE schema_.table_ SET sales_count = sales_count + rec_b.sales_count;
END LOOP;
END LOOP;
END;