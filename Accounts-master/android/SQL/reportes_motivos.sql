//Nuevos 
SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, table1.IdViaje, trip.Nombre as Viaje, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1 
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo, IdViaje
            FROM Movimiento 
            WHERE  IdMoneda == 1 and strftime('%Y',Fecha) == '2019' and Cantidad < 0 GROUP BY IdMotivo, IdViaje
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda 
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == 1 
            then Cantidad * Cambio end) as Gasto, IdMotivo, IdViaje
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) ==  '2019' GROUP BY IdMotivo, IdViaje
        ) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2, IdViaje
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, IdViaje
            FROM Movimiento 
            WHERE Cantidad > 0 and IdMoneda == 1 and strftime('%Y',Fecha) == '2019' 
            Group BY IdMotivo2, IdViaje
        union
        SELECT SUM( CASE WHEN (
            SELECT Totales.idMoneda 
            FROM Totales, Movimiento 
            WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
        then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, IdViaje
        FROM Movimiento 
        WHERE Cantidad < 0 and strftime('%Y',Fecha) == '2019' and IdMoneda == 1 and Cambio IS NOT NULL Group BY IdMotivo2, IdViaje
        ) as table3, Motivo
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2, IdViaje
) as table2 on table1.IdMotivo = table2.IdMotivo2
left outer join (
	SELECT _id, Nombre
	FROM Trips
) as trip on table1.IdViaje == Trip._id ,  Motivo
WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo, Viaje ORDER BY count1 DESC


// mes y año
SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, table1.IdViaje, trip.Nombre as Viaje, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1 
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo, IdViaje
            FROM Movimiento 
            WHERE  IdMoneda == 9 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' and Cantidad < 0 GROUP BY IdMotivo, IdViaje
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == 9 
                then Cantidad * Cambio end) as Gasto,
                IdMotivo, IdViaje
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) ==  '05'  GROUP BY IdMotivo, IdViaje
        ) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2, IdViaje
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, IdViaje
            FROM Movimiento WHERE Cantidad > 0 and IdMoneda == 9 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' Group BY IdMotivo2, IdViaje
        union
        SELECT 
        SUM( CASE WHEN (
            SELECT 
                Totales.idMoneda 
                FROM Totales, Movimiento 
                WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, IdViaje
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' and IdMoneda == 9 and Cambio IS NOT NULL Group BY IdMotivo2, IdViaje
        ) as table3, Motivo 
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2, IdViaje
) as table2 on table1.IdMotivo = table2.IdMotivo2 
left outer join (
	SELECT _id, Nombre
	FROM Trips
) as trip on table1.IdViaje == Trip._id ,  Motivo
WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo, Viaje ORDER BY count1 DESC


////Viejos
SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1 
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 
            FROM Movimiento 
            WHERE  IdMoneda == 1 and strftime('%Y',Fecha) == '2019' and Cantidad < 0 GROUP BY IdMotivo
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda 
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == 1 
            then Cantidad * Cambio end) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) ==  '2019' GROUP BY IdMotivo
        union
        SELECT 
        SUM (CASE WHEN idMotivo == 3 and (
            SELECT 
                Totales.idMoneda 
                FROM Totales, Movimiento 
                WHERE Totales._id == IdTotales) == 1 
            THEN Cantidad * Cambio * -1 end) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 
            FROM Movimiento 
            WHERE strftime('%Y',Fecha) == '2019' GROUP BY IdMotivo) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2 
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 
            FROM Movimiento 
            WHERE Cantidad > 0 and IdMoneda == 1 and strftime('%Y',Fecha) == '2019' 
            Group BY IdMotivo2
        union
        SELECT SUM( CASE WHEN (
            SELECT Totales.idMoneda 
            FROM Totales, Movimiento 
            WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
        then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 
        FROM Movimiento 
        WHERE Cantidad < 0 and strftime('%Y',Fecha) == '2019' and IdMoneda == 1 and Cambio IS NOT NULL Group BY IdMotivo2
        union
        SELECT  
            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 
            From Totales, Movimiento 
            WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == 1 and strftime('%Y',Fecha) == '2019' Group BY IdMotivo ) as table3, Motivo 
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2
) as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo 
WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC


////OTR_AAAA


SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1 
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo, COUNT(IdMotivo) as count1 
            FROM Movimiento 
            WHERE  IdMoneda == 9 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' and Cantidad < 0 GROUP BY IdMotivo
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == 9 
                then Cantidad * Cambio end) as Gasto,
                IdMotivo, COUNT(IdMotivo) as count1 
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) ==  '05'  GROUP BY IdMotivo
        union
        SELECT 
            SUM (CASE WHEN idMotivo == 3 and (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales) == 9 THEN
                    Cantidad * Cambio * -1 end) as Gasto, 
                IdMotivo, COUNT(IdMotivo) as count1
                FROM Movimiento 
                WHERE strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' GROUP BY IdMotivo) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2 
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 
            FROM Movimiento WHERE Cantidad > 0 and IdMoneda == 9 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' Group BY IdMotivo2
        union
        SELECT 
        SUM( CASE WHEN (
            SELECT 
                Totales.idMoneda 
                FROM Totales, Movimiento 
                WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' and IdMoneda == 9 and Cambio IS NOT NULL Group BY IdMotivo2
        union
        SELECT  
            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2, COUNT(IdMotivo) as count1 
            From Totales, Movimiento 
            WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == 9 and strftime('%Y',Fecha) == '2019' and strftime('%m',Fecha) == '05' Group BY IdMotivo ) as table3, Motivo 
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2
) as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC
