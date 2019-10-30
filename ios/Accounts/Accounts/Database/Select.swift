//
//  Select.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/17/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation

//-------------------------------Totales----------------------------

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

//---------------------------------Movimiento--------------------

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
            FROM AccountsMovimiento as m, AccountsTotales as t
            WHERE t._id = m.IdTotales \(tmpY) \(tmpM) and (IdTotales = ? or Traspaso = ?)
        Group by m.Fecha ORDER BY Fecha DESC, m._id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [id, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getMovimientosFecha 0", error)
    }
    return []
}
func getMotivosMovFecha(id: Int64, year:String?, month:String?) -> [[String:Any?]] {
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
            WHERE mot._id = m.IdMotivo \(tmpY) \(tmpM) and IdMotivo = ?
        Group by m.Fecha ORDER BY Fecha DESC, m._id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch{
        print("Error select getMovimientosFecha 0", error)
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
                SELECT mov.*, mot.Motivo, mon.Moneda, t.Cuenta
                    FROM AccountsMovimiento as mov, AccountsTotales as t, AccountsMotivo as mot, AccountsMoneda as mon
                    WHERE mov.IdTotales = t._id and mov.IdMotivo = mot._id and mov.IdMoneda = mon._id and Fecha = ? and (IdTotales = ? or Traspaso = ?) ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [date, id, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getMovimientosByDate ", error)
    }
    return []
}

func getMotivosMovimientosByDate(id: Int64, date:String) -> [[String:Any?]]{
    let query = """
                SELECT mov.*, mot.Motivo, mon.Moneda, t.Cuenta
                    FROM AccountsMovimiento as mov, AccountsTotales as t, AccountsMotivo as mot, AccountsMoneda as mon
                    WHERE mov.IdTotales = t._id and mov.IdMotivo = mot._id and mov.IdMoneda = mon._id and Fecha = ? and IdMotivo = ? ORDER BY Fecha DESC, _id DESC
"""
    do{
        let stmt = try Database.db.prepare(query, [date, id])
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error Select getMovimientosByDate ", error)
    }
    return []
}
///------------------------------------END MOVIMIENTOS----------------------------------------

//Monedas

func getMonedas() -> [[String:Any?]]{
    let query = """
            SELECT mon._id, mon.Moneda, Cuenta FROM (
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

//Motivos
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

func getMotives() -> [[String:Any?]] {
    let query = """
                SELECT mot._id, mot.Motivo, Cuenta, Fecha FROM (
                    SELECT * FROM AccountsMotivo WHERE _id > 15 and Active = 1
                ) as mot
                left join(
                    SELECT max(Fecha) as Fecha, IdMotivo, count(IdMotivo) as Cuenta FROM AccountsMovimiento
                        WHERE Fecha BETWEEN date('now', '-1 month') and date('now') GROUP BY IdMotivo
                ) as mov on mot._id = mov.IdMotivo
                GROUP BY mot._id, mot.Motivo Order By Cuenta desc, Fecha desc
"""
    
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return []
}

//Reportes

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
// Config

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
