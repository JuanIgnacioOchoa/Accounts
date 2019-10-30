//
//  AnalisisMotivesTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 25/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class AnalisisMotivesTableViewCell: UITableViewCell {
    @IBOutlet weak var motiveLbl: UILabel!
    @IBOutlet weak var outcomeLbl: UILabel!
    @IBOutlet weak var incomeLbl: UILabel!
    
    
    func setCell(data: Dictionary<String, Any?>){
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let m = data["Motivo"] as! String
        let inc = data["Ingreso"] as? NSNumber
        let outc = data["Gasto"] as? NSNumber
        var incomeN:NSNumber = 0.0
        if inc != nil{
            incomeN = inc!
        }
        var outcomeN:NSNumber = 0.0
        if outc != nil{
            outcomeN = outc!
        }
        incomeLbl.text = numberFormatter.string(from: incomeN)
        outcomeLbl.text = numberFormatter.string(from: outcomeN)
        motiveLbl.text = m
    }
}
