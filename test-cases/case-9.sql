
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