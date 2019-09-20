//
//  SeeMovByCuentasTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/20/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class SeeMovByCuentasTableViewCell: UITableViewCell {

    @IBOutlet weak var fechaLbl: UILabel!
    @IBOutlet weak var motivoLbl: UILabel!
    @IBOutlet weak var cantidadLbl: UILabel!
    

    func setCell(data: Dictionary<String, Any?>){
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let cantidad = data[Movimiento.Cantidad] as! NSNumber
        let motivo = data[Motivo.Motivo] as! String
        let fecha = data[Movimiento.Fecha] as! String
        cantidadLbl.text = numberFormatter.string(from: cantidad)
        motivoLbl.text = motivo
        fechaLbl.text = fecha
        if Int(truncating: cantidad) < 0{
            cantidadLbl.textColor = UIColor.red
        } else if Int(truncating: cantidad) > 0 {
            cantidadLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
    }

}
