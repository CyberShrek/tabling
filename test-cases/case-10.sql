DECLARE
    req_dt date;
kolz bigint;
seq1 bigint;

BEGIN
	-- при смене схемы, не забыть, что она присутствует в именах последовательностей в явном виде!
SET search_path to rawdl2_day;
RAISE INFO 'Начало работы в %.', to_char(clock_timestamp()::time, 'HH24:MI:SS');
req_dt = $1;
	--определяем дату как предыдущий день от текущей даты или от переданной в ф-ию даты
IF req_dt IS null then
SELECT now()::date - interval '1 day' INTO req_dt;
END IF;
RAISE INFO '% Определена дата: %.', to_char(clock_timestamp()::time, 'HH24:MI:SS'), req_dt;

DELETE FROM link_els_day WHERE id_els IN (SELECT DISTINCT id_els FROM els_day WHERE _m_date=req_dt);
GET DIAGNOSTICS kolz = ROW_COUNT;
RAISE INFO 'Удалено % записей из link_els_day от предыдущего запуска.', kolz;

DELETE FROM els_day where _m_date = req_dt;
GET DIAGNOSTICS kolz = ROW_COUNT;
RAISE INFO 'Удалено % записей из els_day от предыдущего запуска.', kolz;

--    SELECT NEXTVAL('rawdl2_day.els_day_id_els_seq') INTO seq1;

DROP TABLE IF EXISTS els_tt0;
CREATE TABLE els_tt0 as
    (
        WITH
-- Параметры, передаваемые сервису. Сервис должен подставлять значения из входных данных запроса
--	ag
params AS (SELECT -1 AS ag, -1 AS vs, '' AS fd, req_dt::date AS dt1),

-- Первичный запрос данных по записям за выбранный период по выбранному агенту
t9 AS (
    SELECT
        t902.agent_code, t904.subagent_code, t902.els_code,	t904.dor_code, -- Коды агента, субагента, ЕЛС и кода дороги-филиала держателя договора
        COALESCE(t902.oper_date, t902._m_date) AS oper_date, -- Дата оформления
        COALESCE(t902.posting_date, t902.oper_date, t902._m_date) AS posting_date, -- Дата проводки
        t902._m_date AS d2, -- Дата операции
        t902.flags_process[8] AS sd, -- Признак старого договора
        t904.flags, -- Флаги (признаки)
        CASE WHEN t902.flags_process[3] THEN 1
             ELSE 2
            END AS wo_code, -- Код вида оформления
        CASE WHEN t902.flags_process[3] THEN 'Р'
             ELSE 'Э'
            END AS wo, -- Вид оформления
        CASE WHEN t904._m_date >= t904.begin_contract_date AND t904._m_date <= t904.end_contract_date THEN 1
             ELSE 0
            END AS pnd, -- Признак наличия договора (проверка по наличию договора на дату операции)
        t904.flags[5] AS pko, -- Признак контроля остатка
        CASE WHEN t904.dor_code = '' THEN 1
             ELSE 2 END AS ebd, -- Признак ЕЛС без договора (по отсутствию данных филиала-держателя)

        t902.request_type_oper, t902.request_subtype_oper, -- Коды типа и подтипа операции
        t902._m_vidr, t902._m_pvid, t902._m_arch, -- Коды вида и подвида работы и записи в архив
        t902.corr_sum, t902.flags_process, -- сумма проводки и флаги признаков
        t902.id AS t9_id, -- ID документов нижнего уровня
        tc._m_surk, -- Код связки таблиц
        tc.id AS tc_id, -- ID таблиц верхнего уровня
        t902.ticket_ser AS t9_tser, -- Серия документа в таблицах нижнего уровня
        t902.ticket_num AS t9_tnum --  Номер документа в таблицах нижнего уровня
    FROM rawd.t1_00902 AS t902
             JOIN params ON true
             JOIN rawd.t1_00904 AS t904 ON t904.id = t902.id
             INNER JOIN rawd.t1_common AS tc ON tc._m_surk=t902._m_surk -- Связка между таблицами верхнего и нижнего уровня через rawd.t1_common
        AND tc._m_arch IN (17,25)
        AND	tc._m_repl=t902._m_repl/10*10 -- здесь делается обнуление последней цифры _m_repl из t1_00902
    WHERE
        CASE WHEN params.ag = -1 THEN true -- Если входной параметр ag агента равен -1 - берутся все агенты
             WHEN t902.agent_code = params.ag THEN true -- Иначе проверяется соответствие агента входному параметру
             ELSE false END
--		t902.agent_code = params.ag
      AND CASE WHEN params.vs = -1 THEN true -- Если входной параметр vs агента равен -1 - берутся все субагенты
               WHEN params.vs = 0 AND t904.subagent_code <= 0 THEN true -- Если входной параметр vs агента равен 0 - отчёт по юр.лицам
               WHEN params.vs > 0 AND t904.subagent_code > 0 THEN true --  Если входной параметр vs агента больше 0 - отчёт коммерческим кассам
               ELSE false END
      AND t902._m_date >= params.dt1 AND t902._m_date <= params.dt1 -- Берём документы только за заданную дату
      AND CASE WHEN t904.dor_code = params.fd THEN true -- При необходимости фильтруем по коду филиала (дороги) держателя договора
               WHEN params.fd = '' THEN true
               ELSE false END
),

-- Из полученных записей t9 формируем записи по видам документов
-- пассажирские перевозки
pass AS (SELECT
             DISTINCT(t9.t9_id), t9.tc_id, t9.wo, t9.els_code, t9.dor_code, t9.ebd, t9.agent_code, t9.subagent_code, -- Основные элементы из таблиц нижнего уровня
                     CASE WHEN t9.subagent_code = 0 THEN tl2.agent_code ELSE t9.agent_code END AS sale_agent,
                     t9.pnd, t9.pko, t9._m_vidr, t9._m_pvid, t9._m_arch,
                     t9.sd, t9.request_type_oper, t9.request_subtype_oper, t9.corr_sum, t9_tser, t9_tnum,
                     tl2.term_pos,
                     tl2.term_dor,
                     tl2.term_trm,
                     CONCAT(tl2.term_pos, tl2.term_dor, tl2.term_trm) AS ht,
                     tl2.oper, tl2.oper_g, '' AS pay_code,
                     tl2.request_type, tl2.request_subtype,
--	CASE WHEN request_type IN ('10', '26') THEN 1
--		 END AS wt_code,
                     1 AS wt_code,
                     'П' AS wt,
-- 	CASE WHEN tl2.oper = 'O' AND tl2.oper_g = 'N' THEN 1
-- 		WHEN tl2.oper = 'V' AND request_type = '26' AND request_subtype = '26' THEN 4
-- 		WHEN tl2.oper = 'V' THEN 2
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 3
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 5
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 6
-- 	END o_code,
-- 	CASE WHEN tl2.oper = 'O' AND tl2.oper_g= 'N' THEN ''
-- 		WHEN tl2.oper = 'V' AND request_type = '26' AND request_subtype = '26' THEN 'ЧТВЗ'
-- 		WHEN tl2.oper = 'V' THEN 'ВЗ'
-- 		WHEN tl2.oper_g = 'G'  AND NOT t9.flags_process[7] THEN 'ГШ'
-- 		WHEN tl2.oper_g = 'G'  AND t9.flags_process[7] THEN 'ВЗ'
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 'О'
-- 	END o,
                     request_num AS zk,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 1
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 4
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 5
-- 	 ELSE 6 END AS wr_code,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 'ПЮ'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 'Н'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 'ЭФ'
-- 	 ELSE 'ЛГ' END AS wr,
                     '' AS nvg,
                     '' AS kpl2,
                     request_time AS vro,

                     CASE WHEN t9.subagent_code = 0 AND t9._m_vidr = '9' AND t9._m_pvid = '4' AND NOT t9.flags[6] THEN 1
--		WHEN t9.subagent_code = 0 AND substr(t9.t9_tser,3,1) IN ('I', 'И') AND tl2.flg_eticket='1' AND flg_checktape = '0' THEN 2
                          ELSE 0
                         END AS spr_code,
                     'pass' AS src_tbl

--	tl2.ticket_ser AS tl2_tser,
--	tl2.ticket_num AS tl2_tnum
         FROM t9
                  INNER JOIN rawdl2.l2_pass_main AS tl2 ON tl2.id=t9.tc_id
--	 		AND tl2.ticket_num=t9.t9_tnum -- Отсеиваются групповые документы
         WHERE t9._m_vidr='9' AND ((t9._m_pvid='98' AND t9._m_arch='22') OR (t9._m_pvid<>'98' AND t9._m_arch IN ('17','25')))
),

-- Перевозка багажа
bag AS (SELECT
            DISTINCT(t9.t9_id), t9.tc_id, t9.wo, t9.els_code, t9.dor_code, t9.ebd, t9.agent_code, t9.subagent_code,
                    CASE WHEN t9.subagent_code = 0 THEN tl2.agent_code ELSE t9.agent_code END AS sale_agent,
                    t9.pnd, t9.pko, t9._m_vidr, t9._m_pvid, t9._m_arch,
                    t9.sd, t9.request_type_oper, t9.request_subtype_oper, t9.corr_sum, t9_tser, t9_tnum,
                    tl2.term_pos,
                    tl2.term_dor,
                    tl2.term_trm,
                    CONCAT(tl2.term_pos, tl2.term_dor, tl2.term_trm) AS ht,
                    tl2.oper, tl2.oper_g, '' AS pay_code,
                    tl2.request_type, tl2.request_subtype,
                    2 AS wt_code,
                    shipment_type AS wt,
-- 	CASE WHEN tl2.oper = 'O' AND tl2.oper_g = 'N' THEN 1
-- 		WHEN tl2.oper = 'V' THEN 2
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 3
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 5
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 6
-- 	END o_code,
-- 	CASE WHEN tl2.oper = 'O' AND oper_g = 'N' THEN ''
-- 		WHEN tl2.oper = 'V' THEN 'ВЗ'
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 'ГШ'
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 'ВЗ'
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 'О'
-- 	END o,
                    request_num AS zk,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 2
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = 'Т' THEN 3
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 4
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 5
-- 	 ELSE 6 END AS wr_code,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 'Ч'
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = 'Т' THEN 'Ц'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 'Н'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 'ЭФ'
-- 	 ELSE 'ЛГ' END AS wr,
                    carriage_num8 AS nvg,
                    '' AS kpl2,
                    request_time AS vro,
                    CASE WHEN t9.subagent_code = 0 AND t9._m_vidr = '9' AND t9._m_pvid = '4' AND NOT t9.flags[6] THEN 1
--		WHEN t9.subagent_code = 0 AND substr(t9.t9_tser,3,1) IN ('I', 'И') AND tl2.flg_eticket='1' AND flg_checktape = '0' THEN 2
                         ELSE 0
                        END AS spr_code,
                    'bag' AS src_tbl


--	tl2.ticket_ser AS tl2_tser,
--	tl2.ticket_num AS tl2_tnum
        FROM t9
                 INNER JOIN rawdl2.l2_bag_main AS tl2 ON tl2.id=t9.tc_id
--	 		AND tl2.ticket_num=t9.t9_tnum
        WHERE t9._m_vidr='9' AND ((t9._m_pvid='98' AND t9._m_arch='22') OR (t9._m_pvid<>'98' AND t9._m_arch IN ('17','25')))
),

-- Питание
meal AS (SELECT
             DISTINCT(t9.t9_id), t9.tc_id, t9.wo, t9.els_code, t9.dor_code, t9.ebd, t9.agent_code, t9.subagent_code,
                     CASE WHEN t9.subagent_code = 0 THEN tl2.agent_code ELSE t9.agent_code END AS sale_agent,
                     t9.pnd, t9.pko, t9._m_vidr, t9._m_pvid, t9._m_arch,
                     t9.sd, t9.request_type_oper, t9.request_subtype_oper, t9.corr_sum, t9_tser, t9_tnum,
                     tl2.term_pos,
                     tl2.term_dor,
                     tl2.term_trm,
                     CONCAT(tl2.term_pos, tl2.term_dor, tl2.term_trm) AS ht,
                     tl2.oper, tl2.oper_g, '' AS pay_code,
                     tl2.request_type, tl2.request_subtype,
                     1 AS wt_code,
                     'П' AS wt,
-- 	CASE WHEN tl2.oper = 'O' AND tl2.oper_g = 'N' THEN 1
-- 		WHEN tl2.oper = 'V' THEN 2
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 3
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 5
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 6
-- 	END o_code,
-- 	CASE WHEN tl2.oper = 'O' AND oper_g = 'N' THEN ''
-- 		WHEN tl2.oper = 'V' THEN 'ВЗ'
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 'ГШ'
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 'ВЗ'
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 'О'
-- 	END o,
                     request_num AS zk,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 1
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 4
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 5
-- 	 ELSE 6 END AS wr_code,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 'ПЮ'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 'Н'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 'ЭФ'
-- 	 ELSE 'ЛГ' END AS wr,
                     '' AS nvg,
                     '' AS kpl2,
                     request_time AS vro,
                     CASE WHEN t9.subagent_code = 0 AND t9._m_vidr = '9' AND t9._m_pvid = '4' AND NOT t9.flags[6] THEN 1
--		WHEN t9.subagent_code = 0 AND substr(t9.t9_tser,3,1) IN ('I', 'И') AND web_id > '' THEN 2
                          ELSE 0
                         END AS spr_code,
                     'meal' AS src_tbl


--	tl2.ticket_ser AS tl2_tser,
--	tl2.ticket_num AS tl2_tnum
         FROM t9
                  INNER JOIN rawdl2.l2_meal AS tl2 ON tl2.id=t9.tc_id
--	 		AND tl2.ticket_num=t9.t9_tnum
         WHERE t9._m_vidr='9' AND ((t9._m_pvid='98' AND t9._m_arch='22') OR (t9._m_pvid<>'98' AND t9._m_arch IN ('17','25')))
),

-- Услуги
krs AS (SELECT
            DISTINCT(t9.t9_id), t9.tc_id, t9.wo, t9.els_code, t9.dor_code, t9.ebd, t9.agent_code, t9.subagent_code,
                    CASE WHEN t9.subagent_code = 0 THEN tl2.agent_code ELSE t9.agent_code END AS sale_agent,
                    t9.pnd, t9.pko, t9._m_vidr, t9._m_pvid, t9._m_arch,
                    t9.sd, t9.request_type_oper, t9.request_subtype_oper, t9.corr_sum, t9_tser, t9_tnum,
                    tl2.term_pos,
                    tl2.term_dor,
                    tl2.term_trm,
                    CONCAT(tl2.term_pos, tl2.term_dor, tl2.term_trm) AS ht,
                    tl2.oper, tl2.oper_g, tl2.pay_code,
                    tl2.request_type, tl2.request_subtype,
                    1 AS wt_code,
                    'П' AS wt,
-- 	CASE WHEN tl2.oper = 'O' AND tl2.oper_g = 'N' THEN 1
-- 		WHEN tl2.oper = 'V' THEN 2
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 3
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 5
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 6
-- 	END o_code,
-- 	CASE WHEN tl2.oper = 'O' AND oper_g = 'N' THEN ''
-- 		WHEN tl2.oper = 'V' THEN 'ВЗ'
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 'ГШ'
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 'ВЗ'
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 'О'
-- 	END o,
                    request_num AS zk,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 1
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 4
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 5
-- 	 ELSE 6 END AS wr_code,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 'ПЮ'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 'Н'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 'ЭФ'
-- 	 ELSE 'ЛГ' END AS wr,
                    '' AS nvg,
                    pay_code AS kpl2,
                    request_time AS vro,
                    CASE WHEN t9.subagent_code = 0 AND t9._m_vidr = '9' AND t9._m_pvid = '4' AND NOT t9.flags[6] THEN 1
--		WHEN t9.subagent_code = 0 AND substr(t9.t9_tser,3,1) IN ('I', 'И') AND flg_internet = '1' THEN 2
                         ELSE 0
                        END AS spr_code,
                    'krs' AS src_tbl

--	tl2.ticket_ser AS tl2_tser,
--	tl2.ticket_num AS tl2_tnum
        FROM t9
                 INNER JOIN rawdl2.l2_krs AS tl2 ON tl2.id=t9.tc_id
--	 		AND tl2.ticket_num=t9.t9_tnum
        WHERE t9._m_vidr='9' AND ((t9._m_pvid='98' AND t9._m_arch='22') OR (t9._m_pvid<>'98' AND t9._m_arch IN ('17','25')))
),

-- Карты
cards AS (SELECT
              DISTINCT(t9.t9_id), t9.tc_id, t9.wo, t9.els_code, t9.dor_code, t9.ebd, t9.agent_code, t9.subagent_code,
                      CASE WHEN t9.subagent_code = 0 THEN tl2.agent_code ELSE t9.agent_code END AS sale_agent,
                      t9.pnd, t9.pko, t9._m_vidr, t9._m_pvid, t9._m_arch,
                      t9.sd, t9.request_type_oper, t9.request_subtype_oper, t9.corr_sum, t9_tser, t9_tnum,
                      tl2.term_pos,
                      tl2.term_dor,
                      tl2.term_trm,
                      CONCAT(tl2.term_pos, tl2.term_dor, tl2.term_trm) AS ht,
                      tl2.oper, tl2.oper_g, '' AS pay_code,
                      tl2.request_type, tl2.request_subtype,
                      1 AS wt_code,
                      'П' AS wt,
-- 	CASE WHEN tl2.oper = 'O' AND tl2.oper_g = 'N' THEN 1
-- 		WHEN tl2.oper = 'V' THEN 2
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 3
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 5
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 6
-- 	END o_code,
-- 	CASE WHEN tl2.oper = 'O' AND oper_g = 'N' THEN ''
-- 		WHEN tl2.oper = 'V' THEN 'ВЗ'
-- 		WHEN tl2.oper_g = 'G' AND NOT t9.flags_process[7] THEN 'ГШ'
-- 		WHEN tl2.oper_g = 'G' AND t9.flags_process[7] THEN 'ВЗ'
-- 		WHEN tl2.oper = 'O' AND tl2.oper_g = 'O' THEN 'О'
-- 	END o,
                      request_num AS zk,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 1
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 4
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 5
-- 	 ELSE 6 END AS wr_code,
-- 	CASE
-- 		 WHEN t9.subagent_code <= 0 AND paymenttype = '6' THEN 'ПЮ'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '1' THEN 'Н'
-- 		 WHEN t9.subagent_code > 0 AND paymenttype = '8' THEN 'ЭФ'
-- 	 ELSE 'ЛГ' END AS wr,
                      '' AS nvg,
                      '' AS kpl2,
                      request_time AS vro,
                      CASE WHEN t9.subagent_code = 0 AND t9._m_vidr = '9' AND t9._m_pvid = '4' AND NOT t9.flags[6] THEN 1
--		WHEN t9.subagent_code = 0 AND substr(t9.t9_tser,3,1) IN ('I', 'И') AND flg_internet = '1' THEN 2
                           ELSE 0
                          END AS spr_code,
                      'cards' AS src_tbl

--	tl2.ticket_ser AS tl2_tser,
--	tl2.ticket_num AS tl2_tnum
          FROM t9
                   INNER JOIN rawdl2.l2_cards AS tl2 ON tl2.id=t9.tc_id
--	 		AND tl2.ticket_num=t9.t9_tnum
          WHERE t9._m_vidr='9' AND ((t9._m_pvid='98' AND t9._m_arch='22') OR (t9._m_pvid<>'98' AND t9._m_arch IN ('17','25')))
),

-- Объединяем полученные документы
docs_union AS (
    SELECT * FROM pass
    UNION SELECT * FROM bag
    UNION SELECT * FROM meal
    UNION SELECT * FROM krs
    UNION SELECT * FROM cards),

-- Добавляем специальный признак
docs AS (
    SELECT docs_union.*,
--  		-- Формируем признак отказного документа
--   		CASE WHEN LEAD(o) OVER (ORDER BY t9_tser, t9_tnum, t9_id) = 'О'
--   --				AND (LEAD(corr_sum)  OVER (ORDER BY t9_tser, t9_tnum, t9_id) + corr_sum = 0)
--   				THEN true
--   			ELSE false
--   		END AS annuled,
--		CASE WHEN LEAD(o) OVER (ORDER BY t9_tser, t9_tnum, t9_id) = 'ЧТВЗ'
--				AND (LEAD(corr_sum)  OVER (ORDER BY t9_tser, t9_tnum, t9_id) + corr_sum = 0)
--				THEN true
--			ELSE false
--		END AS vz_annuled,
           CASE WHEN spr_code = '1' THEN 'б/д'
                WHEN spr_code = '2' THEN 'IE'
                ELSE ''
               END AS spr

    FROM docs_union
),

-- Все записи по суммам поступлений
spb AS (SELECT t902.id AS t9_id, tc.id AS tc_id, t902.els_code, t904.dor_code, t902.agent_code, t904.subagent_code, t902._m_date,
               COALESCE(t902.posting_date, t902._m_date) AS dof, null AS wt,
               CASE WHEN t902.flags_process[3] THEN 'Р'
                    ELSE 'Э'
                   END AS wo,
               CASE WHEN t904._m_date >= t904.begin_contract_date AND t904._m_date <= t904.end_contract_date THEN 1
                    ELSE 0
                   END AS pnd,
               t904.flags[5] AS pko,
               CASE WHEN t904.dor_code = '' THEN 1
                    ELSE 2 END AS ebd,
               t902._m_vidr AS rv, -- 17. Вид работы
               t902._m_pvid AS rp, -- 18. Подвид работы,

               (CASE WHEN t902._m_vidr='9' AND t902._m_pvid ='1'
                   AND t902.els_type!='2' -- !!! Тип поступлений для исключения задваивания с обеспечительными платежами
                   AND (t901.request_mode='1' or t901.request_mode='0')
                         THEN t902.corr_sum ELSE 0 END) AS spb_bnupp, -- из БНУПП
               0 AS spb_cash,
               (CASE WHEN t902._m_vidr='9' AND t902._m_pvid ='98'
                   AND t902.request_type_oper ='17' AND t902.request_subtype_oper ='795'
                   AND tkrs.pay_code IN ('0194', '0195', '0196', '0197', '0198') -- !!! Не находит документов, например для ЕЛС 1000073770 spb_gsh за 15.12.2022
                   AND tkrs.oper = 'O' AND tkrs.oper_g = 'G'
                         THEN t902.corr_sum ELSE 0 END) AS spb_gsh -- Гашение
        FROM rawd.t1_00902 AS t902
                 JOIN params ON true
                 JOIN rawd.t1_00904 AS t904 ON t904.id = t902.id
                 JOIN rawd.t1_00901 AS t901 ON t901.id = t902.id
                 LEFT JOIN rawd.t1_common AS tc ON tc._m_surk=t902._m_surk
            AND tc._m_arch IN (17,25)
            AND	tc._m_repl=t902._m_repl/10*10 -- здесь делается обнуление последней цифры _m_repl из t1_00902
                 LEFT JOIN rawdl2.l2_krs AS tkrs ON tkrs.id = tc.id AND tkrs.ticket_num = t902.ticket_num
        WHERE
            CASE WHEN params.ag = -1 THEN true
                 WHEN t902.agent_code = params.ag THEN true
                 ELSE false END
--		t902.agent_code = params.ag --AND t904.subagent_code = 0
          AND t902._m_date >= params.dt1 AND t902._m_date <= params.dt1
          AND CASE WHEN params.vs = -1 THEN true
                   WHEN params.vs = 0 AND t904.subagent_code <= 0 THEN true
                   WHEN params.vs > 0 AND t904.subagent_code > 0 THEN true
                   ELSE false END
          AND CASE WHEN t904.dor_code = params.fd THEN true
                   WHEN params.fd = '' THEN true
                   ELSE false END
          -- Обязательное условие для таблиц нижнего уровня
          AND t902._m_vidr='9' AND ((t902._m_pvid='98' AND t902._m_arch='22') OR (t902._m_pvid<>'98' AND t902._m_arch IN ('17','25')))
),

-- Раскладываем поступления по документам
spb_docs AS (
    SELECT t9_id, tc_id,
           spb_bnupp,
           spb_cash,
           spb_gsh,
           spb_bnupp+spb_cash+spb_gsh AS spb_all
    FROM spb
),

-- Суммируем поступления по ЕЛС и объединяем в массивы ID записей таблиц верхнего и нижнего уровней
sum_spb AS (
    SELECT els_code, dor_code, ebd, agent_code, subagent_code, dof,
           wt, wo, pnd, pko, rv, rp,
           SUM(spb_bnupp) AS spb_bnupp,
           SUM(spb_cash) AS spb_cash,
           SUM(spb_gsh) AS spb_gsh,
           array_agg(t9_id) AS t9_arr,
           array_agg(tc_id) AS tc_arr
    FROM spb
    WHERE spb_bnupp != 0 OR spb_cash != 0 OR spb_gsh != 0
    GROUP BY els_code, dor_code, ebd, agent_code, subagent_code, dof, wt, wo, pnd, pko, rv, rp
),

-- Приводим полученные данные по поступлениям к единому формату для объединения с другими подзапросами
spb_agg AS (
    SELECT els_code, dor_code, ebd, agent_code, subagent_code, agent_code AS sale_agent, pnd, pko,
           '000' AS term_pos,
           '' AS term_dor,
           '00' term_trm,
           dof,
           wt, wo, rv, rp, '' AS spr,
           0 AS sop, spb_bnupp+spb_gsh AS spb_all, --spb_bnupp+spb_cash+spb_gsh AS spb_all, -- Внесение наличных в кассу перенесено в S4
           0 AS s4_pp, 0 AS s4_krs, 0 AS s4_all, 0 AS ssd_all, t9_arr, tc_arr, 'spb' AS src_tbl
    FROM sum_spb
),

-- Записи сумм поступлений обеспечительного платежа
sop AS (
    SELECT t902.id AS t9_id, tc.id AS tc_id, t902.els_code, t904.dor_code, t902.agent_code, t904.subagent_code, t902._m_date,
           COALESCE(t902.posting_date, t902._m_date) AS dof, null AS wt,
           CASE WHEN t902.flags_process[3] THEN 'Р'
                ELSE 'Э'
               END AS wo,
           CASE WHEN t904._m_date >= t904.begin_contract_date AND t904._m_date <= t904.end_contract_date THEN 1
                ELSE 0
               END AS pnd,
           t904.flags[5] AS pko,
           CASE WHEN t904.dor_code = '' THEN 1
                ELSE 2 END AS ebd,
           t902._m_vidr AS rv, -- 17. Вид работы
           t902._m_pvid AS rp, -- 18. Подвид работы,

           (CASE WHEN t902._m_vidr='9' AND t902._m_pvid ='1'
               AND t902.els_type='2'
                     THEN t902.corr_sum ELSE 0 END) AS sop -- из БНУПП
    FROM rawd.t1_00902 AS t902
             JOIN params ON true
             JOIN rawd.t1_00904 AS t904 ON t904.id = t902.id
             LEFT JOIN rawd.t1_common AS tc ON tc._m_surk=t902._m_surk
        AND tc._m_arch IN (17,25)
        AND	tc._m_repl=t902._m_repl/10*10 -- здесь делается обнуление последней цифры _m_repl из t1_00902
    WHERE
        CASE WHEN params.ag = -1 THEN true
             WHEN t902.agent_code = params.ag THEN true
             ELSE false END
--		t902.agent_code = params.ag --AND t904.subagent_code = 0
      AND t902._m_date >= params.dt1 AND t902._m_date <= params.dt1
      AND CASE WHEN params.vs = -1 THEN true
               WHEN params.vs = 0 AND t904.subagent_code <= 0 THEN true
               WHEN params.vs > 0 AND t904.subagent_code > 0 THEN true
               ELSE false END
      AND CASE WHEN t904.dor_code = params.fd THEN true
               WHEN params.fd = '' THEN true
               ELSE false END
      -- Обязательное условие для таблиц нижнего уровня
      AND t902._m_vidr='9' AND ((t902._m_pvid='98' AND t902._m_arch='22') OR (t902._m_pvid<>'98' AND t902._m_arch IN ('17','25')))
),

-- Раскладываем поступления О/П по документам
sop_docs AS (
    SELECT t9_id, tc_id,
           sop
    FROM sop
),

-- Суммируем поступления О/П по ЕЛС и объединяем ID записей в массивы
sum_sop AS (
    SELECT els_code, dor_code, ebd, agent_code, subagent_code,
           dof, wt, wo, pnd, pko, rv, rp,
           SUM(sop) AS sop,
           array_agg(t9_id) AS t9_arr,
           array_agg(tc_id) AS tc_arr
    FROM sop
    WHERE sop != 0
    GROUP BY els_code, dor_code, ebd, agent_code, subagent_code, dof, wt, wo, pnd, pko, rv, rp
),

-- Приводим полученные данные по поступлениям О/П к единому формату для объединения с другими подзапросами
sop_agg AS (
    SELECT els_code, dor_code, ebd, agent_code, subagent_code, agent_code AS sale_agent, pnd, pko,
           '000' AS term_pos,
           '' AS term_dor,
           '00' term_trm,
           dof, wt, wo, rv, rp, '' AS spr,
           sop, 0 AS spb_all, -- Внесение наличных в кассу перенесено в S4
           0 AS s4_pp, 0 AS s4_krs, 0 AS s4_all, 0 AS ssd_all, t9_arr, tc_arr, 'sop' AS src_tbl
    FROM sum_sop
),

-- Записи по провозным платежам и услугам
s4 AS (SELECT
           t9_id, tc_id, els_code, dor_code,
           ebd, agent_code, subagent_code, sale_agent, pnd, pko, term_pos, term_dor, term_trm,
           wt, wo,
           COALESCE(docs.request_type, _m_vidr) AS rv, -- 17. Вид работы
           COALESCE(docs.request_subtype, _m_pvid) AS rp, -- 18. Подвид работы,
           spr, sd, src_tbl,
--	   t9.tl2_tser, t9.tl2_tnum,
           (CASE WHEN
                             request_type_oper IN ('10', '17') AND request_subtype_oper IN ('0', '15')
                     THEN -corr_sum
                 ELSE 0 END)	AS pass_sale, -- 19.1. Продажа пассажиры
           (CASE WHEN
                             request_type_oper = '71' AND request_subtype_oper IN ('10','13','16', '70', '80', '71', '81')
                         AND oper = 'O'
                     THEN -corr_sum
                 ELSE 0 END)	AS bag_sale, -- 19.2. Продажа багаж
           (CASE WHEN
                             request_type_oper = '26' AND request_subtype_oper IN ('20','21','26')
                     THEN -corr_sum
                 ELSE 0 END)	AS vz_pass, -- 19.3. ВЗ пассажиры
           (CASE WHEN
                             request_type_oper = '71' AND request_subtype_oper IN ('20', '70', '80', '71', '81')
                         AND oper = 'V'
                     THEN -corr_sum
                 ELSE 0 END)	AS vz_bag, -- 19.4. ВЗ багаж
           (CASE WHEN
                             request_type_oper = '26' AND request_subtype_oper = '25'
                     THEN -corr_sum
                 ELSE 0 END)	AS gsh_pass, -- 19.5. ГШ пассажиры
           (CASE WHEN
                             request_type_oper = '71' AND request_subtype_oper IN ('25', '26', '72', '82')
                     THEN -corr_sum
                 ELSE 0 END)	AS gsh_bag, -- 19.6. ГШ багаж
           (CASE WHEN
                             request_type_oper = '26' AND request_subtype_oper IN ('99')
                     THEN -corr_sum
                 ELSE 0 END)	AS gsh_vz, -- 19.7. ГШ ВЗ
           (CASE WHEN
                             request_type_oper = '3' AND request_subtype_oper = '15'
                     THEN -corr_sum
                 ELSE 0 END)	AS meal_sale, -- 19.8. Продажа питание
           (CASE WHEN
                             request_type_oper = '4' AND request_subtype_oper = '266'
                     THEN -corr_sum
                 ELSE 0 END)	AS cards_sale, -- 19.9. Продажа карты
           (CASE WHEN
                             request_type_oper = '17' AND request_subtype_oper IN ('773', '783')
                         AND docs.pay_code NOT IN ('0194', '0195', '0196', '0197', '0198') -- !!! Добавил признак исключения Внесение наличных
                     THEN -corr_sum
                 ELSE 0 END)	AS krs_sale, -- 19.10. Продажа услуги
           (CASE WHEN
                             request_type_oper = '17' AND request_subtype_oper IN ('783')
                         AND docs.pay_code IN ('0194', '0195', '0196', '0197', '0198')
                     THEN corr_sum
                 ELSE 0 END)	AS krs_spb, -- !!! ВНЕСЕНИЕ НАЛИЧНЫХ
           (CASE WHEN
                             request_type_oper = '17' AND request_subtype_oper = '788'
                     THEN -corr_sum
                 ELSE 0 END)	AS vz_krs, -- 19.11. ВЗ услуги
           (CASE WHEN
                             request_type_oper = '17' AND request_subtype_oper = '795'
                         AND docs.pay_code NOT IN ('0194', '0195', '0196', '0197', '0198')
                         AND docs.oper = 'O' AND docs.oper_g = 'G'
                     THEN -corr_sum
                 ELSE 0 END)	AS gsh_krs, -- 19.12. ГШ услуги
           (CASE WHEN
                             request_type_oper = '03' AND request_subtype_oper = '20'
                     THEN -corr_sum
                 ELSE 0 END)	AS vz_meal, -- 19.13. ВЗ питание
           (CASE WHEN
                             request_type_oper = '03' AND request_subtype_oper = '027'
                     THEN -corr_sum
                 ELSE 0 END)	AS gsh_meal, -- 19.14. ГШ питание
           (CASE WHEN
                             request_type_oper = '04' AND request_subtype_oper = '281'
                     THEN -corr_sum
                 ELSE 0 END)	AS gsh_cards, -- 19.15. ГШ карты
           (CASE WHEN
                             request_type_oper = '17' AND request_subtype_oper = '795'
                         AND docs.pay_code NOT IN ('0194', '0195', '0196', '0197', '0198')
                         AND docs.oper = 'V' AND docs.oper_g = 'G'
                     THEN -corr_sum
                 ELSE 0 END)	AS gsh_vz_krs -- 19.16. ГШ ВЗ услуги
       FROM docs
                JOIN params ON true
       WHERE
               _m_vidr = '9' AND _m_pvid = '98' AND _m_arch = '22'
--	   AND docs.o != 'О' AND NOT docs.annuled
),

-- Раскладываем провозные платежи и услуги по документам
s4_docs AS (
    SELECT
        DISTINCT(t9_id), tc_id, els_code, dor_code,
                ebd, agent_code, subagent_code, sale_agent, pnd, pko, term_pos, term_dor, term_trm, wt, wo, rv, rp, spr, src_tbl,
                CASE WHEN NOT sd THEN COALESCE(pass_sale+bag_sale+vz_pass+vz_bag+gsh_pass+gsh_bag+gsh_vz, 0) ELSE 0 END AS s4_pp,
                CASE WHEN NOT sd THEN COALESCE(meal_sale+cards_sale+krs_sale+vz_krs+gsh_krs+vz_meal+gsh_meal+gsh_cards+gsh_vz_krs,0) ELSE 0 END AS s4_krs,
                CASE WHEN NOT sd THEN COALESCE(pass_sale+bag_sale+vz_pass+vz_bag+gsh_pass+gsh_bag+gsh_vz, 0) +
                                      COALESCE(meal_sale+cards_sale+krs_sale+vz_krs+gsh_krs+vz_meal+gsh_meal+gsh_cards+gsh_vz_krs,0) ELSE 0 END AS s4_all,
                COALESCE(krs_spb) AS s4_spb,
                CASE WHEN sd THEN COALESCE(pass_sale+bag_sale+vz_pass+vz_bag+gsh_pass+gsh_bag+gsh_vz, 0) +
                                  COALESCE(meal_sale+cards_sale+krs_sale+vz_krs+gsh_krs+vz_meal+gsh_meal+gsh_cards+gsh_vz_krs,0) ELSE 0 END AS ssd_all
    FROM s4
--	WHERE NOT sd -- Исключаем старые договора
),

-- Приводим полученные данные по провозным платежам и услугам к единому формату для объединения с другими подзапросами
s4_agg AS (
    SELECT
        els_code, dor_code,
        ebd, agent_code, subagent_code, sale_agent, pnd, pko, term_pos, term_dor, term_trm, null::date AS dof, wt, wo, rv, rp, spr,
        0 AS sop,
        SUM(s4_spb) AS spb_all, -- Добавляем для объединения в единую структуру
        SUM(s4_pp) AS s4_pp,
        SUM(s4_krs) AS s4_krs,
        SUM(s4_all) AS s4_all,
        SUM(ssd_all) AS ssd_all, -- Добавляем для объединения в единую структуру
        array_agg(t9_id) AS t9_arr,
        array_agg(tc_id) AS tc_arr,
        src_tbl
    FROM s4_docs
    GROUP BY els_code, dor_code, ebd, agent_code, subagent_code, sale_agent, pnd, pko, term_pos, term_dor, term_trm, dof,
             wt, wo, rv, rp, spr, src_tbl
),
-- Раскладываем по документам данные провозных платежей и услуг по старым договорам
-- ssd_docs AS (
-- SELECT
-- 	DISTINCT(t9_id), tc_id, els_code, dor_code,
-- 	ebd, agent_code, subagent_code, sale_agent, pnd, pko, term_pos, term_dor, term_trm, null::date AS dof, wt, wo, rv, rp, spr,
-- 	COALESCE(pass_sale+bag_sale+vz_pass+vz_bag+gsh_pass+gsh_bag+gsh_vz, 0) AS ssd_pp,
-- 	COALESCE(meal_sale+cards_sale+krs_sale+vz_krs+gsh_krs+vz_meal+gsh_meal+gsh_cards+gsh_vz_krs,0) AS ssd_krs,
-- 	COALESCE(pass_sale+bag_sale+vz_pass+vz_bag+gsh_pass+gsh_bag+gsh_vz, 0) +
-- 	COALESCE(meal_sale+cards_sale+krs_sale+vz_krs+gsh_krs+vz_meal+gsh_meal+gsh_cards+gsh_vz_krs,0) AS ssd_all
-- 	FROM s4
-- 	WHERE sd -- Включаем только старые договора
-- ),

-- -- Приводим полученные данные по провозным платежам и услугам по старым договорам к единому формату для объединения с другими подзапросами
-- ssd_agg AS (
-- SELECT
-- 	els_code, dor_code,	ebd, agent_code, subagent_code, sale_agent, pnd, pko, term_pos, term_dor, term_trm, dof, wt, wo, rv, rp, spr,
-- --	SUM(ssd_pp) AS ssd_pp,
-- --	SUM(ssd_krs) AS ssd_krs,
-- 	0 AS sop,
-- 	0 AS spb_all, -- Добавляем для объединения в единую структуру
-- 	0 AS s4_pp, 0 AS s4_krs, 0 AS s4_all,
-- 	SUM(COALESCE(ssd_all, 0)) AS ssd_all,
-- 	array_agg(t9_id) AS t9_arr,
-- 	array_agg(tc_id) AS tc_arr,
-- 	'ssd' AS src_tbl
-- 	FROM ssd_docs
-- 	GROUP BY els_code, dor_code, ebd, agent_code, subagent_code, sale_agent, pnd, pko, term_pos, term_dor, term_trm, dof, wt, wo, rv, rp, spr
-- ),

-- Вычисляем сальдо на начало месяца
saldo_m AS (SELECT DISTINCT(t902.els_code) AS els_code, t904.dor_code,
                           CASE WHEN t904.dor_code = '' THEN 1 ELSE 2 END AS ebd,
                           t902.agent_code, t904.subagent_code, t902.agent_code AS sale_agent,
                           CASE WHEN t904._m_date >= t904.begin_contract_date AND t904._m_date <= t904.end_contract_date THEN 1
                                ELSE 0
                               END AS pnd,
                           t904.flags[5] AS pko,
                           t902.saldo AS sal_m -- 25.1. Сумма на начало месяца
            FROM rawd.t1_00902 AS t902
                     JOIN params ON true
--					JOIN els ON els.els_code = t902.els_code
                     JOIN rawd.t1_00904 AS t904 ON t904.id = t902.id

            WHERE
                    t902._m_date = CAST(date_trunc('month', dt1) AS date) -- Определяем сальдо по записи на первое число месяца
              AND CASE WHEN params.ag = -1 THEN true
                       WHEN t902.agent_code = params.ag THEN true
                       ELSE false END
--				AND t902.agent_code = params.ag
              AND CASE WHEN params.vs = -1 THEN true
                       WHEN params.vs = 0 AND t904.subagent_code <= 0 THEN true
                       WHEN params.vs > 0 AND t904.subagent_code > 0 THEN true
                       ELSE false END
              AND t902._m_vidr = '9'
              AND t902._m_pvid = '0'
              AND t902._m_arch IN ('17', '25')
              AND CASE WHEN params.fd = '' THEN true WHEN t904.dor_code = params.fd THEN true ELSE false END
),

-- Вычисляем сумму наличных на начало месяца
cash_m AS (SELECT DISTINCT(t902.els_code) AS els_code, t904.dor_code, t902.agent_code, t904.subagent_code,
                          t904.remains_asufr AS snm -- 32. Сумма наличных на начало месяца
           FROM rawd.t1_00902 AS t902
                    JOIN params ON true
                    JOIN rawd.t1_00904 AS t904 ON t904.id = t902.id
           WHERE
               CASE WHEN params.ag = -1 THEN true
                    WHEN t902.agent_code = params.ag THEN true
                    ELSE false END
--				t902.agent_code = params.ag
             AND CASE WHEN params.vs = -1 THEN true
                      WHEN params.vs = 0 AND t904.subagent_code <= 0 THEN true
                      WHEN params.vs > 0 AND t904.subagent_code > 0 THEN true
                      ELSE false END
             AND t902._m_vidr = '9'
             AND t902._m_pvid = '0'
             AND t902._m_arch IN ('17', '25')
             AND CASE WHEN params.fd = '' THEN true WHEN t904.dor_code = params.fd THEN true ELSE false END
),

-- Вычисляем сумму обеспечительного платежа на начало месяца
sop_m AS (SELECT DISTINCT(t902.els_code) AS els_code, t904.dor_code,
                         CASE WHEN t904.dor_code = '' THEN 1 ELSE 2 END AS ebd,
                         t902.agent_code, t904.subagent_code, t902.agent_code AS sale_agent,
                         CASE WHEN t904._m_date >= t904.begin_contract_date AND t904._m_date <= t904.end_contract_date THEN 1
                              ELSE 0
                             END AS pnd,
                         t904.flags[5] AS pko,
                         -t904.min_saldo AS som -- 25.1. Сумма на начало месяца
          FROM rawd.t1_00904 AS t904
                   JOIN params ON true
                   JOIN rawd.t1_00902 AS t902 ON t902.id = t904.id

          WHERE
              CASE WHEN params.ag = -1 THEN true
                   WHEN t902.agent_code = params.ag THEN true
                   ELSE false END
--				t902.agent_code = params.ag
--				AND t902._m_date >= params.dt1 AND t902._m_date <= params.dt1
            AND t904._m_date = CAST(date_trunc('month', dt1) AS date)
            AND CASE WHEN params.vs = -1 THEN true
                     WHEN params.vs = 0 AND t904.subagent_code <= 0 THEN true
                     WHEN params.vs > 0 AND t904.subagent_code > 0 THEN true
                     ELSE false END
            AND t902._m_vidr = '9'
            AND t902._m_pvid = '0'
            AND t902._m_arch IN ('17', '25')
            AND CASE WHEN params.fd = '' THEN true WHEN t904.dor_code = params.fd THEN true ELSE false END
),

-- Определяем из каких таблиц в итоге сформируют перечень ЕЛС и объединяем записи
els AS (
    SELECT DISTINCT(els_code), dor_code, ebd, agent_code, subagent_code, sale_agent FROM s4_agg
    UNION SELECT DISTINCT(els_code), dor_code, ebd, agent_code, subagent_code, sale_agent FROM spb_agg
--	UNION SELECT DISTINCT(els_code), dor_code, ebd, agent_code, subagent_code, sale_agent FROM saldo_m
--	UNION SELECT DISTINCT(els_code), dor_code, ebd, agent_code, subagent_code, sale_agent, pnd, pko FROM sop_m
    UNION SELECT DISTINCT(els_code), dor_code, ebd, agent_code, subagent_code, sale_agent FROM sop_agg
),

-- Объединяем все агрегированные по признакам ЕЛС записи из всех подзапросов
all_union AS (
    SELECT * FROM s4_agg
--	UNION SELECT * FROM ssd_agg
    UNION SELECT *  FROM spb_agg
    UNION SELECT *  FROM sop_agg
),

-- Сборка количественного отчёта по ЕЛС
sum_rep AS (
    SELECT
        params.dt1 AS _m_date,
--	CASE WHEN els.ebd = 1 OR els.pnd = 2 THEN 1 ELSE 2 END AS ebd,
        els.agent_code, -- Код агента-держателя договора ФПК/ ДОСС
        els.subagent_code, -- Код субагента (0 – юридическое лицо)
        els.dor_code, -- Код филиала-держателя договора
        els.sale_agent,
        els.els_code, -- 1. Код плательщика

        CAST(COALESCE(sal_m, 0)/10 AS NUMERIC(15,2)) AS m_saldo, -- Сумма на начало месяца
        CAST(SUM(COALESCE(spb_all, 0))/10 AS NUMERIC(15,2)) AS sum_in, -- Сумма поступлений
        CAST(SUM(COALESCE(s4_pp, 0))/10 AS NUMERIC(15,2)) AS sum_pp, -- Сумма провозных платежей
        CAST(SUM(COALESCE(s4_krs, 0))/10 AS NUMERIC(15,2)) AS sum_service, -- Сумма за услуги
        CASE WHEN els.subagent_code = 0 THEN CAST(SUM(COALESCE(ssd_all, 0))/10 AS NUMERIC(15,2)) ELSE 0 END AS sum_sd, -- Сумма по старым договорам
        CAST(COALESCE(som, 0)/10 AS NUMERIC(15,2)) AS sum_op_m, -- Сумма обеспечительного платежа на начало месяца
        CAST(SUM(COALESCE(sop, 0))/10 AS NUMERIC(15,2)) AS sum_op_in, -- Сумма поступлений обеспечительного платежа
        CASE WHEN els.subagent_code > 0 THEN CAST(SUM(COALESCE(ssd_all, 0))/10 AS NUMERIC(15,2)) ELSE 0 END AS sum_wo_els, -- Сумма без учета на ЕЛС

        all_union.pnd AS contract_pr, -- Наличие договора
        all_union.pko AS balance_control, -- Включен контроль остатк
        all_union.term_dor AS sale_dor, -- Дорога (филиал) продажи
        all_union.term_pos AS sale_pos, -- Номер пункта продажи
        all_union.term_trm AS sale_trm, -- Номер терминала продажи - !!! Множит строки по сравнению со старым приложением
        dof, -- Дата оформления
        wt AS doc_type, -- Вид документа
        CASE WHEN wo = '1' THEN true ELSE false END AS form_type, -- Вид оформления (Э/Р)
        rv AS request_type, -- Вид работ
        rp AS request_subtype, -- Подвид работ
        CASE WHEN spr IN ('б/д', 'IE') THEN '*'
             ELSE '' END AS spec_pr, -- Специальный признак
        t9_arr, -- Массив ID записей таблиц нижнего уровня, участвовавших в расчёте
        tc_arr, --  Массив ID записей таблиц верхнего уровня, участвовавших в расчёте
--	src_tbl -- Таблица - источник данных
        CASE WHEN src_tbl= 'pass' THEN tc_arr ELSE null END AS id_pass,
        CASE WHEN src_tbl= 'bag' THEN tc_arr ELSE null END AS id_bag,
        CASE WHEN src_tbl= 'krs' THEN tc_arr ELSE null END AS id_krs,
        CASE WHEN src_tbl= 'meal' THEN tc_arr ELSE null END AS id_meal,
        CASE WHEN src_tbl= 'cards' THEN tc_arr ELSE null END AS id_cards

    FROM els
             JOIN params ON true
        -- Подключаем сальдо на начало месяца
             LEFT JOIN saldo_m ON saldo_m.els_code = els.els_code AND saldo_m.dor_code = els.dor_code
        AND saldo_m.agent_code = els.agent_code AND saldo_m.subagent_code = els.subagent_code
        -- Подключаем сумму обеспечительного платежа на начало месяца
             LEFT JOIN sop_m ON sop_m.els_code = els.els_code AND sop_m.dor_code = els.dor_code
        AND sop_m.agent_code = els.agent_code AND sop_m.subagent_code = els.subagent_code
        -- Подключаем все остальные записи
             LEFT JOIN all_union ON all_union.els_code = els.els_code AND all_union.dor_code = els.dor_code
        AND all_union.agent_code = els.agent_code AND all_union.subagent_code = els.subagent_code

    GROUP BY params.dt1, els.agent_code, els.subagent_code,	els.dor_code, els.sale_agent, els.els_code,
             all_union.pnd, all_union.pko, all_union.term_dor, all_union.term_pos, all_union.term_trm, dof,
             wt, wo, rv, rp, spr, sal_m, som, t9_arr, tc_arr, src_tbl

    ORDER BY all_union.pnd DESC, agent_code, subagent_code, els.dor_code, els.els_code, rv, rp
)


        SELECT ROW_NUMBER() OVER(ORDER BY contract_pr, agent_code, subagent_code, dor_code, els_code, request_type, request_subtype) AS row_num, sum_rep.* FROM sum_rep

    );
GET DIAGNOSTICS kolz = ROW_COUNT;
RAISE INFO 'Всего к обработке % записей .', kolz;

	--------------------
INSERT INTO rawdl2_day.els_day(_m_date, agent_code, subagent_code, dor_code, sale_agent, els_code, m_saldo, sum_in, sum_pp, sum_service, sum_sd, sum_op_m, sum_op_in, sum_wo_els, contract_pr, balance_control, sale_dor, sale_pos, sale_trm, doc_type, form_type, request_type, request_subtype, spec_pr)
SELECT _m_date, agent_code, subagent_code, dor_code, sale_agent, els_code, m_saldo, sum_in, sum_pp, sum_service, sum_sd, sum_op_m, sum_op_in, sum_wo_els, contract_pr, balance_control, sale_dor, sale_pos, sale_trm, doc_type, form_type, request_type, request_subtype, spec_pr
FROM els_tt0;
GET DIAGNOSTICS kolz = ROW_COUNT;
RAISE INFO 'Вставлено в els_day % записей .', kolz;
SELECT MIN(id_els) FROM els_day WHERE _m_date=req_dt INTO seq1;
INSERT INTO link_els_day(id_els, id_rawd, id_l2_pass_main, id_l2_bag_main, id_l2_krs, id_l2_meal, id_l2_cards)
SELECT seq1+row_num-1, t9_arr, id_pass, id_bag, id_krs, id_meal, id_cards
FROM els_tt0;
GET DIAGNOSTICS kolz = ROW_COUNT;
RAISE INFO 'Вставлено в link_els_day % записей .', kolz;

--    perform setval('rawdl2_day.bag_cost_day_id_bag_cost_day_seq', seq3+kolz);

	-------------------------------------------------------------------------------
RAISE INFO 'Окончание работы в %.', to_char(clock_timestamp()::time, 'HH24:MI:SS');
DROP TABLE IF EXISTS els_tt0;
-- возвращаем сообщение
RETURN kolz;
END;

