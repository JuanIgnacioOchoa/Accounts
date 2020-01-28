//
//  PrestamosDetTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 12/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class PrestamosDetTableViewCell: UITableViewCell {

    @IBOutlet weak var CantidadLbl: UILabel!
    @IBOutlet weak var CuentaLbl: UILabel!
    @IBOutlet weak var FechaLbl: UILabel!
    
    func setCell(data: [String:Any?]){
        let cantidad = data["Cantidad"] as! NSNumber
        let fecha = data["Fecha"] as! String
        let moneda = data[Moneda.Moneda] as! String
        let cuenta = data[Totales.Cuenta] as! String
        
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let date:Date = dateFormatter.date(from: fecha)!
        dateFormatter.dateFormat = "dd-MMM-yy"
        
        CuentaLbl.text = cuenta
        CantidadLbl.text = "\(numberFormatter.string(from: cantidad)!) \(moneda)"
        FechaLbl.text = "\(dateFormatter.string(from: date))"
    }
}
