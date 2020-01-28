//
//  Database.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/12/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation
import SQLite


class Database{
    
    var database: Connection!
    static var db: Connection!
    let id = Expression<Int>("_id")
    
    let totalesTable = Table("AccountsTotales")
    let totalesCuenta = Expression<String>("Cuenta")
    let totalesInicial = Expression<Double>("CantidadInicial")
    let totalesCantidad = Expression<Double>("CurrentCantidad")
    let totalesIdMoneda = Expression<Int>("IdMoneda")
    let totalesActiva = Expression<Bool>("Activa")
    let totalesTipo = Expression<Int>("Tipo")
    
    let cambioMonedaTable = Table("AccountsCambioMoneda")
    let cambioMonedaMoneda1 = Expression<Int>("IdMoneda1")
    let cambioMonedaMoneda2 = Expression<Int>("IdMoneda2")
    let cambioMonedaTipo = Expression<Double?>("Tipo_de_cambio")
    
    let configTable = Table("AccountsConfig")
    let configKey = Expression<String>("KeyCode")
    let configValue = Expression<String>("ValueCode")
    
    let monedaTable = Table("AccountsMoneda")
    let monedaMoneda = Expression<String>("Moneda")
    let monedaActive = Expression<Int>("Active")
    
    let motivoTable = Table("AccountsMotivo")
    let motivoMotivo = Expression<String>("Motivo")
    let motivoActive = Expression<Bool>("Active")
    
    let movimientoTable = Table("AccountsMovimiento")
    let movimientoCantidad = Expression<Double>("Cantidad")
    let movimientoFecha = Expression<Date>("Fecha")
    let movimientoIdTotales = Expression<Int>("IdTotales")
    let movimientoIdMotivo = Expression<Int>("IdMotivo")
    let movimientoIdMoneda = Expression<Int>("IdMoneda")
    let movimientoCambio = Expression<Double?>("Cambio")
    let movimientoTraspaso = Expression<Int?>("Traspaso")
    let movimientoComment = Expression<String?>("comment")
    let movimientoIdViaje = Expression<Int?>("IdViaje")
    
    let personasTable = Table("AccountsPersonas")
    let personasNombre = Expression<String>("Nombre")
    let personasActive = Expression<Bool>("Active")
    
    let prestamosTable = Table("AccountsPrestamos")
    let prestamosCantidad = Expression<Double>("Cantidad")
    let prestamosFecha = Expression<Date>("Fecha")
    let prestamosIdTotales = Expression<Int>("IdTotales")
    let prestamosIdPersona = Expression<Int>("IdPersona")
    let prestamosIdMoneda = Expression<Int>("IdMoneda")
    let prestamosCambio = Expression<Double?>("Cambio")
    let prestamosIdMovimiento = Expression<Int?>("IdMovimiento")
    let prestamosComment = Expression<String?>("Comment")
    let prestamosCerrada = Expression<Bool>("Cerrada")
    
    let prestamosDetTable = Table("AccountsPrestamosDetalle")
    let prestamosDetCantidad = Expression<Double>("Cantidad")
    let prestamosDetFecha = Expression<Date>("Fecha")
    let prestamosDetIdTotales = Expression<Int>("IdTotales")
    let prestamosDetIdMoneda = Expression<Int>("IdMoneda")
    let prestamosDetCambio = Expression<Double>("Cambio")
    let prestamosDetIdPrestamo = Expression<Int>("IdPrestamo")
    
    let tiposCuentasTable = Table("AccountsTiposCuentas")
    let tiposCuentaTipo = Expression<String>("Tipo")
    
    let tripsTable = Table("AccountsTrips")
    let tripsNombre = Expression<String>("Nombre")
    let tripsDesc = Expression<String?>("Descripcion")
    let tripsFechaCre = Expression<Date>("FechaCreacion")
    let tripsFechaCier = Expression<Date?>("FechaCierre")
    let tripsFechaIni = Expression<Date?>("FechaInicio")
    let tripsFechaFin = Expression<Date?>("FechaFin")
    let tripsTotal = Expression<Double>("Total")
    let tripsIdMoneda = Expression<Int>("IdMoneda")
    
    init() {
        do{
            let documentDirectory = try FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            
            let fileUrl = documentDirectory.appendingPathComponent("Account").appendingPathExtension("sqlite")
            let dbase = try Connection(fileUrl.path)
            database = dbase
            Database.db = database
            if createTables() {
                initData()
            }
            
        } catch {
            print("Error ", error)
        }
        
    }
    func createTables() -> Bool{
        let createTableTotales = self.totalesTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(totalesCuenta, unique: true)
            table.column(totalesInicial)
            table.column(totalesCantidad)
            table.column(totalesIdMoneda)
            table.column(totalesActiva)
            table.column(totalesTipo)
        }
        let createTableCambioMoneda = self.cambioMonedaTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(self.cambioMonedaMoneda1)
            table.column(self.cambioMonedaMoneda2)
            table.column(self.cambioMonedaTipo)
        }
        let createTableConfig = self.configTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(self.configKey)
            table.column(self.configValue)
        }
        let createTableMoneda = self.monedaTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(self.monedaMoneda, unique: true)
            table.column(self.monedaActive)
        }
        let createTableMotivo = self.motivoTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(self.motivoMotivo, unique: true)
            table.column(self.motivoActive)
        }
        let createTableMovimiento = self.movimientoTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(movimientoCantidad)
            table.column(movimientoFecha)
            table.column(movimientoIdTotales)
            table.column(movimientoIdMotivo)
            table.column(movimientoIdMoneda)
            table.column(movimientoCambio)
            table.column(movimientoTraspaso)
            table.column(movimientoComment)
            table.column(movimientoIdViaje)
        }
        let createTablePersonas = personasTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(personasNombre, unique: true)
            table.column(personasActive)
        }
        let createTablePrestamos = prestamosTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(prestamosCantidad)
            table.column(prestamosFecha)
            table.column(prestamosIdTotales)
            table.column(prestamosIdMoneda)
            table.column(prestamosComment)
            table.column(prestamosIdPersona)
            table.column(prestamosCambio)
            table.column(prestamosIdMovimiento, defaultValue: nil)
            table.column(prestamosCerrada)
        }
        let createTablePrestamosDet = prestamosDetTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(prestamosDetCantidad)
            table.column(prestamosDetFecha)
            table.column(prestamosDetIdTotales)
            table.column(prestamosDetCambio)
            table.column(prestamosDetIdPrestamo)
            table.column(prestamosDetIdMoneda)
        }
        let createTableTiposCuentas = tiposCuentasTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(tiposCuentaTipo)
        }
        
        let createTableTrips = tripsTable.create{ table in
            table.column(id, primaryKey: true)
            table.column(tripsNombre)
            table.column(tripsDesc)
            table.column(tripsFechaCre)
            table.column(tripsFechaCier)
            table.column(tripsFechaIni)
            table.column(tripsFechaFin)
            table.column(tripsTotal)
            table.column(tripsIdMoneda)
        }
        do {
            try database.run(createTableCambioMoneda)
            try database.run(createTableConfig)
            try database.run(createTableMoneda)
            try database.run(createTableMotivo)
            try database.run(createTableMovimiento)
            try database.run(createTablePersonas)
            try database.run(createTablePrestamos)
            try database.run(createTablePrestamosDet)
            try database.run(createTableTiposCuentas)
            try database.run(createTableTotales)
            try database.run(createTableTrips)
            return true
        } catch {
            print("error create ", error)
        }
        return false
    }
    
    func initData(){
        let q1 = """
                    insert into \(Totales.Table)
                        (_id, \(Totales.Cuenta), \(Totales.CantidadInicial), \(Totales.CurrentCantidad), \(Totales.IdMoneda), \(Totales.Activa), \(Totales.Tipo))
                    values
                    (1, 'Prestamos', 0, 0, 1, 0, 1)
                """
        let q2 = """
            insert into \(Totales.Table)
                (_id, \(Totales.Cuenta), \(Totales.CantidadInicial), \(Totales.CurrentCantidad), \(Totales.IdMoneda), \(Totales.Activa), \(Totales.Tipo))
            values
            (20, 'xxxxx', 0, 0, 1, 0, 1)
        """
        let q3 = """
            insert into \(Motivo.Table)
                (_id, \(Motivo.Motivo), \(Motivo.Active))
            values
            (1, 'Traspaso', 0)
        """
        let q4 =  """
                   insert into \(Motivo.Table)
                       (_id, \(Motivo.Motivo), \(Motivo.Active))
                   values
                   (2, 'Retiro', 0)
               """
        
        let q5 = """
            insert into \(Motivo.Table)
                (_id, \(Motivo.Motivo), \(Motivo.Active))
            values
            (3, 'RetiroMonedaDiferente', 0)
        """
        
        let q6 = """
            insert into \(Config.Table)
                (_id, \(Config.KeyCode), \(Config.ValueCode))
            values
                (\(Config.LastUpdated), 'LastUpdated', CURRENT_TIMESTAMP)
        """
        let q7 = """
            insert into \(Config.Table)
                (_id, \(Config.KeyCode), \(Config.ValueCode))
            values
                (\(Config.LastSync), 'LastSync', CURRENT_TIMESTAMP)
        """
        let q8 = """
            insert into \(Config.Table)
                (_id, \(Config.KeyCode), \(Config.ValueCode))
            values
                (\(Config.Wifi), 'Use Wifi Only', 1)
        """
        let q9 = """
            INSERT into \(TiposCuentas.Table)
                (_id, \(TiposCuentas.Tipo))
            values ( 1 , 'Efectivo')
        """
        let q10 = """
            INSERT into \(TiposCuentas.Table)
                (_id, \(TiposCuentas.Tipo))
            values ( 2 , 'Tarjeta de Credito')
        """
        let q11 = """
            INSERT into \(TiposCuentas.Table)
                (_id, \(TiposCuentas.Tipo))
            values ( 3 , 'Tarjeta de Debito')
        """
        let q12 = """
            INSERT into \(TiposCuentas.Table)
                (_id, \(TiposCuentas.Tipo))
            values ( 4 , 'Cuentas de Inversion')
        """
        let q13 = """
            INSERT into \(TiposCuentas.Table)
                (_id, \(TiposCuentas.Tipo))
            values ( 5 , 'Prestamos')
        """
        
        let q14 = """
            insert into \(Motivo.Table)
                (_id, \(Motivo.Motivo), \(Motivo.Active))
            values
            (15, 'xxxxx', 0)
        """
        
        do {
            try database.run(q1)
            try database.run(q2)
            try database.run(q3)
            try database.run(q4)
            try database.run(q5)
            try database.run(q6)
            try database.run(q7)
            try database.run(q8)
            try database.run(q9)
            try database.run(q10)
            try database.run(q11)
            try database.run(q12)
            try database.run(q13)
            try database.run(q14)
        } catch {
            print("Error q: ", error)
        }
        
        
        
    }
    static func deleteTable(table: String){
        do {
            let stmt = try db.prepare("DELETE from \(table)")
            try stmt.run()
        } catch {
            print("Error delete: ", error)
        }
    }
    
    static func insertIntoTable(table: String, columns: Array<String>, values:Array<String?>){
        var i = 0
        //\(withName) and \(foldersOnly) and \(ownedByUser)"
        var query = "INSERT INTO \(table) ("
        var tmpVal = ""
        while i < columns.count {
            query += columns[i] + ", "
            i += 1
            tmpVal += "?, "
        }
        query.removeLast(2)
        tmpVal.removeLast(2)
        query += ") Values (" + tmpVal+")"
        //print("query: ", query)
        do {
            let stmt = try db.prepare(query)
            try stmt.run(values)
        } catch {
            print("Error insert ", error)
        }
    }
    static func stmtToDictionary(stmt: Statement) -> [[String:Any?]]{
        var result: [[String:Any?]] = [[:]]
        result.remove(at: 0)
        var columns = stmt.columnNames
        var i = 0
        for row in stmt {
            var x = 0
            var dictionary: [String:Any?] = [:]
            while x < columns.count {
                dictionary[columns[x]] = row[x]
                x = x + 1
            }
            result.append(dictionary)
            i = i + 1
        }
        return result
    }
}
