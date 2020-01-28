//
//  Insert.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/17/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation
import SQLite

//MARK: Totales

func newTotales(cantidad:Double, cuenta:String, idMoneda:Int64, tipoCuenta:Int64) -> Bool{
    let query = """
    INSERT INTO \(Totales.Table) (\(Totales.Cuenta), \(Totales.CantidadInicial), \(Totales.CurrentCantidad), \(Totales.IdMoneda), \(Totales.Activa), \(Totales.Tipo))
    VALUES (?, ?, ?, ?, 1, ?)
"""
    do{
        try Database.db.run(query, [cuenta, cantidad, cantidad, idMoneda, tipoCuenta])
        return true
    } catch {
        print("Error new Totales: ", error)
    }
    return false
}
//-----------------------------End Totales------------------
//MARK:Movimientos

func newMove(cantidad:Double, idCuenta:Int64, comment:String? , idMotivo:Int64, idMoneda:Int64, cambio:Double, date: Date, idViaje: Int64) -> Bool{
    //updateLastSync()
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var query = ""
    if comment == nil{
        query = """
        INSERT INTO \(Movimiento.Table) (\(Movimiento.Cantidad), \(Movimiento.IdTotales), \(Movimiento.IdMotivo), \(Movimiento.IdMoneda), \(Movimiento.Cambio), \(Movimiento.Fecha), \(Movimiento.IdViaje)) Values (\(cantidad), \(idCuenta), \(idMotivo), \(idMoneda), \(cambio), '\(dateFormatter.string(from: date))', \(idViaje)
        """
    } else {
        query = """
        INSERT INTO \(Movimiento.Table) (\(Movimiento.Cantidad), \(Movimiento.IdTotales), \(Movimiento.Comment),
        \(Movimiento.IdMotivo), \(Movimiento.IdMoneda), \(Movimiento.Cambio), \(Movimiento.Fecha), \(Movimiento.IdViaje)) Values (\(cantidad), \(idCuenta), '\(comment!)', \(idMotivo), \(idMoneda), \(cambio), '\(dateFormatter.string(from: date))', \(idViaje))
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
    var query = ""
    var array:[Binding?]
    if comment != nil {
        query = """
        INSERT INTO \(Movimiento.Table) (\(Movimiento.Cantidad), \(Movimiento.Comment), \(Movimiento.IdMotivo), \(Movimiento.IdMoneda), \(Movimiento.IdTotales), \(Movimiento.Traspaso), \(Movimiento.Fecha), \(Movimiento.Cambio)) Values (?, ?, ?, ?, ?, ?, ?)
        """
        array = [cantidad, comment! ,idMot, idMon, cuentaFrom, cuentaTo, dateFormatter.string(from: date), cambio]
    } else {
        query = """
        INSERT INTO \(Movimiento.Table) (\(Movimiento.Cantidad), \(Movimiento.IdMotivo), \(Movimiento.IdMoneda), \(Movimiento.IdTotales), \(Movimiento.Traspaso), \(Movimiento.Fecha), \(Movimiento.Cambio)) Values (?, ?, ?, ?, ?, ?)
        """
        array = [cantidad,idMot, idMon, cuentaFrom, cuentaTo, dateFormatter.string(from: date), cambio]
    }
    do {
        try Database.db.run(query, array)
        return true
    } catch {
        print("Error New Move: ", error)
    }
    return false
}
//------------------------------------End Movimientos--------------------------
//MARK: Prestamos
func newPrestamo(cantidad:Double, idTotales:Int64, idMoneda:Int64, idPersona:Int64, comment:String?, cambio:Double, idMove:Int64, date:Date) -> Bool {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var query = ""
    var array:[Binding?]
    if comment != nil {
        query = """
        INSERT INTO \(Prestamos.Table) (\(Prestamos.Cantidad), \(Prestamos.IdTotales), \(Prestamos.IdMoneda), \(Prestamos.IdPersona), \(Prestamos.Cambio), \(Prestamos.IdMovimiento), \(Prestamos.Fecha), \(Prestamos.Comment), \(Prestamos.Cerrada)) Values (?, ?, ?,? , ?, ?, ?, ?, ?)
        """
        array = [cantidad, idTotales, idMoneda, idPersona, cambio, idMove, dateFormatter.string(from: date), comment!, 0]
    } else {
        query = """
        INSERT INTO \(Prestamos.Table) (\(Prestamos.Cantidad), \(Prestamos.IdTotales), \(Prestamos.IdMoneda), \(Prestamos.IdPersona), \(Prestamos.Cambio), \(Prestamos.IdMovimiento), \(Prestamos.Fecha), \(Prestamos.Cerrada)) Values (?, ?, ?, ?, ?, ?, ?, ?)
        """
        array = [cantidad, idTotales, idMoneda, idPersona, cambio, idMove, dateFormatter.string(from: date), 0]
    }
    do {
        try Database.db.run(query, array)
        return true
    } catch {
        print("Error New Prestamo: ", error)
        print(query, array)
    }
    return false
}

func insertPrestamoDetalle(cantidad:Double, idTotales:Int64, idMoneda:Int64, idPrestamo:Int64, cambio:Double, date:Date) -> Bool {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var array:[Binding?]
    let query = """
    INSERT INTO \(PrestamosDet.Table) (\(PrestamosDet.Cantidad), \(PrestamosDet.IdTotales), \(PrestamosDet.IdMoneda), \(PrestamosDet.IdPrestamo),  \(PrestamosDet.Cambio), \(PrestamosDet.Fecha)) Values (?, ?, ?, ?, ?, ?)
    """
    array = [ cantidad, idTotales, idMoneda, idPrestamo, cambio, dateFormatter.string(from: date) ]

    do {
        try Database.db.run(query, array)
        return true
    } catch {
        print("Error New insertPrestamoDetalle: ", error)
        print(query, array)
    }
    return false
}
//MARK: Motivo
func guardarMotivo(motivo:String) -> Bool {
    let query = """
                    INSERT INTO AccountsMotivo(Motivo, Active)
                    SELECT ?, 1
                    WHERE NOT EXISTS(SELECT 1 FROM AccountsMotivo WHERE Motivo = ?)
                """
    print(query)
    do {
        try Database.db.run(query, [motivo, motivo])
        return true
    } catch {
        print("Error New Motivo: ", error, [motivo])
    }
    return false
}

//MARK: Moneda

func guardarMoneda(moneda:String) -> Bool {
    let query = """
                    INSERT INTO AccountsMoneda(Moneda, Active)
                    SELECT ?, 1
                    WHERE NOT EXISTS(SELECT 1 FROM AccountsMoneda WHERE Moneda = ?)
                """
    print(query)
    do {
        try Database.db.run(query, [moneda, moneda])
        return true
    } catch {
        print("Error New Moneda: ", error, [moneda])
    }
    return false
}


//MARK: Trips

func createTrip(nombre:String, fechaInic:Date?, fechaFin:Date?, moneda:Int64, descripcion:String) -> Bool{
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var tmpInicCol = ""
    var tmpFinCol = ""
    var tmpInicVal = ""
    var tmpFinVal = ""
    if fechaFin != nil {
        tmpFinCol = ", \(Trips.FechaFin)"
        tmpFinVal = ", \(dateFormatter.string(from: fechaFin!))"
    }
    if fechaInic != nil {
        tmpInicCol = ", \(Trips.FechaInicio)"
        tmpInicVal = ", \(dateFormatter.string(from: fechaInic!))"
    }
    let query = """
                    INSERT INTO \(Trips.Table)(\(Trips.Nombre) \(tmpInicCol) \(tmpFinCol), \(Trips.IdMoneda), \(Trips.Descripcion))
                    VALUES(\(nombre) \(tmpInicVal) \(tmpFinVal), \(moneda), \(descripcion) )
                   """
    do {
        try Database.db.run(query)
        return true
    } catch {
        print("Error New Moneda: ", error, [moneda])
    }
    return false
}
