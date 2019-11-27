//
//  GastoGraphViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 30/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
import Charts

class GastoGraphViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    

    @IBOutlet weak var pieChartView: PieChartView!
    @IBOutlet weak var tableView: UITableView!
    var dataArray:[[String:Any?]] = []
    var month:String? = nil
    var year:String? = nil
    var moneda:Int64 = 1
    var total: Double = 0.0
    let numberFormatter = NumberFormatter()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        tableView.rowHeight = 25.0
        tableView.delegate = self
        tableView.dataSource = self
        
        let date:Date = Date()
        let calendar = Calendar.current
        self.year = "\(calendar.component(.year, from: date))"
        self.month = "\(calendar.component(.month, from: date))"
        //dataArray = getReportesGastoMotives(idMoneda: moneda, year: year, month: month)
        setChart()
        //setChart(dataPoints: months, values: unitsSold)
    }
    func setChart(){
    //func setChart(dataPoints: [String], values: [Double]) {
        dataArray = getReportesGastoMotives(idMoneda: moneda, year: year, month: month)
        tableView.reloadData()
        let formatter = BarChartFormatter2()
        formatter.setValues(values: dataArray)
        let xaxis:XAxis = XAxis()

        var dataEntries: [ChartDataEntry] = []
        total = getGastoTotalByMoneda(moneda: moneda, year: year, month: month)
        let quote = "Total:\n\(numberFormatter.string(from: NSNumber(value: total))!)"
        let paragraph = NSMutableParagraphStyle()
        paragraph.alignment = .center
        let font = UIFont.systemFont(ofSize: 18)
        let attributes = [NSAttributedString.Key.font: font, NSAttributedString.Key.paragraphStyle: paragraph]
        pieChartView.centerAttributedText = NSAttributedString(string: quote, attributes: attributes)
        for i in 0..<dataArray.count {
            let g = dataArray[i]["Gasto"] as! Double
            let m = dataArray[i]["Motivo"] as! String
            let dataEntry = PieChartDataEntry(value: g/total, label: m)
            dataEntries.append(dataEntry)
        }
            
        let pieChartDataSet = PieChartDataSet(entries: dataEntries)
        
        let pieChartData = PieChartData(dataSet: pieChartDataSet)
        pieChartView.data = pieChartData
        
        pieChartDataSet.colors = Utils.colors
        
        xaxis.valueFormatter = formatter
        pieChartView.legend.enabled = false


         //background color
         //pieChartView.backgroundColor = UIColor(red: 189/255, green: 195/255, blue: 199/255, alpha: 1)

         //chart animation
         pieChartView.animate(xAxisDuration: 1.5, yAxisDuration: 1.5, easingOption: .linear)
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let data = dataArray[indexPath.row]
        //let cell: UITableViewCell
        let g = data["Gasto"] as! Double
        let m = data["Motivo"] as! String
        let cell = tableView.dequeueReusableCell(withIdentifier: "GtaphMotives") as! GraphMotivesTableViewCell
        cell.setCell(motivo: m, cantidad: g, porcentaje: (g/total), colorc: Utils.colors[indexPath.row%Utils.colors.count])
        
        return cell
    }
}
@objc(BarChartFormatter2)
public class BarChartFormatter2: NSObject, IAxisValueFormatter
{
    var val:[[String:Any?]] = []

    public func stringForValue(_ value: Double, axis: AxisBase?) -> String
    {
        if Int(value) >= val.count{
            return ""
        }
        return val[Int(value)]["Motivo"] as! String
    }

    public func setValues(values: [[String:Any?]])
    {
        self.val = values
    }
}

