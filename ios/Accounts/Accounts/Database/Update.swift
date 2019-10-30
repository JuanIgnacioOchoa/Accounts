//
//  Update.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/24/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation

// Totales

func newMoveCuenta(cantidad:Double, idCuenta:Int64) -> Bool{
    //updateLastSync()
    let total = getTotal(id: idCuenta)
    let cantidadOld = total[Totales.CurrentCantidad] as! Double
    let query = """
    UPDATE \(Totales.Table) SET \(Totales.CurrentCantidad) = \(cantidadOld + cantidad) WHERE _id = ?
"""
    print("Query new move Cuenta: ", query)
    do {
        try Database.db.run(query, [idCuenta])
        return true
    } catch {
        print("Error NewMoveCuenta: ", error)
    }
    return false
}

func actualizarCuentaMove(cantidad:Double, idCuenta:Int64, idMove:Int64) -> Bool {
    let query1 = """
                UPDATE AccountsTotales set CurrentCantidad = (select (t.CurrentCantidad - m.Cantidad) as Cantidad from AccountsMovimiento as m, AccountsTotales as t where m.IdTotales = t._id and m._id = ?) WHERE _id = (SELECT IdTotales FROM AccountsMovimiento WHERE _id = ?)
                """
    let query2 = """
                UPDATE \(Totales.Table) set \(Totales.CurrentCantidad) = \(Totales.CurrentCantidad) + ? WHERE _id = ?
                """
    do{
        try Database.db.run(query1, [idMove])
        try Database.db.run(query2, [cantidad, idCuenta])
        return true
    } catch {
        print("Error actializarCuentaMove: ", error)
    }
    return false
}
//Movimiento

func actualizarMovimiento(id:Int64, cantidad:Double, idTotales:Int64, comment:String, idMotivo:Int64, idMoneda:Int64, cambio:Double, date:Date) -> Bool{
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    let query = """
            UPDATE \(Movimiento.Table) SET \(Movimiento.Cantidad) = ?, \(Movimiento.IdTotales) = ?, \(Movimiento.Comment) = ?, \(Movimiento.IdMotivo) = ?, \(Movimiento.IdMoneda) = ?, \(Movimiento.Cambio) = ?, \(Movimiento.Fecha) = ?
            WHERE _id = ?
    """
    do{
        try Database.db.run(query, [cantidad, idTotales, comment, idMotivo, idMoneda, cambio, dateFormatter.string(from: date), id])
        _ = newMoveCuenta(cantidad: cantidad, idCuenta: idTotales)
        return true
    } catch {
        print("Error actualizar Movimiento: ", error)
    }
    return false
}

func actualizarTraspaso(id: Int64, cantidad:Double, idFrom:Int64, idTo: Int64, comment:String?, idMot:Int64, cambio: Double, date:Date) -> Bool {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var sCom = ""
    if comment != nil {
        sCom = comment!
    }
    let query = """
        UPDATE \(Movimiento.Table) SET \(Movimiento.Cantidad) = ?, \(Movimiento.IdTotales) = ?, \(Movimiento.IdMotivo) = ?, \(Movimiento.Traspaso) = ?, \(Movimiento.Cambio) = ?, \(Movimiento.Fecha) = ?, \(Movimiento.Comment) = ?
                   WHERE _id = ?
       """
    
    do {
        try Database.db.run(query, [cantidad, idFrom, idMot, idTo, cambio, dateFormatter.string(from: date), sCom, id])
        if(idMot == 1) {
            let _ = newMoveCuenta(cantidad: cantidad * -1, idCuenta: idFrom);
            let _ = newMoveCuenta(cantidad: cantidad * cambio, idCuenta: idTo);
        } else {
            let _ = newMoveCuenta(cantidad: cantidad, idCuenta: idTo);
            let _ = newMoveCuenta(cantidad: cantidad * cambio * -1, idCuenta: idFrom);
        }
        return true
    } catch {
        print("Error actualizar Movimiento: ", error)
    }
    
    return false
}

//CambioMoneda

func actualizarTipoCambio(moneda1:Int64, moneda2:Int64, cambio:Double) {
    //updateLastSync()
    let query1 = """
            UPDATE \(CambioMoneda.Table) SET \(CambioMoneda.TipoCambio) = \(cambio) WHERE \(CambioMoneda.IdMoneda1) = ? and \(CambioMoneda.IdMoneda2) = ?
        """
    let query2 = """
            UPDATE \(CambioMoneda.Table) SET \(CambioMoneda.TipoCambio) = \(1/cambio) WHERE \(CambioMoneda.IdMoneda1) = ? and \(CambioMoneda.IdMoneda2) = ?
        """
    do{
        try Database.db.run(query1, [moneda1, moneda2])
        try Database.db.run(query2, [moneda2, moneda1])
    } catch {
        print("Error actualizarTipoCambio: ", error)
    }
}


//Config
func updateLastSync(){
    let date = Date()
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
    formatter.timeZone = TimeZone.init(abbreviation: "UTC")
    let query = """
    UPDATE \(Config.Table) SET \(Config.ValueCode) = ? WHERE _id = \(Config.LastSync)
"""
    do {
        try Database.db.run(query, [formatter.string(from: date)])
    } catch {
        print("Error updateLastSync: ", error)
    }
}
