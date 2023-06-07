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