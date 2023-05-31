

@CASE 1 {schema_a.table_a<-schema_b.table_a}
BEGIN
	INSERT INTO schema_a.table_a
	SELECT * FROM schema_b.table_a;
END;
		
@CASE 2 {schema_a.table_a<-schema_a.table_b}
BEGIN
	UPDATE schema_a.table_a SET sales_count = sales_count + 1
	FROM schema_a.table_b
	WHERE accounts.name = 'Acme Corporation'
	  AND schema_a.table_a.id = schema_a.table_a.sales_person;
END;

@CASE 3 {schema_.table_a<-schema_.table_b; 
		 schema_.table_a<-schema_.table_c}
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

@CASE 4 {schema_.table_a<-schema_.table_c}
BEGIN
	WITH upd AS (
		SELECT sales_person, ddd FROM schema_.table_c WHERE name = 'Acme Corporation'
	)
	INSERT INTO schema_.table_a SELECT * FROM upd;
END;


@CASE 5 {schema_.table_<-schema_a.table_a;
		 schema_.table_<-schema_b.table_b;
		 schema_.table_<-schema_c.table_c}
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

@CASE 6 {car.rmestday<-ng.rmest}

DECLARE				
		rr RECORD;		
		godipr	Integer;
		mesipr	Integer;
		dayipr  Integer;
		idnit 	Integer;
		datazp Date;
		tdat Date;
		ndat Date;
		sxem Character(18);
		minrate_m Numeric(14,0);
BEGIN
       datazp = $1;
	   
	   if datazp is NULL then
	   SELECT now()::date - interval '1 day' INTO datazp;
	   end if;
	   --наростающим итогом каждый день суммируются показатели после продажи
		--DELETE FROM car.rmestdays where datapzd = datazp;

FOR rr IN 
      select datapzd,pzd,nit,katvag,gosv,skp,kat,(sum(rasst::integer*(kolpm::integer-kolvozm::integer))/(case when sum(kolpm::integer-kolvozm::integer)<>0 then sum(kolpm::integer-kolvozm::integer) else 1 end)) as rasst,sum(kolpm-kolvozm) as kol,
sum((kolpm::integer-kolvozm::integer)*rasst::integer) as passkm,
sum(sumbil*car.get_kurs(gosp,dataop)) as sumbil,
sum(sumpl*car.get_kurs(gosp,dataop)) as sumpl,
sum(sumserv*car.get_kurs(gosp,dataop)) as sumserv,
sum(sumkomsb*car.get_kurs(gosp,dataop)) as sumkomsb
from ng.rmest
where dataop=datazp and rasst<>0
group by datapzd,pzd,nit,katvag,gosv,skp,kat
order by pzd,nit,kat

	  
    LOOP 
		-- минимальная сумма
		select car.get_minrate(rr.datapzd,rr.pzd,rr.nit,rr.gosv,rr.skp,rr.katvag) into minrate_m;				  
		IF	not exists (select * from car.rmestday where pzd = rr.pzd and nit = rr.nit and datapzd = rr.datapzd and gosskp = rr.gosv and skp = rr.skp and tipv = rr.katvag) THEN							
		
		INSERT INTO car.rmestday (datapzd,pzd, nit, gosskp, skp, tipv,incbil,incpl,incserv,inckomsb,katpzd,passkm,avdistance,minrate,trpass)	
			VALUES(rr.datapzd,rr.pzd,rr.nit,rr.gosv,rr.skp,rr.katvag,rr.sumbil,rr.sumpl,rr.sumserv,rr.sumkomsb,rr.kat,rr.passkm,rr.rasst,minrate_m,rr.kol);	
		ELSE 
						  -- 2020-04-09 изменила расчет среднего расстояния
		 UPDATE car.rmestday set incbil=incbil+rr.sumbil,incpl=incpl+rr.sumpl,incserv=incserv+rr.sumserv,inckomsb=inckomsb+rr.sumkomsb,katpzd = rr.kat,passkm=passkm+rr.passkm,avdistance=(case when (trpass+rr.kol) <>0 then (passkm+rr.passkm)/(trpass+rr.kol) else 0 end),trpass=trpass+rr.kol,minrate=minrate_m
			where pzd = rr.pzd and nit = rr.nit and datapzd = rr.datapzd and gosskp = rr.gosv and skp = rr.skp and tipv = rr.katvag;
		
		END	IF; 
						  
		
    END LOOP;
	
	
END; 

@CASE 7 {car.rmestday<-ng.rmest}


DECLARE				
		rr RECORD;		
		godipr	Integer;
		mesipr	Integer;
		dayipr  Integer;
		idnit 	Integer;
		datazp Date;
		tdat Date;
		ndat Date;
		sxem Character(18);
		minrate_m Numeric(14,0);
BEGIN
       datazp = $1;
	   
	   if datazp is NULL then
	   SELECT now()::date - interval '1 day' INTO datazp;
	   end if;
	   
		DELETE FROM car.rmestday where datapzd = datazp;

FOR rr IN 
      select datapzd,pzd,nit,katvag,gosv,skp,kat,(sum(rasst::integer*(kolpm::integer-kolvozm::integer))/(case when sum(kolpm::integer-kolvozm::integer)<>0 then sum(kolpm::integer-kolvozm::integer) else 1 end)) as rasst,sum(kolpm-kolvozm) as kol,
sum((kolpm::integer-kolvozm::integer)*rasst::integer) as passkm,
sum(sumbil*car.get_kurs(gosp,dataop)) as sumbil,
sum(sumpl*car.get_kurs(gosp,dataop)) as sumpl,
sum(sumserv*car.get_kurs(gosp,dataop)) as sumserv,
sum(sumkomsb*car.get_kurs(gosp,dataop)) as sumkomsb
from ng.rmest
where datapzd=datazp and rasst<>0
group by datapzd,pzd,nit,katvag,gosv,skp,kat
order by pzd,nit,kat

	  
    LOOP 
		select car.get_minrate(rr.datapzd,rr.pzd,rr.nit,rr.gosv,rr.skp,rr.katvag) into minrate_m;
		INSERT INTO car.rmestday (datapzd,pzd, nit, gosskp, skp, tipv,incbil,incpl,incserv,inckomsb,katpzd,passkm,avdistance,minrate,trpass)	
			VALUES(rr.datapzd,rr.pzd,rr.nit,rr.gosv,rr.skp,rr.katvag,rr.sumbil,rr.sumpl,rr.sumserv,rr.sumkomsb,rr.kat,rr.passkm,rr.rasst,0,rr.kol);				
		
    END LOOP;
	
	
END; 


@CASE 8 {svod.co32<-car.pzdmes; svod.co32<-nsi.osk; svod.co32<-nsi.gosk; svod.co32<-nsi.dork}


DECLARE
datecalc DATE;-- Дата расчета (этот месяц)
datecalc1 DATE;-- Дата расчета (прошлый месяц)


--tms TIMESTAMP;-- для логирования


tmpdatef DATE; -- 1 число месяца 
tmpdatet DATE; --  Последнее число месяца
datapzd1 DATE;-- Дата отправления поезда


mes1 INTEGER;god1 INTEGER; -- Месяц и год расчета


rec RECORD;  -- HASH для поездониток
TMPrec RECORD; -- временный HASH

-- Данные по нитке поезда
STO_DOR TEXT;STO_GOS TEXT;STO_SNG TEXT;
STN_DOR TEXT;STN_GOS TEXT;STN_SNG TEXT;

DCS INTEGER;

-- Данные по поезду (Основная нитка)
PZD_DOR TEXT;
PZD_SKP INTEGER;
PZD_GOSSKP TEXT;
PZD_DCS INTEGER;

-- Текущие данные
bufPZD TEXT; -- Текущий поезд, для повторов.

CalcOS TEXT;-- Основная нитка
bufSTO TEXT; -- ст. отправление нитки
bufSTN TEXT; -- ст. назначения нитки

tmpNIT TEXT;


-- Станция отправлнния осн. нитки поезд и след. станция
 STO_LINE TEXT; findST TEXT;
 STNEXT_CODE TEXT; STNEXT_LINE TEXt;

VIDS1 INTEGER;KATPZD1 INTEGER;




BEGIN
	
-- Определяем дату расчета
	datecalc =$1;
	IF datecalc is null THEN
		SELECT now()::date  INTO datecalc; -- этот месяц и год
		END IF;

-- Прошлый месяц
SELECT  datecalc - interval '1 month' INTO datecalc1;
-- Разбиваем прошлый месяц на год и месяц
SELECT CAST(date_part('MONTH',datecalc1::date ) as Integer) INTO mes1;
SELECT CAST(date_part('YEAR',datecalc1::date ) as Integer) INTO god1;



SELECT (date_trunc('MONTH', datecalc1) + INTERVAL '1 MONTH - 1 day')::DATE INTO tmpdatet;

SELECT date_trunc('MONTH',datecalc1)::DATE  INTO tmpdatef;


-- Очищаем svod.co32 на год и месяц расчета
DELETE FROM svod.co32 c where c.mes = mes1 and c.god=god1;


-- Очистка лога
--DELETE FROM svod.test;

--SELECT clock_timestamp()::timestamp  INTO tms;
--insert into svod.test (message) values(tms:: TEXT ||' -- Start work --');
--insert into svod.test (message) values('Param =' || datecalc );
--insert into svod.test (message) values('Date prev mes =' || datecalc1 );
--insert into svod.test (message) values('GOD=' || god1 );
--insert into svod.test (message) values('MES=' || mes1 );
--insert into svod.test (message) values('Last DATA=' || tmpdateT );
--insert into svod.test (message) values('First DATA =' || tmpdateF );

bufPZD='-';

FOR rec IN -- 1.XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

select p.pzd,p.pr_os,p.nit,p.gosskp,p.skp,
p.stanot_n  as sto, d1.d_nom3 as stodor,g1.g_prsng as stosng,g1.g_kod as stogos, --STO
p.stannaz_n as stn, d2.d_nom3 as stndor,g2.g_prsng as stnsng,g2.g_kod as stngos, --STN
p.tipv,p.trpass,p.passkm,p.incbil,p.incpl,p.incserv,p.inckomsb

from car.pzdmes p

inner JOIN nsi.osk o1 ON substring(o1.os_kod, 3)=p.stanot_n
INNER JOIN nsi.dork d1 on o1.os_vidd = d1.d_vid
INNER JOIN nsi.gosk g1 on o1.os_vidgm = g1.g_vid

inner JOIN nsi.osk o2 ON substring(o2.os_kod, 3)=p.stannaz_n
INNER JOIN nsi.dork d2 on o2.os_vidd = d2.d_vid
INNER JOIN nsi.gosk g2 on o2.os_vidgm = g2.g_vid

where p.godpzd= god1 and p.mespzd = mes1
and (((p.godpzd - 2000)*12 + p.mespzd ) < ((p.godcalcul - 2000)*12 + p.mescalcul))
 
and o1.os_datan  <= datecalc1 and o1.os_datak  >= datecalc1
and o1.os_datani <= datecalc1 and o1.os_dataki >= datecalc1
and d1.d_datan  <= datecalc1 and d1.d_datak  >= datecalc1
and d1.d_datani <= datecalc1 and d1.d_dataki >= datecalc1
and g1.g_datan  <= datecalc1 and g1.g_datak  >= datecalc1
and g1.g_datani <= datecalc1 and g1.g_dataki >= datecalc1


and o2.os_datan  <= datecalc1 and o2.os_datak  >= datecalc1
and o2.os_datani <= datecalc1 and o2.os_dataki >= datecalc1
and d2.d_datan  <= datecalc1 and d2.d_datak  >= datecalc1
and d2.d_datani <= datecalc1 and d2.d_dataki >= datecalc1
and g2.g_datan  <= datecalc1 and g2.g_datak  >= datecalc1
and g2.g_datani <= datecalc1 and g2.g_dataki >= datecalc1

order by p.pzd, p.pr_os desc,p.gosskp,p.skp,p.stanot_n,p.stannaz_n



LOOP

--SELECT clock_timestamp()::timestamp  INTO tms;
--insert into svod.test (message) values(tms:: TEXT||'--Read PZD='||rec.pzd||' '||rec.nit|| ' ' || rec.sto ||' '|| rec.stn );



if bufPZD != rec.pzd::text then -- Изменился поезд, начинаем расчет всего

--insert into svod.test (message) values('New PZD');
	-- Запоминаем поезд
	bufPZD = rec.pzd;
	CalcOS ='0';-- Оснавная нитка
	bufSTO ='-'; -- ст. отправление нитки
	bufSTN ='-'; -- ст. назначения нитки
	
ELSE

--insert into svod.test (message) values('Povtor PZD'   );


end if; --if bufPZD != rec.pzd



if(bufSTO != rec.sto or bufSTN != rec.STN) THEN
-- Поменялся маршрут нитки 
	bufSTO = rec.sto;
	bufSTN = rec.STN;



	STO_DOR = rec.stodor;STO_GOS = rec.stogos;STO_SNG = rec.stosng;
	STN_DOR = rec.stndor;STN_GOS = rec.stngos;STN_SNG = rec.stnsng;


	--insert into svod.test (message) values('STO NIT dor gos sng ' || STO_DOR ||' '||STO_GOS||' '||STO_SNG);

	--insert into svod.test (message) values('STN NIT dor gos sng ' || STN_DOR ||' '||STN_GOS||' '||STN_SNG);




	if (rec.pr_os='1' and CalcOS='0' ) THEN -- Основную еще не считали!
		-- Определяем ВИД СООБЩЕНИЯ, по основной нитке.

		PZD_DOR = STO_DOR;
		PZD_SKP = rec.skp;
		PZD_GOSSKP = rec.gosskp;
--insert into svod.test (message) values('PZD DOR=' || PZD_DOR ||' SKP='||PZD_SKP||' GOSSKP='||PZD_GOSSKP);


		IF    (STO_GOS= '20' AND STN_GOS = '20' AND STO_DOR= STN_DOR  ) THEN 
				VIDS1=3;
		ELSIF (STO_GOS= '20' AND STN_GOS = '20' AND STO_DOR != STN_DOR ) THEN 
				VIDS1=1;
		ELSIF (STO_GOS!='20' AND STN_GOS !='20' AND STO_SNG !='1' AND STN_SNG !='1' ) THEN 
				VIDS1=4;
		ELSE VIDS1 =2;

		END IF;



-- КАТЕГОРИЯ ПОЕЗДА 
		KatPZD1=-1;

		FOR TMPrec IN -- VID
		select k.vid
		from nsi.katpzdco32 k
		where k.datan <= datecalc1 and k.datak >= datecalc1
		and k.pzdosnn <= substring(rec.pzd from 1 for 4):: INTEGER
		and k.pzdosnk >= substring(rec.pzd from 1 for 4):: INTEGER

		LOOP
		KatPZD1 = TMPrec.vid;


		END LOOP; -- VID

		--insert into svod.test (message) values('KATPZD=' || KatPZD1::text);





		-- Определяем 1 дату отправления поезда с таким маршрутом основной нитки
		datapzd1= '1990-01-01':: DATE;

		FOR TMPrec IN -- DATAOPZD
		select r.data as dataopzd,r.nitm
		from train.rnit r
		where r.pzd= rec.pzd  --and r.nitm= rec.nit
		and r.data >= tmpdatef and r.data <=tmpdatet
		and r.stfnit = rec.sto and r.stnnit = rec.stn
		limit 1
		LOOP
		datapzd1 =TMPrec.dataopzd;
		tmpNIT=TMPrec.nitm;
		--insert into svod.test (message) values('Data otpr PZD=' || datapzd1::text   );
		END LOOP; -- DATAOPZD
		--insert into svod.test (message) values('Data otpr PZD=' || datapzd1::text   );




		-- Находим станцию отправления и след нитки поезда по rmrt (линия)
		STO_LINE='---';
		STNEXT_CODE='---';
		STNEXT_LINE='---';
		findST='-';


		FOR TMPrec IN -- Станция отправления нитки
		select *
		from train.rmrt r
		where r.rmt_pzd = rec.pzd  and r.rmt_nit = tmpNIT
		and r.rmt_data =datapzd1  
		order by r.rmt_rst
		LOOP


		if (TMPrec.rmt_stan = rec.sto) then -- нашли станцию отправления

		findST='1';-- Нашли STO
		STO_LINE=TMPrec.rmt_lin;
		--insert into svod.test (message) values('FIND STO_LINE='|| STO_LINE   );

		elseif  (findST='1') then -- Следующая станция
		findST='0';
		STNEXT_CODE=TMPrec.rmt_stan;
		STNEXT_LINE = TMPrec.rmt_lin;
		--insert into svod.test (message) values('FIND NextCode='|| STNEXT_CODE||' LINE =' || STNEXT_LINE   );
		EXIT;
		end if;

		END LOOP; -- Станция отправления нитки

		--insert into svod.test (message) values( 'NEXT ST='||STNEXT_CODE|| ',STO=' || rec.sto   );
		--insert into svod.test (message) values('NextLine='|| STNEXT_LINE||' LINE =' || STO_LINE   );


		-- Находим ДЦС для станции и линии
		DCS= -1;
		FOR TMPrec IN -- Lines DCS
		select l.stan,l.noml,l.koddcs
		from nsi.lines l
		where l.datand <= datecalc1 and l.datakd >= datecalc1
		and   l.datani <= datecalc1 and l.dataki >= datecalc1
		and l.stan in( STNEXT_CODE,rec.sto  )
		order by l.noml
		LOOP
		-- Пока упрощаем, берем что есть!!
		--insert into svod.test (message) values('ST='|| TMPrec.stan ||' DCS='||TMPrec.koddcs::TEXT   );
	
		
		if (TMPrec.stan= rec.sto) THEN
		
		DCS =TMPrec.koddcs;
		--EXIT;

		end if;

		END LOOP; -- Lines DCS

		--insert into svod.test (message) values('Find DCS='|| DCS::TEXT   );
	
		PZD_DCS= DCS;

		CalcOS='0';

		END IF;--if (rec.pr_os='1' and CalcOS='0' )



END IF;-- if(bufSTO != rec.sto or bufSTN != rec.STN)




--PZD_DOR TEXT;
--PZD_SKP INTEGER;
--PZD_GOSSKP TEXT;
--PZD_DCS INTEGER;




-- INSERT or UPDATE? Check KEY
IF NOT EXISTS ( select * from svod.co32
 	where mes = mes1 and god=god1 
	and gosskp = PZD_GOSSKP and skppzd =PZD_SKP -- перевозчик осн. нитки
	and dorpzd = PZD_DOR -- Дорога поезда
	and dorvag = STO_DOR -- Дорога отпр. нитки вагонов
	
 	and gosvag = rec.gosskp and skpvag =rec.skp -- перевозчик группы вагонов
	and dcspzd=PZD_DCS and dcsvag = DCS -- ДЦС группы вагонов и поезда
	and katpzd= katpzd1 -- Категория поезда
	and vids = vids1 --вид сообщения
	and tipv = rec.tipv
)
	THEN --------- INSERT
--insert into svod.test (message) values('insert' );


	INSERT INTO svod.co32 (mes,god,gosskp,skppzd,dorpzd,dorvag,
		gosvag, skpvag,dcspzd,dcsvag, 
		katpzd,vids,tipv,
		otpass_p,passkm_p,
		sumbil,sumpl,sumserv,sumkomsb)

	VALUES (mes1,god1, PZD_GOSSKP,PZD_SKP,PZD_DOR,STO_DOR,
		rec.gosskp,rec.skp,PZD_DCS,DCS,
		katpzd1,vids1,rec.tipv,
		rec.trpass,rec.passkm,
		rec.incbil*10,rec.incpl*10,rec.incserv*10,rec.inckomsb*10);

------------- INSERT
ELSE --------- UPDATE
--insert into svod.test (message) values('UPDATE' );

UPDATE svod.co32 SET  
		otpass_p =otpass_p +rec.trpass,
		passkm_p =passkm_p +rec.passkm,
		sumbil   =sumbil   +rec.incbil*10,
		sumpl    =sumpl    +rec.incpl*10,
		sumserv  =sumserv  +rec.incserv*10,
		sumkomsb =sumkomsb +rec.inckomsb*10
where mes = mes1 and god=god1 
	and gosskp = PZD_GOSSKP and skppzd =PZD_SKP -- перевозчик осн. нитки
	and dorpzd = PZD_DOR -- Дорога поезда
	and dorvag = STO_DOR -- Дорога отпр. нитки вагонов
	
 	and gosvag = rec.gosskp and skpvag =rec.skp -- перевозчик группы вагонов
	and dcspzd=PZD_DCS and dcsvag = DCS -- ДЦС группы вагонов и поезда
	and katpzd= katpzd1 -- Категория поезда
	and vids = vids1 --вид сообщения
	and tipv = rec.tipv;

------------------- UPDATE

END IF;




END LOOP; -- 1.XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX


--SELECT clock_timestamp()::timestamp  INTO tms;
--insert into svod.test (message) values(tms::text||' STOP' );

	RETURN;
END

@CASE 8{car.expind<-car.expindplan}


DECLARE				
		rr RECORD;		
		godipr	Integer;
		mesipr	Integer;
		dayipr  Integer;
		oldvagkm 	Numeric;
		oldmestokm	Numeric;
		svagkmpl 	Numeric;
		smestokmpl	Numeric;
		datazp  Date; 
		
BEGIN
       datazp = $1;
   	   if datazp is NULL then
	   SELECT now()::date INTO datazp;
	   end if;
		-- 28.10.2019 Ширман, добавлено удаление строк с 'V', чтобы избежать ошибки повтороного ключа
		DELETE FROM car.expind where datazap = datazp and tarifseg='V';
FOR rr IN 
      select god,mesprib,skp,dorf,sum(vagkm) as svagkm,sum(mestokm) as smestokm
		from car.expind 
		 WHERE datazap=datazp and tarifseg in ('R','D')
		 group by god,mesprib,skp,dorf
		 order by god,mesprib,skp,dorf	 
    LOOP 
		INSERT	INTO	car.expind (god,mesprib,gos,skp,dorf,tarifseg,datazap,vagkm,mestokm)	
			VALUES(rr.god,rr.mesprib,'20',rr.skp,rr.dorf,'V',datazp,rr.svagkm,rr.smestokm);
		
    END LOOP;
	--RETURN '1';
FOR rr IN 
        	SELECT god,mesprib,skp,dorf,vagkm,mestokm,tarifseg
			FROM car.expind 
        	WHERE datazap=datazp
			order by  god,mesprib,skp,dorf,tarifseg
    	LOOP
		
				select e1.vagkm,e1.mestokm into oldvagkm,oldmestokm from car.expind e1
				where e1.god = rr.god-1  and e1.mesprib=rr.mesprib
				and e1.skp = rr.skp and e1.dorf = rr.dorf and e1.tarifseg = rr.tarifseg
				and e1.datazap = (select max(e2.datazap) from car.expind e2 where e2.god = e1.god  and -- Ширман 28.10.2019 заменили e1.god-1 на e1.god
				 e2.mesprib=e1.mesprib and e2.skp = e1.skp and e2.dorf = e1.dorf and e2.tarifseg = e1.tarifseg);

			IF	NOT	FOUND	THEN								
				UPDATE car.expind set pvagkmly = 0,pmestokmly = 0
				where god = rr.god and mesprib = rr.mesprib and skp = rr.skp and dorf = rr.dorf and tarifseg = rr.tarifseg and datazap=datazp;
			ELSE 
				UPDATE car.expind set pvagkmly = case when oldvagkm = 0 then 0 else (vagkm*100)/oldvagkm end, pmestokmly = case when oldmestokm = 0 then 0 else (mestokm*100)/oldmestokm end
				where god = rr.god and mesprib = rr.mesprib and skp = rr.skp and dorf = rr.dorf and tarifseg = rr.tarifseg and datazap=datazp;

			END	IF;
			
		 	IF (rr.tarifseg = 'V') THEN
				select sum(vagkmpl),sum(mestokmpl) into svagkmpl,smestokmpl
				from car.expindplan 
				 WHERE god = rr.god and mesprib = rr.mesprib and skp = rr.skp and dorf = rr.dorf 
				 group by god,mesprib,skp,dorf;				 
			ELSE 
				select vagkmpl,mestokmpl  into svagkmpl,smestokmpl from car.expindplan
					WHERE god = rr.god and mesprib = rr.mesprib and skp = rr.skp and dorf = rr.dorf and tarifseg=rr.tarifseg;
				 		
			END	IF;
			
			IF	NOT	FOUND	THEN								
				UPDATE car.expind set pvagkmpl = 0,pmestokmpl = 0
				where datazap = datazp and god = rr.god and mesprib = rr.mesprib and skp = rr.skp and dorf = rr.dorf and tarifseg = rr.tarifseg;
			ELSE 
				UPDATE car.expind set pvagkmpl = case when svagkmpl = 0 then 0 else (vagkm*100)/svagkmpl end, pmestokmpl = case when smestokmpl = 0 then 0 else (mestokm*100)/smestokmpl end
				where datazap = datazp and god = rr.god and mesprib = rr.mesprib and skp = rr.skp and dorf = rr.dorf and tarifseg = rr.tarifseg;

			END	IF;
			
			
		END LOOP;
	
END; 