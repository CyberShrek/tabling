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