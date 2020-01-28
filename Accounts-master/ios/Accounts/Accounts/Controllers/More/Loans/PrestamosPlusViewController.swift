//
//  PrestamosPlusViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class PrestamosPlusViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    var dataArray:[[String:Any?]] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()

        dataArray = getPrestamosPlus(zero: true)
        
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 30
        // Do any additional setup after loading the view.
    }
    
    func updateArray(zero: Bool){
        dataArray = getPrestamosPlus(zero: zero)
        tableView.reloadData()
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let data = dataArray[indexPath.row]
        //let cell: UITableViewCell=
        let cell = tableView.dequeueReusableCell(withIdentifier: "PrestamosTotals") as! PrestamosTotalsTableViewCell
        cell.setCell(data: data)
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let parent = self.parent?.parent as! PrestamosMainViewController
        parent.selected = dataArray[indexPath.row]["_id"] as! Int64
        parent.performSegue(withIdentifier: "verPrestamo", sender: nil)
    }
    
}
