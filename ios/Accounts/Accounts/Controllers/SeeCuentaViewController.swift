//
//  SeeCuentaViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/20/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class SeeCuentaViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    
    @IBOutlet weak var tableView: UITableView!
    var _id = 0 as Int64
    var dataArray:[[String:Any?]] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        dataArray = getTotalMoves(id: _id)
        tableView.delegate = self
        tableView.dataSource = self
        navigationItem.title = title
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        if "seeMovimientoSegueCuentas" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let viewController = segue.destination as! SeeMovimientoViewController
                viewController._id = dataArray[indexPath.row]["_id"] as! Int64
                viewController.title = "Movimiento"
            }
        }
        
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        //let data = dataArray[indexPath.row]
        let row = indexPath.row
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "seeMovCuentaCell") as! SeeMovByCuentasTableViewCell
        cell.setCell(data: dataArray[row])
        return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.performSegue(withIdentifier: "seeMovimientoSegueCuentas", sender: nil)
    }

}
