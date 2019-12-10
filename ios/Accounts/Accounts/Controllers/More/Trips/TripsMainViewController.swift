//
//  TripsMainViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class TripsMainViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {

    @IBAction func addNewTrip(_ sender: UIButton) {
        self.performSegue(withIdentifier: "createTrip", sender: nil)
    }
    @IBOutlet weak var tableView: UITableView!
    
    var dataArray:[[String:Any?]] = []

    override func viewDidLoad() {
        super.viewDidLoad()
        dataArray = getTrips()
        tableView.delegate = self
        tableView.dataSource = self
        // Do any additional setup after loading the view.
    }

    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        //TripsMain
        let cell = tableView.dequeueReusableCell(withIdentifier: "TripsMain") as! TripsMainTableViewCell
        cell.setCell(data: dataArray[indexPath.row])
        
        return cell
    }
    //SeeTrip
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.performSegue(withIdentifier: "SeeTrip", sender: nil)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        //
        if "SeeTrip" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let vc = segue.destination as! SeeTripsViewController
                vc._id = dataArray[indexPath.row]["_id"] as! Int64
            }
        } else if "createTrip" == segue.identifier {
            
            let vc = segue.destination as! SeeTripsViewController
            vc._id = 0
            
        }
    }
}
