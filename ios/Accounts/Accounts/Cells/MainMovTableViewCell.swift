//
//  MainMovTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/12/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MainMovTableViewCell: UITableViewCell {

    @IBOutlet weak var fechaLbl: UILabel!
    @IBOutlet weak var cuentaLbl: UILabel!
    @IBOutlet weak var cantidadLbl: UILabel!
    @IBOutlet weak var monedaLbl: UILabel!
    @IBOutlet weak var motivoLbl: UILabel!
    
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
        monedaLbl.text = moneda
        fechaLbl.text = fecha
        motivoLbl.text = motivo
    }
    
}
