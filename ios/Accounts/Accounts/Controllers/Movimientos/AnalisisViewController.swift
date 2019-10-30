//
//  AnalisisViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 24/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class AnalisisViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var incomeLbl: UILabel!
    @IBOutlet weak var outcomeLbl: UILabel!
    @IBOutlet weak var savingsLbl: UILabel!
    @IBOutlet weak var porcLbl: UILabel!
    var month:String? = nil, year:String? = nil
    var moneda:Int64 = 1
    let numberFormatter = NumberFormatter()
    var dataArray:[[String:Any?]] = []

    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 28
        updateArrays()
        
    }
    func updateArrays(){
        if moneda <= 0 { return }
        var income = 0.0
        var outcome = 0.0
        outcome = getGastoTotalByMoneda(moneda: moneda, year: year, month: month)
        income = getIngresoTotalByMoneda(moneda: moneda, year: year, month: month)
        dataArray = getReportesMotives(idMoneda: moneda, year: year, month: month)
        let ganancia = income + outcome;
        outcomeLbl.text = numberFormatter.string(from: NSNumber(value: outcome))
        incomeLbl.text = numberFormatter.string(from: NSNumber(value: income))
        savingsLbl.text = numberFormatter.string(from: NSNumber(value: ganancia))
        var porc = 0.0
        if ganancia <= 0{
            porc = ganancia / -outcome * 100;
            porcLbl.textColor = UIColor.red
            savingsLbl.textColor = UIColor.red
        } else {
            porc = ganancia / income * 100
            porcLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            savingsLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
        porcLbl.text = numberFormatter.string(from: NSNumber(value: porc))!+"%"
        tableView.reloadData()
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        //let cell: UITableViewCell
        let cell = tableView.dequeueReusableCell(withIdentifier: "AnalisisMotives") as! AnalisisMotivesTableViewCell
        cell.setCell(data: dataArray[row])
        
        return cell
    }
}
