//Nuevos 
SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, table1.IdViaje, trip.Nombre as Viaje, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1 
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo, IdViaje
            FROM Movimiento 
            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo, IdViaje
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda 
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == ? 
            then Cantidad * Cambio end) as Gasto, IdMotivo, IdViaje
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) ==  ? GROUP BY IdMotivo, IdViaje
        ) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2, IdViaje
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, IdViaje
            FROM Movimiento 
            WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? 
            Group BY IdMotivo2, IdViaje
        union
        SELECT SUM( CASE WHEN (
            SELECT Totales.idMoneda 
            FROM Totales, Movimiento 
            WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
        then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, IdViaje
        FROM Movimiento 
        WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and Cambio IS NOT NULL Group BY IdMotivo2, IdViaje
        ) as table3, Motivo
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2, IdViaje
) as table2 on table1.IdMotivo = table2.IdMotivo2
left outer join (
	SELECT _id, Nombre
	FROM Trips
) as trip on table1.IdViaje == Trip._id ,  Motivo
WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo, Viaje ORDER BY count1 DESC


//
SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, table1.IdViaje, trip.Nombre as Viaje, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1 
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo, IdViaje
            FROM Movimiento 
            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo, IdViaje
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == ? 
                then Cantidad * Cambio end) as Gasto,
                IdMotivo, IdViaje
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ?  GROUP BY IdMotivo, IdViaje
        ) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2, IdViaje
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2, IdViaje
            FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? Group BY IdMotivo2, IdViaje
        union
        SELECT 
        SUM( CASE WHEN (
            SELECT 
                Totales.idMoneda 
                FROM Totales, Movimiento 
                WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2, IdViaje
            FROM Movimiento 
            WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cambio IS NOT NULL Group BY IdMotivo2, IdViaje
        ) as table3, Motivo 
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2, IdViaje
) as table2 on table1.IdMotivo = table2.IdMotivo2 
left outer join (
	SELECT _id, Nombre
	FROM Trips
) as trip on table1.IdViaje == Trip._id ,  Motivo
WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo, Viaje ORDER BY count1 DESC