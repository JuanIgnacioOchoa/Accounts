//
//  MainCuentasTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MainCuentasTableViewCell: UITableViewCell {
    
    @IBOutlet weak var cantidadLbl: UILabel!
    @IBOutlet weak var cuentasLbl: UILabel!
    @IBOutlet weak var monedaLbl: UILabel!
    
    func setCell(data: Dictionary<String, Any?>){
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let cantidad = data[Totales.CurrentCantidad] as! NSNumber
        let cuenta = data[Totales.Cuenta] as! String
        let moneda = data[Moneda.Moneda] as! String
        cantidadLbl.text = numberFormatter.string(from: cantidad)
        cuentasLbl.text = cuenta
        monedaLbl.text = moneda
        if Int(truncating: cantidad) < 0{
            cantidadLbl.textColor = UIColor.red
            cuentasLbl.textColor = UIColor.red
            monedaLbl.textColor = UIColor.red
        } else if Int(truncating: cantidad) > 0 {
            cantidadLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            cuentasLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            monedaLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
        
    }
}
