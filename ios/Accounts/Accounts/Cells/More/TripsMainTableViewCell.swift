//
//  TripsMainTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class TripsMainTableViewCell: UITableViewCell {
    @IBOutlet weak var fecha: UILabel!
    @IBOutlet weak var Name: UILabel!
    @IBOutlet weak var Cantidad: UILabel!
    @IBOutlet weak var Moneda: UILabel!
    
    func setCell(data: [String:Any?]){
        let fechaInic = data[Trips.FechaInicio]
        let fechaFin = data[Trips.FechaFin]
        let moneda = data["Moneda"] as! String
        let nombre = data[Trips.Nombre] as! String
        let cantidad = data["Total"] as! Double
        
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        var fechaFinString = "Not defined"
        var fechaInicString = "Not defined"
        if fechaFin != nil {
            let dateFin:Date = dateFormatter.date(from: fechaFin as! String)!
            fechaFinString = dateFormatter.string(from: dateFin)
        }
        if fechaInic != nil {
            let dateInic:Date = dateFormatter.date(from: fechaInic as! String)!
            fechaInicString = dateFormatter.string(from: dateInic)
        }
        
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        
        
        fecha.text = "\(fechaInicString) - \(fechaFinString)"
        Name.text = nombre
        Cantidad.text = "\(numberFormatter.string(from: NSNumber(value: cantidad))!)"
        Moneda.text = moneda
    }
}
