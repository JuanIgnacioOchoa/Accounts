//
//  ReportesTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/27/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class ReportesTableViewCell: UITableViewCell {

    @IBOutlet weak var income: UILabel!
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var outcome: UILabel!
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
        income.text = numberFormatter.string(from: incomeN)
        outcome.text = numberFormatter.string(from: outcomeN)
        title.text = m
    }

}
