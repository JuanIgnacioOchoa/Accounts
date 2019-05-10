//Nuevos año
						SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo
            FROM Movimiento 
            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == ? 
                then Cantidad * Cambio end) as Gasto,
                IdMotivo
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ?  GROUP BY IdMotivo
        union
        SELECT 
            SUM (CASE WHEN idMotivo == 3 and (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales) == ? THEN
                    Cantidad * Cambio * -1 end) as Gasto, 
                IdMotivo
                FROM Movimiento 
                WHERE strftime('%Y',Fecha) == ? GROUP BY IdMotivo) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2 
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2
            FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? Group BY IdMotivo2
        union
        SELECT 
        SUM( CASE WHEN (
            SELECT 
                Totales.idMoneda 
                FROM Totales, Movimiento 
                WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2
        union
        SELECT  
            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2
            From Totales, Movimiento 
            WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? and strftime('%Y',Fecha) == ? Group BY IdMotivo ) as table3, Motivo 
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2
) as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo 
                        union
                        SELECT 
                            Trips._id as _id, SUM(Gasto) as Gasto, Ingreso , Trips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje
                            FROM(
                                SELECT 
                                    sum(Cantidad ) as Gasto, IdViaje
                                    FROM Movimiento 
                                    WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and Cantidad < 0 GROUP BY IdViaje
                                union
                                SELECT 
                                    SUM( CASE WHEN (
                                        SELECT 
                                            Totales.IdMoneda 
                                            FROM Totales, Movimiento 
                                            WHERE Totales._id == IdTotales and Cambio > 0) == ? 
                                    then Cantidad * Cambio end) as Gasto, IdViaje
                                    FROM Movimiento 
                                    WHERE Cantidad < 0 and strftime('%Y',Fecha) ==  ? GROUP BY IdViaje
                                ) as table1 
                        LEFT OUTER JOIN (
                        SELECT 
                            SUM(Ingreso) as Ingreso, IdViaje
                            FROM (
                                SELECT 
                                    sum(Cantidad ) as Ingreso, IdViaje
                                    FROM Movimiento 
                                    WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ?
                                    Group BY IdViaje
                                union
                                SELECT SUM( CASE WHEN (
                                    SELECT Totales.idMoneda 
                                    FROM Totales, Movimiento 
                                    WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
                                then Cantidad * -1 end) as Ingreso, IdViaje
                                FROM Movimiento 
                                WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and Cambio IS NOT NULL Group BY IdViaje
                                ) as table3, Trips
                            WHERE table3.IdViaje == Trips._id GROUP BY IdViaje
                        ) as table2 on table1.IdViaje = table2.IdViaje ,  Trips
                        WHERE table1.IdViaje == Trips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC



                        
//mes y año
						SELECT 
    Motivo._id as _id, SUM(Gasto) as Gasto, Ingreso , Motivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje
    FROM(
        SELECT 
            sum(Cantidad ) as Gasto, IdMotivo
            FROM Movimiento 
            WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and Cantidad < 0 GROUP BY IdMotivo
        union
        SELECT 
            SUM( CASE WHEN (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales and Cambio > 0) == ? 
                then Cantidad * Cambio end) as Gasto,
                IdMotivo
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ?  GROUP BY IdMotivo
        union
        SELECT 
            SUM (CASE WHEN idMotivo == 3 and (
                SELECT 
                    Totales.idMoneda
                    FROM Totales, Movimiento 
                    WHERE Totales._id == IdTotales) == ? THEN
                    Cantidad * Cambio * -1 end) as Gasto, 
                IdMotivo
                FROM Movimiento 
                WHERE strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? GROUP BY IdMotivo) as table1 
LEFT OUTER JOIN (
SELECT 
    SUM(Ingreso) as Ingreso, IdMotivo2 
    FROM (
        SELECT 
            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2
            FROM Movimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? Group BY IdMotivo2
        union
        SELECT 
        SUM( CASE WHEN (
            SELECT 
                Totales.idMoneda 
                FROM Totales, Movimiento 
                WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
            then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2
            FROM Movimiento 
            WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and IdMoneda == ? and Cambio IS NOT NULL Group BY IdMotivo2
        union
        SELECT  
            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2
            From Totales, Movimiento 
            WHERE IdMotivo2 == 3 and Traspaso == Totales._id and Totales.IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? Group BY IdMotivo ) as table3, Motivo 
    WHERE table3.IdMotivo2 == Motivo._id GROUP BY IdMotivo2
) as table2 on table1.IdMotivo = table2.IdMotivo2 ,  Motivo WHERE table1.IdMotivo == Motivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo 
                        union
                        SELECT 
                            Trips._id as _id, SUM(Gasto) as Gasto, Ingreso , Trips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje
                            FROM(
                                SELECT 
                                    sum(Cantidad ) as Gasto, IdViaje
                                    FROM Movimiento 
                                    WHERE  IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ? and Cantidad < 0 GROUP BY IdViaje
                                union
                                SELECT 
                                    SUM( CASE WHEN (
                                        SELECT 
                                            Totales.IdMoneda 
                                            FROM Totales, Movimiento 
                                            WHERE Totales._id == IdTotales and Cambio > 0) == ? 
                                    then Cantidad * Cambio end) as Gasto, IdViaje
                                    FROM Movimiento 
                                    WHERE Cantidad < 0 and strftime('%Y',Fecha) ==  ? and strftime('%m',Fecha) ==  ? GROUP BY IdViaje
                                ) as table1 
                        LEFT OUTER JOIN (
                        SELECT 
                            SUM(Ingreso) as Ingreso, IdViaje
                            FROM (
                                SELECT 
                                    sum(Cantidad ) as Ingreso, IdViaje
                                    FROM Movimiento 
                                    WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ?
                                    Group BY IdViaje
                                union
                                SELECT SUM( CASE WHEN (
                                    SELECT Totales.idMoneda 
                                    FROM Totales, Movimiento 
                                    WHERE Totales._id == IdTotales and Cambio > 0) <> Movimiento.IdMoneda
                                then Cantidad * -1 end) as Ingreso, IdViaje
                                FROM Movimiento 
                                WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) ==  ? and Cambio IS NOT NULL Group BY IdViaje
                                ) as table3, Trips
                            WHERE table3.IdViaje == Trips._id GROUP BY IdViaje
                        ) as table2 on table1.IdViaje = table2.IdViaje ,  Trips
                        WHERE table1.IdViaje == Trips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC