//
//  CheckBox.swift
//  Accounts
//
//  Created by Juan Ochoa on 01/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class CheckBox: UIButton {
    // Images
    var checkedImage = UIImage(named: "checkbox")! as UIImage
    let uncheckedImage = UIImage(named: "blankbox")! as UIImage
    // Bool property
    var isChecked: Bool = false {
        didSet{
            if isChecked == true {
                self.setImage(checkedImage, for: UIControl.State.normal)
            } else {
                self.setImage(uncheckedImage, for: UIControl.State.normal)
            }
        }
    }

    override func awakeFromNib() {
        //tintColor = UIColor.blue
        // self.addTarget(self, action:#selector(buttonClicked(sender:)), for: UIControl.Event.touchUpInside)
        self.isChecked = false
    }
}
