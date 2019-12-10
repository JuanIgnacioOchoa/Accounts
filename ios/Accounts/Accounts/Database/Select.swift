//
//  Select.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/17/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation

//MARK:Totales

func getTotal(id: Int64) -> Dictionary<String, Any?> {
    let query =
        """
    SELECT * from \(Totales.Table) WHERE _id = ?
    """
    
    do {
        let stmt = try Database.db.prepare(query, [id])
        let x = Database.stmtToDictionary(stmt: stmt)
        return x[0]
    } catch {
        print("Error select: ", error)
    }
    return [:]
}
func getTotales(inactivos: Bool) -> [[String:Any?]]{
    var activa = "";
    if(!inactivos){
        activa = "Activa == 1 and ";
    }
    let query =
        """
        SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.IdMoneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count ,
        AccountsTotales.CurrentCantidad FROM AccountsTotales, AccountsMoneda LEFT JOIN
        AccountsMovimiento on AccountsMovimiento.IdTotales= AccountsTotales._id and Fecha BETWEEN date('now', '-1 month') and date('now')
        WHERE \(activa) AccountsTotales.IdMoneda == AccountsMoneda._id
        GROUP BY AccountsTotales._id ORDER by Count DESC, Fecha DESC
    """
    
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getTotales(id: Int64) -> [[String:Any?]] {
    let query = """
        SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.IdMoneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count ,
        AccountsTotales.CurrentCantidad FROM AccountsTotales, AccountsMoneda LEFT JOIN
        AccountsMovimiento on AccountsMovimiento.IdTotales= AccountsTotales._id and Fecha BETWEEN date('now', '-1 month') and date('now')
        WHERE (Activa == 1 or AccountsTotales._id == ?) and AccountsTotales.IdMoneda == AccountsMoneda._id
        GROUP BY AccountsTotales._id ORDER by Count DESC, Fecha DESC
"""
    do {
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
    
}
func getTotales() -> [[String:Any?]] {
    let query = """
                SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.IdMoneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count ,
                AccountsTotales.CurrentCantidad FROM AccountsTotales, AccountsMoneda LEFT JOIN
                AccountsMovimiento on AccountsMovimiento.IdTotales= AccountsTotales._id and Fecha BETWEEN date('now', '-1 month') and date('now')
                WHERE (Activa == 1) and AccountsTotales.IdMoneda == AccountsMoneda._id
                GROUP BY AccountsTotales._id ORDER by Count DESC, Fecha DESC
"""
    
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
    
}

func getTotalesTotales(idMoneda:Int64) -> [[String:Any?]]{
    let q = """
                SELECT * FROM (
                  select Sum(CurrentCantidad) as Positivo from AccountsTotales where Activa = 1 and CurrentCantidad > 0 and IdMoneda = ?
                ) as t1, (
                  select Sum(CurrentCantidad) as Negativo from AccountsTotales where Activa = 1 and CurrentCantidad < 0 and IdMoneda = ?
                ) as t2
            """
    do {
        let stmt = try Database.db.run(q, [idMoneda, idMoneda])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        return []
    }
}
func getTotalesCash(idMoneda:Int64, inactivos:Bool) -> Double{
    var activa = ""
    if(!inactivos){
        activa = "Activa == 1 and "
    }
    let q = """
                Select Sum(t.CurrentCantidad) as Cantidad From AccountsTiposCuentas as tc
                LEFT JOIN AccountsTotales as t on t.Tipo = tc._id where \(activa) t.IdMoneda = ? and (tc._id = 1 or tc._id = 3)
"""
        do {
            let stmt = try Database.db.prepare(q, [idMoneda])
            let total = stmt.next()![0] as? Double
            return total ?? 0
            //return Database.stmtToDictionary(stmt: stmt)
        } catch {
            print("Error select getTotalesCash: ", error)
        }
        return 0.0
}

func getTotalesCreditCard(idMoneda:Int64, inactivos:Bool) -> Double{
    var activa = ""
    if(!inactivos){
        activa = "Activa == 1 and "
    }
    let q = """
                Select Sum(t.CurrentCantidad) as Cantidad From AccountsTiposCuentas as tc
                LEFT JOIN AccountsTotales as t on t.Tipo = tc._id where \(activa) t.IdMoneda = ? and (tc._id = 2)
"""
        do {
            let stmt = try Database.db.prepare(q, [idMoneda])
            let total = stmt.next()![0] as? Double
            return total ?? 0
            //return Database.stmtToDictionary(stmt: stmt)
        } catch {
            print("Error select getTotalesCreditCard: ", error)
        }
        return 0.0
}

func getTotalesInvests(idMoneda:Int64, inactivos:Bool) -> Double{
    var activa = ""
    if(!inactivos){
        activa = "Activa == 1 and "
    }
    let q = """
                Select Sum(t.CurrentCantidad) as Cantidad From AccountsTiposCuentas as tc
                LEFT JOIN AccountsTotales as t on t.Tipo = tc._id where \(activa) t.IdMoneda = ? and (tc._id = 4)
"""
        do {
            let stmt = try Database.db.prepare(q, [idMoneda])
            let total = stmt.next()![0] as? Double
            return total ?? 0
            //return Database.stmtToDictionary(stmt: stmt)
        } catch {
            print("Error select getTotalesInvests: ", error)
        }
        return 0.0
}

func getTotalesDeudores(idMoneda:Int64) -> Double{

    let q = """
SELECT * FROM (
   select (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad From(
       (select sum(Cantidad) as Cantidad, IdMoneda from AccountsPrestamos where IdMovimiento == 0 and IdMoneda = ? ) as table1
   left join (
       select sum(pd.Cantidad * pd.Cambio) as CantidadMenos, pd.IdMoneda
       from AccountsPrestamos as p, AccountsPrestamosDetalle as pd
       where p._id = pd.IdPrestamo and p.IdMovimiento == 0 and pd.IdMoneda = ?
   group by p.IdMoneda) as table2 on table1.IdMoneda = table2.IdMoneda)
)
"""
        do {
            let stmt = try Database.db.prepare(q, [idMoneda, idMoneda])
            let total = stmt.next()![0] as? Double
            return total ?? 0
            //return Database.stmtToDictionary(stmt: stmt)
        } catch {
            print("Error select getTotalesDeudores: ", error)
        }
        return 0.0
}

func getTotalesDeudas(idMoneda:Int64) -> Double{

    let q = """
SELECT * FROM (
   select (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad From(
       (select sum(Cantidad) as Cantidad, IdMoneda from AccountsPrestamos where IdMovimiento <> 0 and IdMoneda = ? ) as table1
   left join (
       select sum(pd.Cantidad * pd.Cambio) as CantidadMenos, pd.IdMoneda
       from AccountsPrestamos as p, AccountsPrestamosDetalle as pd
       where p._id = pd.IdPrestamo and p.IdMovimiento <> 0 and pd.IdMoneda = ?
   group by p.IdMoneda) as table2 on table1.IdMoneda = table2.IdMoneda)
)
"""
        do {
            let stmt = try Database.db.prepare(q, [idMoneda, idMoneda])
            let total = stmt.next()![0] as? Double
            return total ?? 0
            //return Database.stmtToDictionary(stmt: stmt)
        } catch {
            print("Error select getTotalesDeudas: ", error)
        }
        return 0.0
}

//MARK: Movimiento

func getMoveData(id:Int64) -> [[String:Any?]] {
    let query = "SELECT _id, Cantidad, (case strftime('%m', Fecha) when '01' then 'Jan'" +
    " when '02' then 'Feb' when '03' then 'Mar' when '04' then 'Apr' when '05' then 'May' " +
    "when '06' then 'Jun' when '07' then 'Jul' when '08' then 'Aug' when '09' then 'Sep' " +
    "when '10' then 'Oct' when '11' then 'Nov' when '12' then 'Dec' else '' " +
    "end ||'-'|| strftime('%d-%Y', Fecha))  as Fecha, Fecha as nFecha, " +
    "IdTotales, Comment, IdMotivo, IdMoneda, Cambio, Traspaso, IdViaje " +
    " FROM AccountsMovimiento WHERE _id = ?"
    
    do {
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select getMoveData: ", error)
    }
    
    return []
}
func getMovimientos() -> [[String:Any?]]{
    let query = """
SELECT mov._id, mov.Cantidad, mov.Fecha, t.Cuenta, mot.Motivo, mon.Moneda, mov.Cambio, mov.Traspaso, mov.comment, mov.IdViaje
    FROM AccountsMovimiento as mov, AccountsMoneda as mon, AccountsMotivo as mot, AccountsTotales as t
        WHERE mov.IdMotivo = mot._id and mov.IdMoneda = mon._id and mov.IdTotales = t._id and strftime('%Y',Fecha) = strftime('%Y', date('now'))and
        strftime('%m',Fecha) = strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and
        Traspaso is null ORDER BY Fecha DESC, mov._id DESC
"""
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}
/*
func getMovimientosFecha(year:String) -> [[String:Any?]]{
    let query = """
SELECT mov._id, mov.Cantidad, mov.Fecha, t.Cuenta, mot.Motivo, mon.Moneda, mov.Cambio, mov.Traspaso, mov.comment, mov.IdViaje
    FROM AccountsMovimiento as mov, AccountsMoneda as mon, AccountsMotivo as mot, AccountsTotales as t
        WHERE mov.IdMotivo = mot._id and mov.IdMoneda = mon._id and mov.IdTotales = t._id and strftime('%Y',Fecha) = strftime('%Y', date('now'))and
        strftime('%m',Fecha) = strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and
        Traspaso is null ORDER BY Fecha DESC, mov._id DESC
"""
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}
*/

func getTotalMoves(id: Int64) -> [[String:Any?]]{
    let query = "SELECT m.*, mot.Motivo FROM AccountsMovimiento as m, AccountsMotivo as mot where m.IdMotivo = mot._id and Fecha BETWEEN date('now', '-1 month') and date('now') and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC"
    do{
        let stmt = try Database.db.prepare(query, [id, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getTotalMoves: ", error)
    }
    return []
}
func getTotalMoves(id: Int64, year: Int) -> [[String:Any?]]{
    let query = "SELECT m.*, mot.Motivo FROM AccountsMovimiento as m, AccountsMotivo as mot where m.IdMotivo = mot._id and strftime('%Y',Fecha) == ? and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC"
    do{
        let stmt = try Database.db.prepare(query, [year, id, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getTotalMoves: ", error)
    }
    return []
}

func getTotalMoves(id: Int64, month: Int, year: Int) -> [[String:Any?]]{
    let query = "SELECT m.*, mot.Motivo FROM AccountsMovimiento as m, AccountsMotivo as mot where m.IdMotivo = mot._id and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC"
    do{
        let stmt = try Database.db.prepare(query, [year, month, id, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getTotalMoves: ", error)
    }
    return []
}
func getTotalesMovFecha(id: Int64, year:String?, month:String?) -> [[String:Any?]] {
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and Fecha BETWEEN date('now', '-1 month') and date('now')"
        tmpM = ""
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
        SELECT * FROM (
        SELECT m._id, m.Fecha FROM AccountsMovimiento as m, AccountsTotales as t WHERE t._id = m.IdTotales \(tmpY) \(tmpM) and (IdTotales = ? or Traspaso = ?) Group by m.Fecha
        union
        SELECT p._id, p.Fecha FROM AccountsPrestamos as p, AccountsTotales as t WHERE t._id = p.IdTotales \(tmpY) \(tmpM) and (IdTotales = ?) Group by p.Fecha
        union
        SELECT p._id, p.Fecha FROM AccountsPrestamosDetalle as p, AccountsTotales as t WHERE t._id = p.IdTotales \(tmpY) \(tmpM) and (IdTotales = ?) Group by p.Fecha
        ) Group by Fecha ORDER BY Fecha DESC, _id DESC
    """
    do{
        let stmt = try Database.db.prepare(query, [id, id, id, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getMovimientosFecha 0", error)
    }
    return []
}
func getMotivosMovFecha(id: Int64, year:String?, month:String?, moneda: Int64) -> [[String:Any?]] {
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and strftime('%Y',Fecha) == strftime('%Y', date('now'))"
        tmpM = "and strftime('%m',Fecha) == strftime('%m', date('now'))"
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
        SELECT m._id, m.Fecha
            FROM AccountsMovimiento as m, AccountsMotivo as mot
            WHERE mot._id = m.IdMotivo \(tmpY) \(tmpM) and IdMotivo = ? and IdMoneda = ?
        Group by m.Fecha ORDER BY Fecha DESC, m._id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [id, moneda])
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getMovimientosFecha 0", error)
    }
    return []
}
func getTripsMovimientosFecha(id:Int64) -> [[String:Any?]] {
    let query = """
                SELECT _id, Fecha FROM AccountsMovimiento
                    WHERE IdViaje = ?
                GROUP BY Fecha ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getTripsMovimientosFecha 0", error)
    }
    return []
}
func getMovimientosFecha() -> [[String:Any?]] {
    let query = """
                SELECT _id, Fecha FROM AccountsMovimiento
                    WHERE strftime('%Y',Fecha) = strftime('%Y', date('now'))and
                    strftime('%m',Fecha) = strftime('%m',date('now')) and strftime('%d',Fecha) >= 1
                GROUP BY Fecha ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getMovimientosFecha 0", error)
    }
    return []
}
func getMovimientosFecha(year:String) -> [[String:Any?]] {
    let query = """
                SELECT _id, Fecha FROM AccountsMovimiento
                    WHERE strftime('%Y',Fecha) = ? and strftime('%d',Fecha) >= 1
                GROUP BY Fecha ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [year])
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getMovimientosFecha 0", error)
    }
    return []
}
func getMovimientosFecha(year:String, month:String) -> [[String:Any?]] {
    let query = """
                SELECT _id, Fecha FROM AccountsMovimiento
                    WHERE strftime('%Y',Fecha) = ? and
                    strftime('%m',Fecha) = ? and strftime('%d',Fecha) >= 1
                GROUP BY Fecha ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [year, month])
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getMovimientosFecha 0", error)
    }
    return []
}

func gteMovimientosByDate(date:String) -> [[String:Any?]]{
    let query = """
                SELECT mov.*, mot.Motivo, mon.Moneda, t.Cuenta
                    FROM AccountsMovimiento as mov, AccountsTotales as t, AccountsMotivo as mot, AccountsMoneda as mon
                    WHERE mov.IdTotales = t._id and mov.IdMotivo = mot._id and mov.IdMoneda = mon._id and Fecha = ? ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [date])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getMovimientosByDate ", error)
    }
    return []
}

func getTotalesMovimientosByDate(id: Int64, date:String) -> [[String:Any?]]{
    let query = """
                SELECT mov.*, mon.Moneda, t.Cuenta FROM(
                        SELECT mov.*, '0' as Prestamo, mot.Motivo
                            FROM AccountsMovimiento as mov , AccountsMotivo as mot
                            WHERE mov.IdMotivo = mot._id and (IdTotales = ? or Traspaso = ?) and Fecha = ?
                        union
                        SELECT _id, Cantidad, FEcha, IdTotales, IdPersona, IdMoneda, Cambio, null as Traspaso, comment, null as IdViaje, '1' as Prestamo, 'Prestamo' as Motivo
                            FROM AccountsPrestamos
                            WHERE IdTotales = ? and Fecha = ?
                        union
                        SELECT _id, Cantidad, FEcha, IdTotales, null as IdPersona, IdMoneda, Cambio, null as Traspaso, null as IdComment, null as IdViaje, '2' as Prestamo, 'Prestamo' as Motivo
                            FROM AccountsPrestamosDetalle
                            WHERE IdTotales = ? and Fecha = ?
                    ) as mov, AccountsMoneda as mon, AccountsTotales as t
                    WHERE mon._id = mov.IdMoneda and t._id = mov.IdTotales
                Order by Fecha DEsc, _id desc
"""
    do{
        let stmt = try Database.db.prepare(query, [id, id, date, id, date, id, date])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getMovimientosByDate ", error)
    }
    return []
}

func getMotivosMovimientosByDate(id: Int64, date:String, moneda: Int64) -> [[String:Any?]]{
    let query = """
                SELECT mov.*, mot.Motivo, mon.Moneda, t.Cuenta
                    FROM AccountsMovimiento as mov, AccountsTotales as t, AccountsMotivo as mot, AccountsMoneda as mon
                    WHERE mov.IdTotales = t._id and mov.IdMotivo = mot._id and mov.IdMoneda = mon._id and Fecha = ? and IdMotivo = ? and mov.IdMoneda = ? ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [date, id, moneda])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getMovimientosByDate ", error)
    }
    return []
}
func getTripsMovimientosByDate(id: Int64, date:String) -> [[String:Any?]]{
    let query = """
                SELECT mov.*, mot.Motivo, mon.Moneda, t.Cuenta
                    FROM AccountsMovimiento as mov, AccountsTotales as t, AccountsMotivo as mot, AccountsMoneda as mon
                    WHERE mov.IdTotales = t._id and mov.IdMotivo = mot._id and mov.IdMoneda = mon._id and Fecha = ? and IdViaje = ? ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [date, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getTripsMovimientosByDate ", error)
    }
    return []
}

//MARK:Monedas

func getMonedas() -> [[String:Any?]]{
    let query = """
            SELECT mon._id, mon.Moneda, Cuenta, Active FROM (
                SELECT * FROM AccountsMoneda WHERE _id > 0
            ) as mon
            left join(
                SELECT max(Fecha) as Fecha, IdMoneda, count(IdMoneda) as Cuenta FROM AccountsMovimiento
                    WHERE Fecha BETWEEN date('now', '-1 month') and date('now') GROUP BY IdMoneda
            ) as mov on mon._id = mov.IdMoneda
            GROUP BY mon._id, mon.Moneda Order By Cuenta desc, Fecha desc
"""
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getMonedasWith(id:Int64) -> [[String:Any?]]{
    let query = """
            SELECT mon._id, mon.Moneda, Cuenta, Active FROM (
                SELECT * FROM AccountsMoneda WHERE (_id > 0 and Active = 1) or _id = ?
            ) as mon
            left join(
                SELECT max(Fecha) as Fecha, IdMoneda, count(IdMoneda) as Cuenta FROM AccountsMovimiento
                    WHERE Fecha BETWEEN date('now', '-1 month') and date('now') GROUP BY IdMoneda
            ) as mov on mon._id = mov.IdMoneda
            GROUP BY mon._id, mon.Moneda Order By Cuenta desc, Fecha desc
"""
    do {
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

//----------------------------End Monedas

//MARK: Motivos
func getMotives(id:Int64) -> [[String:Any?]] {
    let query = """
                SELECT mot._id, mot.Motivo, Cuenta, Fecha FROM (
                    SELECT * FROM AccountsMotivo WHERE _id > 15 and Active = 1 or _id = ?
                ) as mot
                left join(
                    SELECT max(Fecha) as Fecha, IdMotivo, count(IdMotivo) as Cuenta FROM AccountsMovimiento
                        WHERE Fecha BETWEEN date('now', '-1 month') and date('now') GROUP BY IdMotivo
                ) as mov on mot._id = mov.IdMotivo
                GROUP BY mot._id, mot.Motivo Order By Cuenta desc, Fecha desc
"""
    
    do {
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getMotives(active:Bool) -> [[String:Any?]] {
    var act = ""
    if active {
        act = "and Active = 1"
    }
    let query = """
                SELECT mot._id, mot.Motivo, Cuenta, Fecha, Active FROM (
                    SELECT * FROM AccountsMotivo WHERE _id > 15 \(act)
                ) as mot
                left join(
                    SELECT max(Fecha) as Fecha, IdMotivo, count(IdMotivo) as Cuenta FROM AccountsMovimiento
                        WHERE Fecha BETWEEN date('now', '-1 month') and date('now') GROUP BY IdMotivo
                ) as mov on mot._id = mov.IdMotivo
                GROUP BY mot._id, mot.Motivo Order By Active desc, Cuenta desc, Fecha desc
"""
    
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}
//----------------------------End Motivos

//MARK: TIPO CUENTAS

func getTiposCuentas() -> [[String:Any?]] {
        let query = """
    SELECT * FROM \(TiposCuentas.Table) WHERE _id <> 5
    """
        
        do {
            let stmt = try Database.db.prepare(query)
            return Database.stmtToDictionary(stmt: stmt)
        } catch {
            print("Error select: ", error)
        }
        return []
}
//MARK: Prestamos

func getPrestamosByPeople(zero:Bool) -> [[String:Any?]] {
    var s = ""
    if zero {
        s = " and Cantidad <> 0 "
    }
    
    let query = """
    SELECT AccountsPersonas._id, AccountsPersonas.Nombre, AccountsMoneda.Moneda, SUM(Cantidad * b) as Cantidad, Fecha FROM(
       SELECT *, (1) as b FROM(
       (
           select (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad, table1.IdPersona, table1.IdMoneda, table1.Fecha  From(
               (select sum(Cantidad) as Cantidad, IdPersona, IdMoneda, Fecha from AccountsPrestamos where IdMovimiento = 0 group by IdPersona, IdMoneda) as table1
               left join (
    select sum(pd.Cantidad * pd.Cambio) as CantidadMenos, p.IdPersona, p.IdMoneda
    from AccountsPrestamos as p, AccountsPrestamosDetalle as pd
    where p._id = pd.IdPrestamo and p.IdMovimiento = 0
    group by p.IdPersona, p.IdMoneda) as table2 on table1.IdPersona = table2.IdPersona and table1.IdMoneda = table2.IdMoneda)
    ) as table1
    )
    union all
    SELECT *, (1)/*(-1)*/ as b FROM(
    (
    select (Cantidad - coalesce(CantidadMenos, 0)) as Cantidad, table1.IdPersona, table1.IdMoneda, table1.Fecha  From(
    (select sum(Cantidad) as Cantidad, IdPersona, IdMoneda, Fecha from AccountsPrestamos where IdMovimiento <> 0 group by IdPersona, IdMoneda) as table1
    left join (
    select sum(pd.Cantidad * pd.Cambio) as CantidadMenos, p.IdPersona, p.IdMoneda
    from AccountsPrestamos as p, AccountsPrestamosDetalle as pd
    where p._id = pd.IdPrestamo and p.IdMovimiento <> 0
    group by p.IdPersona, p.IdMoneda) as table2 on table1.IdPersona = table2.IdPersona and table1.IdMoneda = table2.IdMoneda)
    ) as table2
    )
    ), AccountsPersonas, AccountsMoneda
    where IdPersona = AccountsPersonas._id and IdMoneda = AccountsMoneda._id \(s) group by IdPersona, IdMoneda order by Fecha desc
    """
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getPrestamosPlus(zero: Bool) -> [[String:Any?]] {
    var s = ""
    if zero {
        s = " and Cantidad <> 0 "
    }
    
    let query = """
        select * from (
            select p._id, p.Fecha, AccountsTotales.Cuenta, AccountsMoneda.Moneda, Comment, AccountsPersonas.Nombre, p.Cambio, p.IdMovimiento, p.Cerrada,
            (p.Cantidad - coalesce(pd.Cantidad, 0)) as Cantidad from AccountsPrestamos as p
            left join(
            select _id, (SUM(Cantidad * Cambio)) as Cantidad, IdPrestamo from  AccountsPrestamosDetalle group by IdPrestamo
            ) as pd on p._id = pd.IdPrestamo, AccountsPersonas, AccountsMoneda, AccountsTotales
            Where AccountsPersonas._id = p.IdPersona and AccountsMoneda._id = p.IdMoneda and AccountsTotales._id = p.IdTotales
            ) WHERE Cantidad >= 0 \(s) order by Fecha desc
    """
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getPrestamosMinus(zero: Bool) -> [[String:Any?]] {
    var s = ""
    if zero {
        s = " and Cantidad <> 0 "
    }
    
    let query = """
            select * from (
            select p._id, p.Fecha, AccountsTotales.Cuenta, AccountsMoneda.Moneda, Comment, AccountsPersonas.Nombre,
            p.Cambio, p.IdMovimiento, p.Cerrada,
            (p.Cantidad - coalesce(pd.Cantidad, 0)) as Cantidad from AccountsPrestamos as p
            left join(
            select _id, (SUM(Cantidad * Cambio)) as Cantidad, IdPrestamo from AccountsPrestamosDetalle group by IdPrestamo
            ) as pd on p._id = pd.IdPrestamo, AccountsPersonas, AccountsMoneda, AccountsTotales
            Where AccountsPersonas._id = p.IdPersona and AccountsMoneda._id = p.IdMoneda and AccountsTotales._id = p.IdTotales
            ) WHERE Cantidad <= 0 \(s) order by Fecha desc
    """
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getPrestamos(id:Int64) -> [[String:Any?]] {
    let query = """
        SELECT * From AccountsPrestamos WHERE _id = ?
    """
    do {
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getPrestamoDetalle(id:Int64) -> [[String:Any?]] {
    let query = """
        SELECT det.*, t.Cuenta, m.Moneda
            FROM AccountsPrestamosDetalle as det, AccountsTotales as t, AccountsMoneda as m
                WHERE t._id = det.IdTotales and m._id = det.IdMoneda and IdPrestamo = ?
    """
    do {
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

func getTotalPaid(id:Int64) -> Double {
    let query = """
                SELECT SUM(Cantidad * Cambio) as Cantidad
                    FROM \(PrestamosDet.Table) WHERE \(PrestamosDet.IdPrestamo) = ?
                """
    do {
       let stmt = try Database.db.prepare(query, [id])
       let gasto = stmt.next()![0] as? Double
       return gasto ?? 0
       //return Database.stmtToDictionary(stmt: stmt)
    } catch {
       print("Error select getGastoTotalByMonedaFromCurrentMonth: ", error)
    }
    return 0
}

//--------------------------------End Prestamos

//MARK: Personas
func getPersonas() -> [[String:Any?]] {
    let query = """
        SELECT AccountsPersonas._id as _id, AccountsPersonas.Nombre as Nombre, COUNT(AccountsPersonas.Nombre) as Count
        FROM AccountsPersonas LEFT JOIN
        AccountsPrestamos on AccountsPrestamos.IdPersona= AccountsPersonas._id and date('now','-1 month') <= date('now')
        WHERE Active == 1 GROUP BY AccountsPersonas._id union
         select -1 as _id,  'Otro' as Nombre, -1 as Count
         order by Count desc
    """
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

//----------------------------End Personas

//MARK: Reportes

func getGastoTotalByMoneda(moneda:Int64, year:String?, month:String?) -> Double{
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and strftime('%Y',Fecha) == strftime('%Y', date('now'))"
        tmpM = "and strftime('%m',Fecha) == strftime('%m', date('now'))"
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
            SELECT SUM(Gasto) as Gasto FROM(
                SELECT sum(Cantidad ) as Gasto FROM AccountsMovimiento WHERE Cantidad < 0 and IdMoneda == ? \(tmpY) \(tmpM)
                union
                SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ?
                then Cantidad * Cambio end) FROM AccountsMovimiento WHERE Cantidad < 0 \(tmpY) \(tmpM) and Cambio <> 1
                union
                SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales) == ? THEN
                 Cantidad * Cambio * -1 end) FROM AccountsMovimiento WHERE Fecha is not null \(tmpY) \(tmpM))
"""
    do {
        let stmt = try Database.db.prepare(query, [moneda, moneda, moneda])
        let gasto = stmt.next()![0] as? Double
        return gasto ?? 0
        //return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select getGastoTotalByMonedaFromCurrentMonth: ", error)
    }
    return 0
}

func getIngresoTotalByMoneda(moneda:Int64, year:String?, month:String?) -> Double{
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and strftime('%Y',Fecha) == strftime('%Y', date('now'))"
        tmpM = "and strftime('%m',Fecha) == strftime('%m', date('now'))"
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
                SELECT SUM(Ingreso) as Ingreso FROM (
                  SELECT sum(Cantidad ) as Ingreso FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda == ? \(tmpY) \(tmpM)
                union
                
                SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda
                                then Cantidad * -1 end) as Ingreso FROM AccountsMovimiento WHERE Cantidad < 0 \(tmpY) \(tmpM)
                and IdMoneda == ? and Cambio IS NOT NULL and Cambio <> 1
                union
                SELECT  SUM(Cantidad) as Ingreso From AccountsTotales, AccountsMovimiento WHERE IdMotivo == 3 and Traspaso == AccountsTotales._id and AccountsTotales.IdMoneda == ? \(tmpY) \(tmpM)
                )
"""
    do {
        let stmt = try Database.db.prepare(query, [moneda, moneda, moneda])
        let ingreso = stmt.next()![0] as? Double
        return ingreso ?? 0
    } catch {
        print("Error select getGastoTotalByMonedaFromMonth: ", error)
    }
    return 0
}

func getTotalesByMonth(month: String, year: String) -> [[String:Any?]]{
    let query = "select AccountsTotales._id, AccountsTotales.Cuenta, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso\n" +
        "from(\n" +
        "\tSELECT t._id as idTotales, sum(Gasto) as Gasto \n" +
        "\tfrom(\n" +
        "\t\tSelect _id \n" +
        "\t\tfrom(\n" +
        "\t\t\tselect AccountsTotales.*, AccountsMovimiento.* \n" +
        "\t\t\tfrom AccountsTotales\n" +
        "\t\t\tLEFT JOIN AccountsMovimiento\n" +
        "\t\t\t\ton AccountsTotales._id = AccountsMovimiento.IdTotales\n" +
        "\t\t\t\tWHERE ((strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?))\n" +
        "\t\t\tGroup by idTotales\n" +
        "\t\tUnion all\n" +
        "\t\t\tSelect AccountsTotales.* , AccountsMovimiento.*\n" +
        "\t\t\tfrom AccountsMovimiento\n" +
        "\t\t\tLEFT JOIN AccountsTotales\n" +
        "\t\t\t\tOn AccountsTotales._id = AccountsMovimiento.IdTotales Where AccountsTotales.Activa\n" +
        "\t\t\tGroup by idTotales\n" +
        "\t\t) group by _id\n" +
        "\t) as t \n" +
        "\tLeft outer join (\n" +
        "\t\tSELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
        "\t\tfrom AccountsMovimiento \n" +
        "\t\t\tWHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
        "\t\tGroup by idTotales\n" +
        "\t\tunion\n" +
        "\t\tselect (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales \n" +
        "\t\tfrom AccountsMovimiento \n" +
        "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
        "\t\tGroup by idTotales\n" +
        "\t) as table3 on t._id == table3.idTotales Group by t._id\n" +
        ") as table1\n" +
        "\tLEFT OUTER JOIN (\n" +
        "\tselect idTotales, sum(Ingreso) as Ingreso \n" +
        "\t\tfrom(\n" +
        "\t\t\tselect idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso \n" +
        "\t\t\tfrom AccountsMovimiento \n" +
        "\t\t\t\tWHERE Cantidad > 0 and Traspaso is null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
        "\t\t\tGroup by idTotales\n" +
        "\t\tunion \n" +
        "\t\tselect  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso \n" +
        "\t\tfrom AccountsMovimiento \n" +
        "\t\t\tWHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ? \n" +
        "\t\tgroup by Traspaso\n" +
        "\t) Group by idTotales\n" +
        ") as table2 on table1.idTotales == table2.idTotales, AccountsMovimiento, AccountsTotales \n" +
        "\tWHERE (AccountsTotales._id == table1.idTotales or AccountsTotales._id == table2.idTotales) \n" +
        "group by AccountsTotales._id\n" +
    "\n"
    
    do {
        let stmt = try Database.db.prepare(query, [year, month, year, month, year, month, year, month, year, month] )
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}


func getSumByMotivesMonthly(idMoneda:Int64, month:String, year:String) -> [[String:Any?]]{
    let query = """
    SELECT
        AccountsMotivo._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsMotivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje
        FROM(
            SELECT
                sum(Cantidad ) as Gasto, IdMotivo
                FROM AccountsMovimiento
                WHERE  IdMoneda = ? and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? and Cantidad < 0 GROUP BY IdMotivo
            union
            SELECT
                SUM( CASE WHEN (
                    SELECT
                        AccountsTotales.idMoneda
                        FROM AccountsTotales, AccountsMovimiento
                        WHERE AccountsTotales._id = IdTotales and Cambio > 0) = ?
                    then Cantidad * Cambio end) as Gasto,
                    IdMotivo
                FROM AccountsMovimiento
                WHERE Cantidad < 0 and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? and Cambio <> 1 GROUP BY IdMotivo
            union
            SELECT
                SUM (CASE WHEN idMotivo = 3 and (
                    SELECT
                        AccountsTotales.idMoneda
                        FROM AccountsTotales, AccountsMovimiento
                        WHERE AccountsTotales._id = IdTotales) = ? THEN
                        Cantidad * Cambio * -1 end) as Gasto,
                    IdMotivo
                    FROM AccountsMovimiento
                    WHERE strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? GROUP BY IdMotivo) as table1
    LEFT OUTER JOIN (
    SELECT
        SUM(Ingreso) as Ingreso, IdMotivo2
        FROM (
            SELECT
                sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2
                FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda = ? and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? Group BY IdMotivo2
            union
            SELECT
            SUM( CASE WHEN (
                SELECT
                    AccountsTotales.idMoneda
                    FROM AccountsTotales, AccountsMovimiento
                    WHERE AccountsTotales._id = IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda
                then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2
                FROM AccountsMovimiento
                WHERE Cantidad < 0 and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? and IdMoneda = ? and Cambio IS NOT NULL Group BY IdMotivo2
            union
            SELECT
                SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2
                From AccountsTotales, AccountsMovimiento
                WHERE IdMotivo2 = 3 and Traspaso = AccountsTotales._id and AccountsTotales.IdMoneda = ? and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? Group BY IdMotivo ) as table3, AccountsMotivo
        WHERE table3.IdMotivo2 = AccountsMotivo._id GROUP BY IdMotivo2
    ) as table2 on table1.IdMotivo = table2.IdMotivo2 ,  AccountsMotivo WHERE table1.IdMotivo = AccountsMotivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo
                            union
                            SELECT
                                AccountsTrips._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsTrips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje
                                FROM(
                                    SELECT
                                        sum(Cantidad ) as Gasto, IdViaje
                                        FROM AccountsMovimiento
                                        WHERE  IdMoneda = ? and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? and Cantidad < 0 GROUP BY IdViaje
                                    union
                                    SELECT
                                        SUM( CASE WHEN (
                                            SELECT
                                                AccountsTotales.IdMoneda
                                                FROM AccountsTotales, AccountsMovimiento
                                                WHERE AccountsTotales._id = IdTotales and Cambio > 0) = ?
                                        then Cantidad * Cambio end) as Gasto, IdViaje
                                        FROM AccountsMovimiento
                                        WHERE Cantidad < 0 and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? and Cambio <> 1 GROUP BY IdViaje
                                    ) as table1
                            LEFT OUTER JOIN (
                            SELECT
                                SUM(Ingreso) as Ingreso, IdViaje
                                FROM (
                                    SELECT
                                        sum(Cantidad ) as Ingreso, IdViaje
                                        FROM AccountsMovimiento
                                        WHERE Cantidad > 0 and IdMoneda = ? and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ?
                                        Group BY IdViaje
                                    union
                                    SELECT SUM( CASE WHEN (
                                        SELECT AccountsTotales.idMoneda
                                        FROM AccountsTotales, AccountsMovimiento
                                        WHERE AccountsTotales._id = IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda
                                    then Cantidad * -1 end) as Ingreso, IdViaje
                                    FROM AccountsMovimiento
                                    WHERE Cantidad < 0 and IdMoneda = ? and strftime('%Y',Fecha) = ? and strftime('%m',Fecha) = ? and Cambio IS NOT NULL Group BY IdViaje
                                    ) as table3, AccountsTrips
                                WHERE table3.IdViaje = AccountsTrips._id GROUP BY IdViaje
                            ) as table2 on table1.IdViaje = table2.IdViaje ,  AccountsTrips
                            WHERE table1.IdViaje = AccountsTrips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC
"""
    do {
        let stmt = try Database.db.prepare(query, [idMoneda, year, month, idMoneda, year, month, idMoneda, year, month, idMoneda, year, month, idMoneda, year, month, idMoneda, year, month, idMoneda, year, month, idMoneda, year, month, idMoneda, year, month, idMoneda, year, month])
       
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return []
    }
}

func getReportesMotives(idMoneda:Int64, year:String?, month:String?) -> [[String:Any?]]{
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and strftime('%Y',Fecha) == strftime('%Y', date('now'))"
        tmpM = "and strftime('%m',Fecha) == strftime('%m', date('now'))"
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
    SELECT
        AccountsMotivo._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsMotivo.Motivo as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (0) as isViaje
        FROM(
            SELECT
                sum(Cantidad ) as Gasto, IdMotivo
                FROM AccountsMovimiento
                WHERE  IdMoneda = ? \(tmpY) \(tmpM) and Cantidad < 0 GROUP BY IdMotivo
            union
            SELECT
                SUM( CASE WHEN (
                    SELECT
                        AccountsTotales.idMoneda
                        FROM AccountsTotales, AccountsMovimiento
                        WHERE AccountsTotales._id = IdTotales and Cambio > 0) = ?
                    then Cantidad * Cambio end) as Gasto,
                    IdMotivo
                FROM AccountsMovimiento
                WHERE Cantidad < 0 \(tmpY) \(tmpM) and Cambio <> 1 GROUP BY IdMotivo
            union
            SELECT
                SUM (CASE WHEN idMotivo = 3 and (
                    SELECT
                        AccountsTotales.idMoneda
                        FROM AccountsTotales, AccountsMovimiento
                        WHERE AccountsTotales._id = IdTotales) = ? THEN
                        Cantidad * Cambio * -1 end) as Gasto,
                    IdMotivo
                    FROM AccountsMovimiento
                    WHERE Fecha is not null \(tmpY) \(tmpM) GROUP BY IdMotivo) as table1
    LEFT OUTER JOIN (
    SELECT
        SUM(Ingreso) as Ingreso, IdMotivo2
        FROM (
            SELECT
                sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo2
                FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda = ? \(tmpY) \(tmpM) Group BY IdMotivo2
            union
            SELECT
            SUM( CASE WHEN (
                SELECT
                    AccountsTotales.idMoneda
                    FROM AccountsTotales, AccountsMovimiento
                    WHERE AccountsTotales._id = IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda
                then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2
                FROM AccountsMovimiento
                WHERE Cantidad < 0 \(tmpY) \(tmpM) and IdMoneda = ? and Cambio IS NOT NULL Group BY IdMotivo2
            union
            SELECT
                SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2
                From AccountsTotales, AccountsMovimiento
                WHERE IdMotivo2 = 3 and Traspaso = AccountsTotales._id and AccountsTotales.IdMoneda = ? and strftime('%Y',Fecha) = ? Group BY IdMotivo ) as table3, AccountsMotivo
        WHERE table3.IdMotivo2 = AccountsMotivo._id GROUP BY IdMotivo2
    ) as table2 on table1.IdMotivo = table2.IdMotivo2 ,  AccountsMotivo WHERE table1.IdMotivo = AccountsMotivo._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo
                            union
                            SELECT
                                AccountsTrips._id as _id, SUM(Gasto) as Gasto, Ingreso , AccountsTrips.Nombre as Motivo, (COALESCE(Ingreso,0) - COALESCE(SUM(Gasto),0)) as count1, (1) as isViaje
                                FROM(
                                    SELECT
                                        sum(Cantidad ) as Gasto, IdViaje
                                        FROM AccountsMovimiento
                                        WHERE  IdMoneda = ? \(tmpY) \(tmpM) and Cantidad < 0 GROUP BY IdViaje
                                    union
                                    SELECT
                                        SUM( CASE WHEN (
                                            SELECT
                                                AccountsTotales.IdMoneda
                                                FROM AccountsTotales, AccountsMovimiento
                                                WHERE AccountsTotales._id = IdTotales and Cambio > 0) = ?
                                        then Cantidad * Cambio end) as Gasto, IdViaje
                                        FROM AccountsMovimiento
                                        WHERE Cantidad < 0 \(tmpY) \(tmpM) and Cambio <> 1 GROUP BY IdViaje
                                    ) as table1
                            LEFT OUTER JOIN (
                            SELECT
                                SUM(Ingreso) as Ingreso, IdViaje
                                FROM (
                                    SELECT
                                        sum(Cantidad ) as Ingreso, IdViaje
                                        FROM AccountsMovimiento
                                        WHERE Cantidad > 0 and IdMoneda = ? \(tmpY) \(tmpM)
                                        Group BY IdViaje
                                    union
                                    SELECT SUM( CASE WHEN (
                                        SELECT AccountsTotales.idMoneda
                                        FROM AccountsTotales, AccountsMovimiento
                                        WHERE AccountsTotales._id = IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda
                                    then Cantidad * -1 end) as Ingreso, IdViaje
                                    FROM AccountsMovimiento
                                    WHERE Cantidad < 0 and IdMoneda = ? \(tmpY) \(tmpM) and Cambio IS NOT NULL Group BY IdViaje
                                    ) as table3, AccountsTrips
                                WHERE table3.IdViaje = AccountsTrips._id GROUP BY IdViaje
                            ) as table2 on table1.IdViaje = table2.IdViaje ,  AccountsTrips
                            WHERE table1.IdViaje = AccountsTrips._id and (Gasto IS NOT NULL or Ingreso IS NOT NULL) GROUP BY Motivo ORDER BY count1 DESC
"""
    do {
        let stmt = try Database.db.prepare(query, [idMoneda, idMoneda, idMoneda, idMoneda, idMoneda, idMoneda, idMoneda, idMoneda, idMoneda, idMoneda, idMoneda])
        
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return []
    }
}

func getReportesGastoMotives(idMoneda:Int64, year:String?, month:String?) -> [[String:Any?]]{
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and strftime('%Y',Fecha) == strftime('%Y', date('now'))"
        tmpM = "and strftime('%m',Fecha) == strftime('%m', date('now'))"
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
    SELECT
        AccountsMotivo._id as _id, SUM(Gasto) as Gasto,  AccountsMotivo.Motivo as Motivo
        FROM(
            SELECT
                sum(Cantidad ) as Gasto, IdMotivo
                FROM AccountsMovimiento
                WHERE  IdMoneda = ? \(tmpY) \(tmpM) and Cantidad < 0
            GROUP BY IdMotivo
            union
            SELECT
                SUM( CASE WHEN (
                    SELECT
                        AccountsTotales.idMoneda
                        FROM AccountsTotales, AccountsMovimiento
                        WHERE AccountsTotales._id = IdTotales and Cambio > 0) = ?
                    then Cantidad * Cambio end) as Gasto,
                    IdMotivo
                FROM AccountsMovimiento
                WHERE Cantidad < 0 \(tmpY) \(tmpM) and Cambio <> 1
            GROUP BY IdMotivo
            union
            SELECT
                SUM (CASE WHEN idMotivo = 3 and (
                    SELECT
                        AccountsTotales.idMoneda
                        FROM AccountsTotales, AccountsMovimiento
                        WHERE AccountsTotales._id = IdTotales) = ? THEN
                        Cantidad * Cambio * -1 end) as Gasto,
                    IdMotivo
                    FROM AccountsMovimiento
                    WHERE Fecha is not null \(tmpY) \(tmpM)
        GROUP BY IdMotivo) as table1, AccountsMotivo
        WHERE AccountsMotivo._id = table1.IdMotivo and Gasto is not null
    GROUP BY IdMotivo ORDER BY Gasto
"""
    do {
        let stmt = try Database.db.prepare(query, [idMoneda, idMoneda, idMoneda])
        
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return []
    }
}

func getReportesIngresoMotives(idMoneda:Int64, year:String?, month:String?) -> [[String:Any?]]{
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and strftime('%Y',Fecha) == strftime('%Y', date('now'))"
        tmpM = "and strftime('%m',Fecha) == strftime('%m', date('now'))"
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
    SELECT
            AccountsMotivo._id as _id, Ingreso , AccountsMotivo.Motivo as Motivo
            FROM(
                SELECT
                    SUM(Ingreso) as Ingreso, IdMotivo
                    FROM (
                        SELECT
                            sum(Cantidad ) as Ingreso, IdMotivo as IdMotivo
                            FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda = ? \(tmpY) \(tmpM)
                        Group BY IdMotivo
                        union
                        SELECT
                            SUM( CASE WHEN (
                                SELECT
                                AccountsTotales.idMoneda
                                    FROM AccountsTotales, AccountsMovimiento
                                    WHERE AccountsTotales._id = IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda
                                then Cantidad * -1 end) as Ingreso, IdMotivo as IdMotivo2
                                    FROM AccountsMovimiento
                                    WHERE Cantidad < 0 \(tmpY) \(tmpM) and IdMoneda = ? and Cambio IS NOT NULL
                                Group BY IdMotivo
                        union
                        SELECT
                            SUM(Cantidad) as Ingreso, IdMotivo as IdMotivo2
                                From AccountsTotales, AccountsMovimiento
                                WHERE IdMotivo = 3 and Traspaso = AccountsTotales._id and AccountsTotales.IdMoneda = ? \(tmpY) \(tmpM) Group BY IdMotivo ) as table3, AccountsMotivo
            WHERE table3.IdMotivo = AccountsMotivo._id GROUP BY IdMotivo
        ) as table1,  AccountsMotivo WHERE table1.IdMotivo = AccountsMotivo._id and (Ingreso IS NOT NULL)
    GROUP BY Motivo ORDER BY Ingreso desc

"""
    do {
        let stmt = try Database.db.prepare(query, [idMoneda, idMoneda, idMoneda])
        
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return []
    }
}


func getReportesTotales(year:String?, month:String?) -> [[String:Any?]]{
    var tmpY = ""
    var tmpM = ""
    if year == nil {
        tmpY = "and strftime('%Y',Fecha) == strftime('%Y', date('now'))"
        tmpM = "and strftime('%m',Fecha) == strftime('%m', date('now'))"
    } else if month == nil {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
    } else {
        tmpY = "and strftime('%Y',Fecha) == '\(year!)'"
        tmpM = "and strftime('%m',Fecha) == '\(month!)'"
    }
    let query = """
    select AccountsTotales._id, AccountsTotales.Cuenta as Motivo, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso
        from(
            SELECT t._id as idTotales, sum(Gasto) as Gasto
                from(
                    Select _id
                        from(
                            select AccountsTotales.*, AccountsMovimiento.*
                                from AccountsTotales
                                LEFT JOIN AccountsMovimiento on AccountsTotales._id = AccountsMovimiento.IdTotales
                                WHERE Fecha is not null \(tmpY) \(tmpM)
                            Group by idTotales
                            Union all
                            Select AccountsTotales.* , AccountsMovimiento.*
                                from AccountsMovimiento
                                LEFT JOIN AccountsTotales On AccountsTotales._id = AccountsMovimiento.IdTotales
                                Where AccountsTotales.Activa
                            Group by idTotales
                        )
                    group by _id
                ) as t
        Left outer join (
            SELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales
                from AccountsMovimiento
                WHERE Cantidad < 0 \(tmpY) \(tmpM)
            Group by idTotales
            union
            select (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales
                from AccountsMovimiento
                WHERE Traspaso is not null \(tmpY) \(tmpM)
            Group by idTotales
        ) as table3 on t._id == table3.idTotales Group by t._id
        ) as table1
        LEFT OUTER JOIN (
            select idTotales, sum(Ingreso) as Ingreso
                from(
                    select idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso
                        from AccountsMovimiento
                        WHERE Cantidad > 0 and Traspaso is null \(tmpY) \(tmpM)
                    Group by idTotales
                    union
                    select  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso
                        from AccountsMovimiento
                        WHERE Traspaso is not null \(tmpY) \(tmpM)
                    group by Traspaso
                ) Group by idTotales
        ) as table2 on table1.idTotales == table2.idTotales, AccountsMovimiento, AccountsTotales
        WHERE (AccountsTotales._id == table1.idTotales or AccountsTotales._id == table2.idTotales) and AccountsTotales._id > 20
    group by AccountsTotales._id
"""
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return []
    }
}

func getTotalsHistory(daily: Bool, diference:Int, now:String) -> [[String:Any?]] {
    var groupBy = "GROUP BY t.Tipo, y, mo, t.IdMoneda, dd"
    var groupBy2 = "GROUP BY t.Tipo, y, mo, dd"
    var groupBy4 = "GROUP BY tc._id, y, mo, s.IdMoneda, dd"
    var groupBy5 = "Group BY t._id, mo, dd"
    var group3 = "GROUP BY y, mo, dd, IdMoneda"
    var orderBy = "order by y desc, mo desc, dd desc"
    var date = "date('\(now)', 'start of month', '\(diference) month') and date('\(now)')"
    if !daily {
        groupBy = "GROUP BY t.Tipo, y, mo, t.IdMoneda"
        groupBy2 = "GROUP BY t.Tipo, y, mo"
        group3 = "GROUP BY y, mo, IdMoneda"
        date = "date('\(now)', 'start of year', '\(diference) year') and date('\(now)')"
        orderBy = "order by y desc, mo desc"
        groupBy4 = "GROUP BY tc._id, y, mo, s.IdMoneda"
        groupBy5 = "Group BY t._id, mo"
    }
    
    let query = """
            SELECT (tc.Tipo || ' ' || m.Moneda) as Cuenta, 0 as CurrentCantidad, SUM(Cantidad) as Cantidad, (tc._id ||m.Moneda) as _id, y, mo, dd FROM (
            select (coalesce(SUM(m.Cantidad * m.Cambio), 0) * -1) as Cantidad, t.Tipo as _id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, t.IdMoneda, (strftime('%d',Fecha)) as dd, 'A' as Zone
            from AccountsTotales as t, AccountsMovimiento as m
            WHERE m.IdTotales = t._id and Traspaso is null and IdTotales > 15
            and m.Fecha BETWEEN \(date)
            \(groupBy)
            UNION
            select SUM(CASE WHEN m.IdMotivo = 2
            then m.Cantidad * -1
            else m.Cantidad * m.Cambio * -1 END) as Cantidad, t.Tipo as _id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, t.IdMoneda, (strftime('%d',Fecha)) as dd, 'B' as Zone
            from AccountsTotales as t, AccountsMovimiento as m
            WHERE m.Traspaso = t._id and Traspaso > 15 and m.Fecha BETWEEN \(date)
            \(groupBy)
            UNION
            select (coalesce(SUM(m.Cantidad * m.Cambio), 0)) as Cantidad, t.Tipo as _id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, t.IdMoneda, (strftime('%d',Fecha)) as dd, 'C' as Zone
            from AccountsTotales as t, AccountsMovimiento as m
            WHERE m.IdTotales = t._id and Traspaso is not null and m.IdTotales > 15 and m.Fecha BETWEEN \(date)
            \(groupBy)
            UNION
            select (coalesce(SUM(p.Cantidad * p.Cambio), 0)) as Cantidad, t.Tipo as _id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, t.IdMoneda, (strftime('%d',Fecha)) as dd, 'D' as Zone
            from AccountsPrestamos as p, AccountsTotales as t
            WHERE p.IdTotales = t._id and IdMovimiento = 0 and p.IdTotales > 15 and p.Fecha BETWEEN \(date)
            \(groupBy)
            UNION
            select (coalesce(SUM(p.Cantidad * p.Cambio), 0) * -1) as Cantidad, t.Tipo as _id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, t.IdMoneda, (strftime('%d',Fecha)) as dd, 'E' as Zone
            from AccountsPrestamosDetalle as p, AccountsTotales as t
            WHERE p.IdTotales = t._id and p.IdTotales > 15 and p.Fecha BETWEEN \(date)
            \(groupBy)
            union
            SELECT SUM(Cantidad) as Cantidad, 5 as _id, y, mo, IdMoneda, dd, 'F' as Zone FROM(
                SELECT (SUM( p.Cantidad * Cambio) * -1) as Cantidad, 5 as _id, (strftime('%Y', Fecha)) as y, (strftime('%m',Fecha)) as mo, p.IdMoneda, (strftime('%d',Fecha)) as dd, 'F' as Zone
                    FROM AccountsPrestamos as p
                    where Fecha is not null and p.Fecha BETWEEN \(date)
                \(group3)
                union
                SELECT SUM( p.Cantidad * Cambio) as Cantidad, 5 as _id, (strftime('%Y', Fecha)) as y, (strftime('%m',Fecha)) as mo, p.IdMoneda, (strftime('%d',Fecha)) as dd, 'H' as Zone
                    FROM AccountsPrestamosDetalle as p
                    where Fecha is not null and p.Fecha BETWEEN \(date)
                \(group3)
            ) \(group3)
            )as s, AccountsTiposCuentas as tc, AccountsMoneda as m WHERE s._id = tc._id and m._id = s.IdMoneda and s.IdMoneda > 0
            \(groupBy4)
            UNION
            SELECT t.Cuenta, t.CurrentCantidad, SUM(Cantidad) as Cantidad, t._id, y, mo, dd FROM (
            select (coalesce(SUM(m.Cantidad * m.Cambio), 0) * -1) as Cantidad, t._id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, (strftime('%d',Fecha)) as dd, 'A' as Zone
            from AccountsTotales as t, AccountsMovimiento as m
            WHERE m.IdTotales = t._id and Traspaso is null and m.Fecha BETWEEN \(date)
            \(groupBy5)
            UNION
            select SUM(CASE WHEN m.IdMotivo = 2
            then m.Cantidad * -1
            else m.Cantidad * m.Cambio * -1 END) as Cantidad, t._id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, (strftime('%d',Fecha)) as dd, 'B' as Zone
            from AccountsTotales as t, AccountsMovimiento as m
            WHERE m.Traspaso = t._id and m.Fecha BETWEEN \(date)
            \(groupBy5)
            UNION
            select (coalesce(SUM(m.Cantidad * m.Cambio), 0)) as Cantidad, t._id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, (strftime('%d',Fecha)) as dd, 'C' as Zone
            from AccountsTotales as t, AccountsMovimiento as m
            WHERE m.IdTotales = t._id and Traspaso is not null and m.Fecha BETWEEN \(date)
            \(groupBy5)
            UNION
            select (coalesce(SUM(p.Cantidad * p.Cambio), 0)) as Cantidad, t._id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, (strftime('%d',Fecha)) as dd, 'D' as Zone
            from AccountsPrestamos as p, AccountsTotales as t
            WHERE p.IdTotales = t._id and IdMovimiento = 0 and p.Fecha BETWEEN \(date)
            \(groupBy5)
            UNION
            select (coalesce(SUM(p.Cantidad * p.Cambio), 0) * -1) as Cantidad, t._id, (strftime('%Y',Fecha)) as y, (strftime('%m',Fecha)) as mo, (strftime('%d',Fecha)) as dd, 'E' as Zone
            from AccountsPrestamosDetalle as p, AccountsTotales as t
            WHERE p.IdTotales = t._id and p.Fecha BETWEEN \(date)
            \(groupBy5)
            )as s, AccountsTotales t WHERE s._id = t._id
            \(groupBy5)
            \(orderBy)
"""
    print(query)
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getTotalsHistory: ", error)
        return []
    }
    
    return []
}

func getTotalesLineGraph() -> [[String:Any?]]{
    let query = """
        SELECT t1._id, t1.Moneda, t1.Cuenta, t2.Count, t1.CurrentCantidad, t1.Activa FROM(
        SELECT (tc._id || m.Moneda) as _id, m.Moneda, (tc.Tipo || ' ' || m.Moneda) as Cuenta, 0 as Count, SUM(t.CurrentCantidad) as CurrentCantidad, 2 as Activa, m._id as IdMoneda
                        FROM AccountsTotales as t, AccountsMoneda as m, AccountsTiposCuentas as tc
                        WHERE t.IdMoneda = m._id and t.Tipo = tc._id and t.Activa = 1
                        GROUP BY tc._id, m._id
        UNION
        Select (5||m.Moneda) as _id, m.Moneda as Moneda, ('Prestamos '||m.Moneda), 0 as Count, SUM((p.Cantidad - coalesce(t1.Cantidad, 0)) ) as CurrentCantidad, 1 as Activa, IdMoneda
            FRom AccountsPrestamos as p
        left join (
            SELECT SUM(Cantidad * Cambio) as Cantidad, IdPrestamo
                From AccountsPrestamosDetalle
            group by IdPrestamo) as t1 on p._id = t1.IdPrestamo, AccountsMoneda as m
            WHERE m._id = IdMoneda
        group by IdMoneda
        ) as t1
        LEFT JOIN(
        SELECT max(Fecha) as Fecha, IdMoneda, count(IdMoneda) as Count FROM AccountsMovimiento
        WHERE Fecha BETWEEN date('now', '-1 month') and date('now') GROUP BY IdMoneda
        ) as t2 on t1.IdMoneda = t2.IdMoneda
                        union
        SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count ,
        AccountsTotales.CurrentCantidad, 0 as Activa FROM AccountsTotales, AccountsMoneda LEFT JOIN
        AccountsMovimiento on AccountsMovimiento.IdTotales= AccountsTotales._id and Fecha BETWEEN date('now', '-1 month') and date('now')
        WHERE Activa = 1 and AccountsTotales.IdMoneda == AccountsMoneda._id and AccountsTotales._id > 20 GROUP BY AccountsTotales._id
        ORDER by activa desc, Count DESC
"""
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getTotalesLineGraph: ", error)
    }
    return []
}
//-----------------------------End Reposrtes------------------------------------
//-----------------------------CambioMoneda------------------
func getCambioMoneda(idMon1:Int64, idMon2:Int64) -> NSNumber {
    let q = """
            SELECT * From AccountsCambioMoneda where IdMoneda1 = ? and IdMoneda2 = ?
"""
    do {
        let stmt = try Database.db.prepare(q, [idMon1, idMon2])
        let cambio = stmt.next()![0] as? NSNumber
        return cambio ?? 0.0
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return 0.0
    }
}
//-------------------------------CambioMonedaEnd-------------------
//MARK:TRIPS

func getTripsId(id:Int64) -> [[String:Any?]] {
    let query = """
        SELECT * FROM AccountsTrips WHERE _id = ?
"""
   do {
        let stmt = try Database.db.prepare(query, [id])
    return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getTrips: ", error)
        return []
    }
}

func getTrips() -> [[String:Any?]] {
    let query = """
    SELECT t3.* FROM (
    SELECT t1._id as _id, t1.Nombre, t1.Descripcion, t1.FechaCreacion, t1.FechaCierre, t1.FechaInicio, t1.FechaFin, CASE WHEN t2.Total is null then 0 else t2.Total END Total, t1.IdMoneda, t1.Moneda FROM (
    SELECT t._id, t.Nombre, t.Descripcion, t.FechaCreacion, t.FechaCierre, t.FechaInicio, t.FechaFin, t.IdMoneda, m.Moneda  FROM AccountsTrips as t, AccountsMoneda as m where t.IdMoneda = m._id
    ) as t1 LEFT JOIN (
    SELECT t._id, t.Nombre, t.Descripcion, t.FechaCreacion, t.FechaCierre, t.FechaInicio, t.FechaFin, sum(m.Cantidad) as Total, t.IdMoneda
    FROM AccountsTrips as t, AccountsMovimiento as m
    Where t._id = m.IdViaje
    GROUP by t._id
    order by FechaCreacion DESC
    ) as t2 on t1._id = t2._id order by t1.FechaInicio desc
    ) as t3
    LEFT JOIN (
    SELECT t1._id as _id, t1.Nombre, t1.Descripcion, t1.FechaCreacion, t1.FechaCierre, t1.FechaInicio, t1.FechaFin, CASE WHEN t2.Total is null then 0 else t2.Total END Total, t1.IdMoneda, 1 as Activa FROM (
    SELECT t._id, t.Nombre, t.Descripcion, t.FechaCreacion, t.FechaCierre, t.FechaInicio, t.FechaFin, t.IdMoneda  FROM AccountsTrips as t where date('now') BETWEEN FechaInicio and FechaFin
    ) as t1 LEFT JOIN (
    SELECT t._id, t.Nombre, t.Descripcion, t.FechaCreacion, t.FechaCierre, t.FechaInicio, t.FechaFin, sum(m.Cantidad) as Total, t.IdMoneda
    FROM AccountsTrips as t, AccountsMovimiento as m
    Where t._id = m.IdViaje and date('now') BETWEEN FechaInicio and FechaFin
    GROUP by t._id
    order by FechaCreacion DESC
    ) as t2 on t1._id = t2._id order by t1.FechaInicio desc
    ) as t4 on t3._id = t4._id order by t4.Activa desc
"""
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getTrips: ", error)
        return []
    }
}
//--------------------------------Trips End------------------------
/*
func getTotalesCuentasByMonth(year:String, month:String) -> [[String:Any?]]{
    let query = """
select AccountsTotales._id, AccountsTotales.Cuenta as Motivo, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso
from(
SELECT t._id as idTotales, sum(Gasto) as Gasto
from(
Select _id
from(
select AccountsTotales.*, AccountsMovimiento.*
from AccountsTotales
LEFT JOIN AccountsMovimiento
on AccountsTotales._id = AccountsMovimiento.IdTotales
WHERE ((strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?))
Group by idTotales
Union all
Select AccountsTotales.* , AccountsMovimiento.*
from AccountsMovimiento
LEFT JOIN AccountsTotales
On AccountsTotales._id = AccountsMovimiento.IdTotales Where AccountsTotales.Activa
Group by idTotales
) group by _id
) as t
Left outer join (
SELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales
from AccountsMovimiento
WHERE Cantidad < 0 and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?
Group by idTotales
union
select (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales
from AccountsMovimiento
WHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?
Group by idTotales
) as table3 on t._id == table3.idTotales Group by t._id
) as table1
LEFT OUTER JOIN (
select idTotales, sum(Ingreso) as Ingreso
from(
select idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso
from AccountsMovimiento
WHERE Cantidad > 0 and Traspaso is null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?
Group by idTotales
union
select  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso
from AccountsMovimiento
WHERE Traspaso is not null and strftime('%Y',Fecha) == ? and strftime('%m',Fecha) == ?
group by Traspaso
) Group by idTotales
) as table2 on table1.idTotales == table2.idTotales, AccountsMovimiento, AccountsTotales
WHERE (AccountsTotales._id == table1.idTotales or AccountsTotales._id == table2.idTotales)
group by AccountsTotales._id
"""
    do {
        let stmt = try Database.db.prepare(query, [year, month, year, month, year, month, year, month, year, month])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return []
    }
}

func getTotalesCuentasByYear(year:String) -> [[String:Any?]]{
    let query = """
select AccountsTotales._id, AccountsTotales.Cuenta as Motivo, COALESCE(Gasto,0) as Gasto, COALESCE(Ingreso,0) as Ingreso
from(
SELECT t._id as idTotales, sum(Gasto) as Gasto
from(
Select _id
from(
select AccountsTotales.*, AccountsMovimiento.*
from AccountsTotales
LEFT JOIN AccountsMovimiento
on AccountsTotales._id = AccountsMovimiento.IdTotales
WHERE ((strftime('%Y',Fecha) == ?))
Group by idTotales
Union all
Select AccountsTotales.* , AccountsMovimiento.*
from AccountsMovimiento
LEFT JOIN AccountsTotales
On AccountsTotales._id = AccountsMovimiento.IdTotales Where AccountsTotales.Activa
Group by idTotales
) group by _id
) as t
Left outer join (
SELECT (sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end )) as Gasto, idTotales
from AccountsMovimiento
WHERE Cantidad < 0 and strftime('%Y',Fecha) == ?
Group by idTotales
union
select (-1*sum(CASE WHEN Cambio is not null and IdMotivo == 2 then cantidad*Cambio else cantidad end )) as Gasto, idTotales
from AccountsMovimiento
WHERE Traspaso is not null and strftime('%Y',Fecha) == ?
Group by idTotales
) as table3 on t._id == table3.idTotales Group by t._id
) as table1
LEFT OUTER JOIN (
select idTotales, sum(Ingreso) as Ingreso
from(
select idTotales, sum(CASE WHEN Cambio is not null then cantidad*Cambio else cantidad end ) as Ingreso
from AccountsMovimiento
WHERE Cantidad > 0 and Traspaso is null and strftime('%Y',Fecha) == ?
Group by idTotales
union
select  Traspaso as idTotales, sum(CASE WHEN Cambio is not null and IdMotivo <> 2 then cantidad*Cambio else cantidad end ) as Ingreso
from AccountsMovimiento
WHERE Traspaso is not null and strftime('%Y',Fecha) == ?
group by Traspaso
) Group by idTotales
) as table2 on table1.idTotales == table2.idTotales, AccountsMovimiento, AccountsTotales
WHERE (AccountsTotales._id == table1.idTotales or AccountsTotales._id == table2.idTotales)
group by AccountsTotales._id
"""
    do {
        let stmt = try Database.db.prepare(query, [year, year, year, year, year])
        
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error getSumByMotivesMonthly: ", error)
        return []
    }
}
 */
//MARK: Config
func getOnlyWifi() -> Bool {
    let query = """
    select \(Config.ValueCode) From AccountsConfig where _id = 3
"""
    do {
        let stmt = try Database.db.prepare(query)
        let n = stmt.next()
        if n == nil {
            return true
        }
        let val = n![0] as! String
        return val == "1"
    } catch {
        print("Error getOnlyWifi: ", error)
    }
    return true
}

func getFirstDate() -> String?{
    let q = "SELECT Fecha FROM AccountsMovimiento order by Fecha asc limit 1"

    do {
        let stmt = try Database.db.prepare(q)
        let n = stmt.next()
        if n == nil {
            return nil
        }
        let stringDate = n![0] as! String
        return stringDate
    } catch {
        print("Error getFirstDate: ", error)
    }
    
    return nil
}
func getLastSync() -> String?{
    let query = "SELECT " + Config.ValueCode + " FROM " + Config.Table + " WHERE _id = ?"
    do{
        let stmt = try Database.db.prepare(query, [Config.LastSync])
        let next = stmt.next()
        if next == nil {
            return nil
        }
        let lastSync = next![0] as? String
        return lastSync
    } catch {
        return nil
    }
}

func getAllTables() -> Array<String> {
    let query = "SELECT name FROM sqlite_master WHERE type = 'table' and name like 'Accounts%'"
    var array:Array<String> = []
    do {
        let stmt = try Database.db.run(query)
        for row in stmt {
            array.append(row[0] as! String)
        }
        return array
    } catch {
        print("getAllTables error: ", error)
        return []
    }
}
func getTablesJSONasString(name: String) -> String {
    let query = "SELECT * FROM \(name)"
    do{
        let stmt = try Database.db.prepare(query)
        let columns = stmt.columnNames
        var result = "[ "
        for row in stmt {
            var x = 0
            result += "{ "
            while x < columns.count{
                if let r = row[x] {
                    result = result + "\"" + columns[x] + "\"" + ": \"\(r)\", "
                }
                x = x + 1
            }
            result.removeLast(2)
            result += " }, "
        }
        result.removeLast(2)
        if(result.count < 2){
            return "[]"
        }
        
        result += " ] "
        return result
    } catch {
        return "[]"
    }
}
