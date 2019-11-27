//
//  MoreTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 04/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MoreTableViewCell: UITableViewCell {

    @IBOutlet weak var iconIm: UIImageView!
    @IBOutlet weak var label: UILabel!
    
    func setCell(image:UIImage, string:String){
        label.text = string
        iconIm.image = image
    }

}
