//
//  PrestamosTotalsTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 06/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class PrestamosTotalsTableViewCell: UITableViewCell {

    //PrestamosTotals
    @IBOutlet weak var NombreLbl: UILabel!
    @IBOutlet weak var CantidadLbl: UILabel!
    @IBOutlet weak var lastFechaLbl: UILabel!
    
    func setCell(data:[String:Any?]){
        let cantidad = data["Cantidad"] as! NSNumber
        let fecha = data["Fecha"] as! String
        let moneda = data[Moneda.Moneda] as! String
        
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let date:Date = dateFormatter.date(from: fecha)!
        dateFormatter.dateFormat = "dd-MMM-yy"
        
        if Int(truncating: cantidad) < 0 {
            NombreLbl.textColor = UIColor.red
            CantidadLbl.textColor = UIColor.red
            lastFechaLbl.textColor = UIColor.red
        } else if Int(truncating: cantidad) > 0 {
            NombreLbl.textColor = Utils.green
            CantidadLbl.textColor = Utils.green
            lastFechaLbl.textColor = Utils.green
        }
        
        NombreLbl.text = data[Personas.Nombre] as? String
        CantidadLbl.text = "\(numberFormatter.string(from: cantidad)!) \(moneda)"
        lastFechaLbl.text = "\(dateFormatter.string(from: date))"
    }
}
