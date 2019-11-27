//
//  MotivosTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 26/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MotivosTableViewCell: UITableViewCell {

    @IBAction func checkBoxClick(_ sender: CheckBox) {
        print("cb Clicked: ", row)
        sender.isChecked = !sender.isChecked
        if sender.isChecked {
            parent?.dataMotivos[row][Motivo.Active] = (1 as Int64)
        } else {
            parent?.dataMotivos[row][Motivo.Active] = (0 as Int64)
        }
        if parent?.edit[row] == nil {
             parent?.edit[row] = MotiveData(motivo: nil, active: true)
        }
    }
    @IBAction func motivoEdit(_ sender: UITextField) {
        if parent?.edit[row] == nil {
            parent?.edit[row] = MotiveData(motivo: sender, active: true)
        }
        
    }
    
    
    @IBOutlet var checkBox: CheckBox!
    @IBOutlet var motivo: UITextField!
    var row: Int = 0
    
    var parent: MotivesMainViewController?
    func setCell(data:[String:Any?], parent: MotivesMainViewController, row: Int){
        self.parent = parent
        self.row = row
        let active = data[Motivo.Active] as! Int64
        let motivoString = data[Motivo.Motivo] as! String
        motivo.text = motivoString
        checkBox.isChecked = active == 1
    }
}
