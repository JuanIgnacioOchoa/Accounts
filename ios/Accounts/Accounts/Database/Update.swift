//
//  Update.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/24/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation
import SQLite

// Totales

func newMoveCuenta(cantidad:Double, idCuenta:Int64) -> Bool{
    //updateLastSync()
    let total = getTotal(id: idCuenta)
    let cantidadOld = total[Totales.CurrentCantidad] as! Double
    let query = """
    UPDATE \(Totales.Table) SET \(Totales.CurrentCantidad) = \(cantidadOld + cantidad) WHERE _id = ?
"""
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
        try Database.db.run(query1, [idMove, idMove])
        try Database.db.run(query2, [cantidad, idCuenta])
        return true
    } catch {
        print("Error actializarCuentaMove: ", error)
    }
    return false
}

func actualizarCuentaPrestamo(cantidad:Double, idCuenta:Int64, idPrestamo:Int64) -> Bool {
    let query1 = """
                UPDATE AccountsTotales set CurrentCantidad = (select (t.CurrentCantidad - m.Cantidad) as Cantidad from AccountsPrestamos as m, AccountsTotales as t where m.IdTotales = t._id and m._id = ?) WHERE _id = (SELECT IdTotales FROM AccountsPrestamos WHERE _id = ?)
                """
    let query2 = """
                UPDATE \(Totales.Table) set \(Totales.CurrentCantidad) = \(Totales.CurrentCantidad) + ? WHERE _id = ?
                """
    do{
        try Database.db.run(query1, [idPrestamo, idPrestamo])
        try Database.db.run(query2, [cantidad, idCuenta])
        return true
    } catch {
        print("Error actializarCuentaMove: ", error)
    }
    return false
}

func updateTotalesFromPrestamo(cantidad:Double, idCuenta:Int64) -> Bool {
    let query = """
                    UPDATE \(Totales.Table) set \(Totales.CurrentCantidad) = (SELECT (t.CurrentCantidad + ?) as Cantidad FROM \(Totales.Table) as t where t._id = ?)
                        WHERE _id = ?
                """
    do{
        try Database.db.run(query, [cantidad, idCuenta, idCuenta])
        return true
    } catch {
        print("Error actializarCuentaMove: ", error)
    }
    return false
}

//--------------------------End Totales
//--------------------------Movimiento

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
    var query = ""
    var array:[Binding?]
    if comment != nil {
        query = """
         UPDATE \(Movimiento.Table) SET \(Movimiento.Cantidad) = ?, \(Movimiento.IdTotales) = ?, \(Movimiento.IdMotivo) = ?, \(Movimiento.Traspaso) = ?, \(Movimiento.Cambio) = ?, \(Movimiento.Fecha) = ?, \(Movimiento.Comment) = ?
                    WHERE _id = ?
        """
        array = [cantidad, idFrom, idMot, idTo, cambio, dateFormatter.string(from: date), comment!, id]
    } else {
        query = """
         UPDATE \(Movimiento.Table) SET \(Movimiento.Cantidad) = ?, \(Movimiento.IdTotales) = ?, \(Movimiento.IdMotivo) = ?, \(Movimiento.Traspaso) = ?, \(Movimiento.Cambio) = ?, \(Movimiento.Fecha) = ?, \(Movimiento.Comment) = null
                    WHERE _id = ?
        """
        array = [cantidad, idFrom, idMot, idTo, cambio, dateFormatter.string(from: date), id]
    }

    
    do {
        try Database.db.run(query, array)
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
//------------------End Movimientos

//MARK: Motivos

func updateMotive(motivo:String, active:Int64, id:Int64) -> Bool{
    let query = """
    UPDATE \(Motivo.Table) SET \(Motivo.Active) = ?, \(Motivo.Motivo) = ?
                WHERE _id = ?
    """
    do {
        try Database.db.run(query, [active, motivo, id])
        return true
    } catch {
        print("Error updateMotive: ", error)
    }
    
    return false
}

//MARK: CambioMoneda

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

//-----------------------End CambioMoneda

//----------------------Trips
func actualizarTrip(id:Int64, name:String, desc:String?, fechaInic:Date, fechaFin:Date, idMoneda:Int64) -> Bool {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var query = ""
    var array:[Binding?]
    if desc != nil {
        query = """
        UPDATE \(Trips.Table) SET \(Trips.Nombre) = ?, \(Trips.Descripcion) = ?, \(Trips.FechaInicio) = ?, \(Trips.FechaFin) = ?, \(Trips.IdMoneda) = ?
                    WHERE _id = ?
        """
        array = [name, desc, dateFormatter.string(from: fechaInic), dateFormatter.string(from: fechaFin), idMoneda, id]
    } else {
        query = """
        UPDATE \(Trips.Table) SET \(Trips.Nombre) = ?, \(Trips.Descripcion) = null, \(Trips.FechaInicio) = ?, \(Trips.FechaFin) = ?, \(Trips.IdMoneda) = ?
                    WHERE _id = ?
        """
        array = [name, dateFormatter.string(from: fechaInic), dateFormatter.string(from: fechaFin), idMoneda, id]
    }

    
    do {
        try Database.db.run(query, array)
        return true
    } catch {
        print("Error actualizar Movimiento: ", error)
    }
    
    return false
}
//----------------------End Trips

//-----------------------------Prestamos------------------

func updatePrestamo(id:Int64, cantidad:Double, idTotales:Int64, idMoneda:Int64, idPersona:Int64, comment:String?, cambio:Double, date:Date) -> Bool {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    var query = ""
    var array:[Binding?]
    if comment != nil {
        query = """
        UPDATE \(Prestamos.Table) SET \(Prestamos.Cantidad) = ?, \(Prestamos.IdTotales) = ?, \(Prestamos.IdMoneda) = ?, \(Prestamos.IdPersona) = ?, \(Prestamos.Comment) = ?, \(Prestamos.Cambio) = ?, \(Prestamos.Fecha) = ?
                    WHERE _id = ?
        """
        array = [cantidad, idTotales, idMoneda, idPersona, comment!, cambio, dateFormatter.string(from: date), id]
    } else {
        query = """
        UPDATE \(Prestamos.Table) SET \(Prestamos.Cantidad) = ?, \(Prestamos.IdTotales) = ?, \(Prestamos.IdMoneda) = ?, \(Prestamos.IdPersona) = ?, \(Prestamos.Cambio) = ?, \(Prestamos.Fecha) = ?
                    WHERE _id = ?
        """
        array = [cantidad, idTotales, idMoneda, idPersona, cambio, dateFormatter.string(from: date), id]
    }

    
    do {
        try Database.db.run(query, array)
        return true
    } catch {
        print("Error actualizar Movimiento: ", error)
    }
    
    return false
}


func updatePrestamoDetalle(cantidad:Double, idTotales:Int64, idMoneda:Int64, idPrestamo:Int64, cambio:Double, _id:Int64, date:Date) -> Bool {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "yyyy-MM-dd"
    dateFormatter.timeZone = TimeZone.current
    let query = """
                UPDATE \(PrestamosDet.Table) set \(PrestamosDet.Cantidad) = ?, \(PrestamosDet.IdTotales) = ?, \(PrestamosDet.IdMoneda) = ?, \(PrestamosDet.IdPrestamo) = ?, \(PrestamosDet.Cambio) = ?, \(PrestamosDet.Fecha) = ?
                WHERE _id = ?
                """
    do{
        try Database.db.run(query, [cantidad, idTotales, idMoneda, idPrestamo, cambio, dateFormatter.string(from: date), _id])
        return true
    } catch {
        print("Error actializarCuentaMove: ", error)
    }
    return false
}
//-----------------------------------End Prestamos


//MARK: Config
func updateOnlyWifi(val:Bool) {
        let query = """
    UPDATE \(Config.Table) SET \(Config.ValueCode) = ? WHERE _id = \(Config.Wifi)
    """
    var ac = "0"
    if val {
        ac = "1"
    }
    do {
        try Database.db.run(query, [ac])
    } catch {
        print("Error updateLastSync: ", error)
    }
}
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

//------------------End Config
