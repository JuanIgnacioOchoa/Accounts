//
//  Names.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/18/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation

//Totales
class Totales{
    static let Table = "AccountsTotales"
    static let Cuenta = "Cuenta"
    static let CantidadInicial = "CantidadInicial"
    static let CurrentCantidad = "CurrentCantidad"
    static let IdMoneda = "IdMoneda"
    static let Activa = "Activa"
    static let Tipo = "Tipo"
}
class Movimiento {
    static let Table = "AccountsMovimiento"
    static let Cantidad = "Cantidad"
    static let Fecha = "Fecha"
    static let IdTotales = "IdTotales"
    static let IdMotivo = "IdMotivo"
    static let IdMoneda = "IdMoneda"
    static let Cambio = "Cambio"
    static let Traspaso = "Traspaso"
    static let Comment = "comment"
    static let IdViaje = "IdViaje"
}

class CambioMoneda {
    static let Table = "AccountsCambioMoneda"
    static let IdMoneda1 = "IdMoneda1"
    static let IdMoneda2 = "IdMoneda2"
    static let TipoCambio = "Tipo_de_cambio"
}

class Config {
    static let Table = "AccountsConfig"
    static let KeyCode = "KeyCode"
    static let ValueCode = "ValueCode"
    static let LastUpdated = 1;
    static let LastSync = 2;
    static let Wifi = 3;
}

class Moneda{
    static let Table = "AccountsMoneda"
    static let Moneda = "Moneda"
    static let Active = "Active"
}

class Motivo {
    static let Table = "AccountsMotivo"
    static let Motivo = "Motivo"
    static let Active = "Active"
}

class Personas {
    static let Table = "AccountsPersonas"
    static let Nombre = "Nombre"
    static let Active = "Active"
}

class Prestamos {
    static let Table = "AccountsPrestamos"
    static let Cantidad = "Cantidad"
    static let Fecha = "Fecha"
    static let IdTotales = "IdTotales"
    static let IdPersona = "IdPersona"
    static let IdMoneda = "IdMoneda"
    static let Cambio = "Cambio"
    static let IdMovimiento = "IdMovimiento"
    static let Comment = "Comment"
    static let Cerrada = "Cerrada"
}

class PrestamosDet {
    static let Table = "AccountsPrestamosDetalle"
    static let Cantidad = "Cantidad"
    static let Fecha = "Fecha"
    static let IdTotales = "IdTotales"
    static let IdMoneda = "IdMoneda"
    static let Cambio = "Cambio"
    static let IdPrestamo = "IdPrestamo"
}

class TiposCuentas {
    static let Table = "AccountsTiposCuentas"
    static let Tipo = "Tipo"
}

class Trips {
    static let Table = "AccountsTrips"
    static let Nombre = "Nombre"
    static let Descripcion = "Descripcion"
    static let FechaCreacion = "FechaCreacion"
    static let FechaCierre = "FechaCierre"
    static let FechaInicio = "FechaInicio"
    static let FechaFin = "FechaFin"
    static let Total = "Total"
    static let IdMoneda = "IdMoneda"
}
