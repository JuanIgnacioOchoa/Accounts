//
//  ReportesDataViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/26/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class ReportesDataViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    
    @IBOutlet weak var titleTxt: UILabel!
    var index: Int?
    var month:String? = "09"
    var year:String = "2019"
    var moneda:Int64 = 1
    var dataArrayMotives:[[String:Any?]] = []
    var dataArrayTotales:[[String:Any?]] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.delegate = self
        tableView.dataSource = self
        dataArrayMotives = getSumByMotivesMonthly(idMoneda: moneda, month: month!, year: year)
        dataArrayTotales = getTotalesCuentasByMonth(year: year, month: month!)
        if index == 0{
            titleTxt.text = "Motives"
        } else {
            titleTxt.text = "Account"
        }
        // Do any additional setup after loading the view.
        
    }
    
    func updateArrays(){
        if month == nil {
            dataArrayMotives = getSumByMotivesYearly(idMoneda: moneda, year: year)
            dataArrayTotales = getTotalesCuentasByYear(year: year)
        } else {
            dataArrayMotives = getSumByMotivesMonthly(idMoneda: moneda, month: month!, year: year)
            dataArrayTotales = getTotalesCuentasByMonth(year: year, month: month!)
        }
        tableView.reloadData()
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if index == 0 {
            return dataArrayMotives.count
        } else {
            return dataArrayTotales.count
        }
        return 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        //let data = dataArray[indexPath.row]
        let row = indexPath.row
        //let cell: UITableViewCell
        let cell = tableView.dequeueReusableCell(withIdentifier: "ReportesTableCell") as! ReportesTableViewCell
        if(index == 0){
            cell.setCell(data: dataArrayMotives[row])
        } else {
            cell.setCell(data: dataArrayTotales[row])
        }
        
        return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //seeCuentasSegue
        //let row = indexPath.row
    }
    
}
