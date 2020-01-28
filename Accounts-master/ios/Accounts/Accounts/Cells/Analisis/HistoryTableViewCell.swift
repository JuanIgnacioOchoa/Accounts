//
//  HistoryTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 01/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class HistoryTableViewCell: UITableViewCell {

    @IBOutlet weak var checkBox: CheckBox!
    @IBOutlet weak var label1: UILabel!
    @IBOutlet weak var label2: UILabel!
    
    
    func setCell(cuenta: CuentaData){
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        checkBox.tintColor = cuenta.color
        checkBox.isChecked = cuenta.selected
        label1.text = cuenta.name
        label2.text = numberFormatter.string(from: NSNumber(value: cuenta.cantidadActual))
    }
}
