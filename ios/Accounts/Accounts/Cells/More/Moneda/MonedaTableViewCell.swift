//
//  MonedaTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 15/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MonedaTableViewCell: UITableViewCell {
    
    @IBOutlet weak var checkBox: CheckBox!
    @IBOutlet weak var code: UILabel!
    @IBOutlet weak var name: UILabel!
    @IBOutlet var flag: UIImageView!
    
    
    func setCell(data:[String:Any?], info:LocaleInfo?) {
        if info == nil {
            let c = data[Moneda.Moneda] as! String
            name.text = c
        } else {
            code.text = info!.currencyCode
            name.text = info!.currencyName
            flag.image = info!.currencyFlag
        }
        let active = data[Moneda.Active] as! Int64
        checkBox.isChecked = active == 1
        checkBox.addTarget(self, action: #selector(buttonAction), for: .touchUpInside)
        //checkBox.isChecked = (data[Moneda.Active] as! Int) == 1
    }
    @objc func buttonAction(sender: CheckBox!) {
        sender.isChecked = !sender.isChecked
        //CHECK: Inactivate
    }
}
