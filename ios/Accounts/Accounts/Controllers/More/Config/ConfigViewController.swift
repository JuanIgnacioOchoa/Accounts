//
//  ConfigViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class ConfigViewController: UIViewController {

    @IBAction func wifiCBClick(_ sender: CheckBox) {
        wifiCheckBox.isChecked = !wifiCheckBox.isChecked
        updateOnlyWifi(val: wifiCheckBox.isChecked)
    }
    @IBOutlet var wifiCheckBox: CheckBox!
    override func viewDidLoad() {
        super.viewDidLoad()

        wifiCheckBox.isChecked = getOnlyWifi()
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
