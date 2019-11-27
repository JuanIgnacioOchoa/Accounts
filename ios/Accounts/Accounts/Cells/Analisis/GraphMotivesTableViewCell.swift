//
//  GraphMotivesTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 31/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class GraphMotivesTableViewCell: UITableViewCell {
    @IBOutlet weak var motiveTxt: UILabel!
    @IBOutlet weak var cantidadTxt: UILabel!
    @IBOutlet weak var porcentajeTxt: UILabel!
    @IBOutlet weak var color: UILabel!
    
    func setCell(motivo:String, cantidad:Double, porcentaje:Double, colorc: UIColor){
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        
        var color = Utils.green
        if cantidad < 0 {
            color = UIColor.red
        }
        
        cantidadTxt.textColor = color
        porcentajeTxt.textColor = color
        self.color.backgroundColor = colorc
        cantidadTxt.text = "\(numberFormatter.string(from: NSNumber(value: cantidad))!)"
        porcentajeTxt.text = "\(numberFormatter.string(from: NSNumber(value: porcentaje * 100))!)%"
        motiveTxt.text = motivo
    }
}
