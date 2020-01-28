//
//  MovByFechaTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 10/23/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MovByFechaTableViewCell: UITableViewCell {

    @IBOutlet weak var cantidadLbl: UILabel!
    @IBOutlet weak var motivoLbl: UILabel!
    @IBOutlet weak var cuentaLbl: UILabel!
    
    func setCell(data: Dictionary<String, Any?>){
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let cantidad = data[Movimiento.Cantidad] as! NSNumber
        let cuenta = data[Totales.Cuenta] as! String
        let moneda = data[Moneda.Moneda] as! String
        let fecha = data[Movimiento.Fecha] as! String
        let motivo = data[Motivo.Motivo] as! String
        cantidadLbl.text = numberFormatter.string(from: cantidad)
        cuentaLbl.text = cuenta
        motivoLbl.text = motivo
        if Int(truncating: cantidad) < 0{
            cantidadLbl.textColor = UIColor.red
        } else if Int(truncating: cantidad) > 0 {
            cantidadLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
    }
}
