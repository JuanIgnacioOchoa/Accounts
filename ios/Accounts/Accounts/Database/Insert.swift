//
//  Insert.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/17/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation


//--------------------------------Totales

func newTotales(cantidad:Double, cuenta:String, idMoneda:Int64) -> Bool{
    let query = """
    INSERT INTO \(Totales.Table) (\(Totales.Cuenta), \(Totales.CantidadInicial), \(Totales.CurrentCantidad), \(Totales.IdMoneda))
    VALUES (?, ?, ?, ?)
"""
    do{
        try Database.db.run(query, [cuenta, cantidad, cantidad, idMoneda])
        return true
    } catch {
        print("Error new Totales: ", error)
    }
    return false
}
//-----------------------------End Totales------------------
//-----------------------------------Movimientos

func newMove(cantidad:Double, idCuenta:Int64, comment:String? , idMotivo:Int64, idMoneda:Int64, cambio:Double, date: Date) -> Bool{
    //updateLastSync()
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var query = ""
    if comment == nil{
        query = """
        INSERT INTO \(Movimiento.Table) (\(Movimiento.Cantidad), \(Movimiento.IdTotales), \(Movimiento.Comment),
        \(Movimiento.IdMotivo), \(Movimiento.IdMoneda), \(Movimiento.Cambio), \(Movimiento.Fecha)) Values (\(cantidad), \(idCuenta), '\("")', \(idMotivo), \(idMoneda), \(cambio), '\(dateFormatter.string(from: date))')
        """
    } else {
        query = """
        INSERT INTO \(Movimiento.Table) (\(Movimiento.Cantidad), \(Movimiento.IdTotales), \(Movimiento.Comment),
        \(Movimiento.IdMotivo), \(Movimiento.IdMoneda), \(Movimiento.Cambio), \(Movimiento.Fecha)) Values (\(cantidad), \(idCuenta), '\(comment!)', \(idMotivo), \(idMoneda), \(cambio), '\(dateFormatter.string(from: date))')
        """
    }

    print("Query: ", query)
    do {
        try Database.db.run(query)
        return true
    } catch {
        print("Error New Move: ", error)
        
    }
    return false
}

func newTraspaso(cuentaFrom:Int64, cuentaTo:Int64, cantidad:Double, cambio:Double, comment:String?, date:Date, idMot:Int, idMon: Int) -> Bool{
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var sComment = ""
    if comment != nil {
        sComment = comment!
    }
    let query = """
    INSERT INTO \(Movimiento.Table) (\(Movimiento.Cantidad), \(Movimiento.Comment), \(Movimiento.IdMotivo), \(Movimiento.IdMoneda), \(Movimiento.IdTotales), \(Movimiento.Traspaso), \(Movimiento.Fecha), \(Movimiento.Cambio) Values (?, ?, ?, ?, ?, ?, ?)
    """
    do {
        try Database.db.run(query, [cantidad, sComment,idMot, idMon, cuentaFrom, cuentaTo, dateFormatter.string(from: date), cambio])
        return true
    } catch {
        print("Error New Move: ", error)
    }
    return false
}
//------------------------------------End Movimientos--------------------------
