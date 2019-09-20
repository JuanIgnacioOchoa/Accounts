//
//  Select.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/17/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation

//Totales
func getTotales(inactivos: Bool) -> [[String:Any?]]?{
    var activa = "";
    if(!inactivos){
        activa = "Activa == 1 and ";
    }
    let query = "SELECT AccountsTotales._id, AccountsMoneda.Moneda, AccountsTotales.Cuenta, COUNT(AccountsTotales.Cuenta) as Count , " +
        "AccountsTotales.CurrentCantidad, AccountsTotales.Activa FROM AccountsTotales, AccountsMoneda LEFT JOIN " +
        "AccountsMovimiento on AccountsMovimiento.IdTotales= AccountsTotales._id and date('now','-1 month') <= date('now') " +
        "WHERE \(activa) AccountsTotales.IdMoneda == AccountsMoneda._id and AccountsTotales._id > 20 GROUP BY AccountsTotales._id " +
        "ORDER by activa desc, Count DESC"
    
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return nil
}

//Movimiento

func getMovimientos() -> [[String:Any?]]?{
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
    return nil
}

//Monedas

func getMonedas() -> [[String:Any?]]?{
    let query = """
            SELECT AccountsMoneda._id, AccountsMoneda.Moneda, COUNT(AccountsMoneda.Moneda) as Cuenta FROM AccountsMoneda LEFT JOIN
                AccountsMovimiento on AccountsMovimiento.IdMoneda = AccountsMoneda._id and date('now','-1 month') <= date('now') WHERE Active == 1
                GROUP BY AccountsMoneda._id ORDER by Fecha DESC, Cuenta DESC
"""
    do {
        let stmt = try Database.db.prepare(query)
        return Database.stmtToDictionary(stmt: stmt)
    } catch {
        print("Error select: ", error)
    }
    return nil
}

//Reportes

func getGastoTotalByMonedaFromCurrentMonth(moneda:Int) -> Double{
    let query = """
            SELECT SUM(Gasto) as Gasto FROM(
                SELECT sum(Cantidad ) as Gasto FROM AccountsMovimiento WHERE Cantidad < 0 and IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now'))and
                strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))
                union
                SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) == ?
                then Cantidad * Cambio end) FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha)
                 == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now')) and Cambio <> 1
                union
                SELECT SUM (CASE WHEN idMotivo == 3 and (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales) == ? THEN
                 Cantidad * Cambio * -1 end) FROM AccountsMovimiento WHERE strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha) == strftime('%m',date('now'))
                 and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now')))
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

func getIngresoTotalByMonedaFromCurrentMonth(moneda:Int) -> Double{
    let query = """
                SELECT SUM(Ingreso) as Ingreso FROM (
                  SELECT sum(Cantidad ) as Ingreso FROM AccountsMovimiento WHERE Cantidad > 0 and IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now'))and
                  strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))
                union
                
                SELECT SUM( CASE WHEN (SELECT AccountsTotales.idMoneda FROM AccountsTotales, AccountsMovimiento WHERE AccountsTotales._id == IdTotales and Cambio > 0) <> AccountsMovimiento.IdMoneda
                                then Cantidad * -1 end) as Ingreso FROM AccountsMovimiento WHERE Cantidad < 0 and strftime('%Y',Fecha) == strftime('%Y', date('now'))
                and strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))
                and IdMoneda == ? and Cambio IS NOT NULL and Cambio <> 1
                union
                SELECT  SUM(Cantidad) as Ingreso From AccountsTotales, AccountsMovimiento WHERE IdMotivo == 3 and Traspaso == AccountsTotales._id and AccountsTotales.IdMoneda == ? and strftime('%Y',Fecha) == strftime('%Y', date('now')) and strftime('%m',Fecha) == strftime('%m',date('now')) and strftime('%d',Fecha) >= 1 and strftime('%d',Fecha) <= strftime('%d',date('now'))
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

func getTotalesByMonth(month: String, year: String) -> [[String:Any?]]?{
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
    return nil
}

// Config

func getLastSync() -> String?{
    let query = "SELECT " + Config.ValueCode + " FROM " + Config.Table + " WHERE _id = ?"
    do{
        let stmt = try Database.db.prepare(query, [Config.LastSync])
        let lastSync = stmt.next()![0] as? String
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
