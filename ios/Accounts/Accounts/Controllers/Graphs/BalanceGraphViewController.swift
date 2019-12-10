//
//  BalanceGraphViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 30/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
import Charts
class BalanceGraphViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, ChartViewDelegate {

    //@IBOutlet weak var barChartView: BarChartView!
    @IBOutlet var pieChart: PieChartView!
    @IBOutlet var tableView: UITableView!
    
    //let months = ["Jan", "K", "Mar", "Apr", "May", "Jun", "Jul", "Ago", "Sept", "Oct", "Nov", "Dic"]
    //let unitsSold = [20.0, 4.0, 6.0, 3.0, 12.0, 20.0, 4.0, 6.0, 3.0, 12.0, 3.0, 12.0]
    //let unitsBought = [10.0, 14.0, 5.0, 13.0, 2.0, 20.0, 4.0, 6.0, 3.0, 12.0, 3.0, 12.0]
    
    var dataArray:[[String:Any?]] = []
    var month:String? = nil
    var year:String? = nil
    var moneda:Int64 = 1
    var ganancia: Double = 0.0
    let numberFormatter = NumberFormatter()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        //setChart(dataPoints: months, values: unitsSold, values2: unitsBought)
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        tableView.rowHeight = 25.0
        tableView.delegate = self
        tableView.dataSource = self
        pieChart.delegate = self
        
        let date:Date = Date()
        let calendar = Calendar.current
        self.year = "\(calendar.component(.year, from: date))"
        self.month = "\(calendar.component(.month, from: date))"
        //dataArray = getReportesGastoMotives(idMoneda: moneda, year: year, month: month)
        setChart()
    }
    
    func setChart(){
    //func setChart(dataPoints: [String], values: [Double]) {
        //dataArray = getReportesGastoMotives(idMoneda: moneda, year: year, month: month)
        let gasto:Double = getGastoTotalByMoneda(moneda: moneda, year: year, month: month)
        let ingreso:Double = getIngresoTotalByMoneda(moneda: moneda, year: year, month: month)
 
        dataArray.append(["Gasto": gasto, "Motivo": "Gasto"])
        dataArray.append(["Gasto": ingreso, "Motivo": "Ingreso"])
        
        ganancia = ingreso + gasto
        var porcentaje = 0.0
        if ganancia <= 0 {
            porcentaje = ganancia / gasto * 100
            //pieChart!!.setCenterTextColor()
        } else {
            porcentaje = ganancia / ingreso * 100
            //pieChart!!.setCenterTextColor(colorsGastoIn[0])
        }
        tableView.reloadData()
        let formatter = BarChartFormatter2()
        formatter.setValues(values: dataArray)
        let xaxis:XAxis = XAxis()

        var dataEntries: [ChartDataEntry] = []
        //total = getGastoTotalByMoneda(moneda: moneda, year: year, month: month)
        let quote = "Savings:\n\(numberFormatter.string(from: NSNumber(value: ganancia))!)"
        let paragraph = NSMutableParagraphStyle()
        paragraph.alignment = .center
        let font = UIFont.systemFont(ofSize: 18)
        let attributes = [NSAttributedString.Key.font: font, NSAttributedString.Key.paragraphStyle: paragraph]
        let sTot = (gasto * -1) + ingreso
        pieChart.centerAttributedText = NSAttributedString(string: quote, attributes: attributes)
        for i in 0..<dataArray.count {
            var g = dataArray[i]["Gasto"] as! Double
            let m = dataArray[i]["Motivo"] as! String
            if g < 0 {
                g = g * -1
            }
            let dataEntry = PieChartDataEntry(value: g/sTot, label: m)
            dataEntries.append(dataEntry)
        }
            
        let pieChartDataSet = PieChartDataSet(entries: dataEntries)
        
        let pieChartData = PieChartData(dataSet: pieChartDataSet)
        pieChart.data = pieChartData
        
        pieChartDataSet.colors = Utils.colors
        
        xaxis.valueFormatter = formatter
        pieChart.legend.enabled = false


         //background color
         //pieChartView.backgroundColor = UIColor(red: 189/255, green: 195/255, blue: 199/255, alpha: 1)

         //chart animation
         pieChart.animate(xAxisDuration: 1.5, yAxisDuration: 1.5, easingOption: .linear)
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
        cell.setCell(motivo: m, cantidad: g, porcentaje: (g/ganancia), colorc: Utils.colors[indexPath.row%Utils.colors.count])
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let row:Double = Double(indexPath.row)
        pieChart.highlightValue(Highlight(x: row, y: 0.0, dataSetIndex: 0))
    }
    
    func chartValueSelected(_ chartView: ChartViewBase, entry: ChartDataEntry, highlight: Highlight) {
        print(highlight)
        let row:Int = Int(highlight.x)
        let indexPath = IndexPath(row: row, section: 0)
        tableView.selectRow(at: indexPath, animated: true, scrollPosition: .middle)
    }
    /*
    func setChart(dataPoints: [String], values: [Double], values2:[Double])
    {
        let formatter = BarChartFormatter()
        formatter.setValues(values: dataPoints)
        let xaxis:XAxis = XAxis()


        barChartView.noDataText = "You need to provide data for the chart."
        var dataEntries: [BarChartDataEntry] = []
        var dataEntries1: [BarChartDataEntry] = []

        for i in 0..<dataPoints.count
        {
            let dataEntry = BarChartDataEntry(x: Double(i), y: values[i])
            dataEntries.append(dataEntry)
            
            let dataEntry1 = BarChartDataEntry(x: Double(i) , y: values2[i])
            dataEntries1.append(dataEntry1)
        }

        let chartDataSet = BarChartDataSet(entries: dataEntries, label: "Unit Sold")
        let chartDataSet1 = BarChartDataSet(entries: dataEntries1, label: "Unit Bought")

        let dataSets: [BarChartDataSet] = [chartDataSet,chartDataSet1]
        
        chartDataSet.colors = [UIColor(red: 230/255, green: 126/255, blue: 34/255, alpha: 1)]

        let chartData = BarChartData(dataSets: dataSets)
        
        let groupSpace = 0.35
        let barSpace = 0.025
        let barWidth = 0.3
        // (0.3 + 0.05) * 2 + 0.3 = 1.00 -> interval per "group"

        let groupCount = self.months.count
        let startYear = 0
        
        chartData.barWidth = barWidth;
        barChartView.xAxis.axisMinimum = Double(startYear)
        let gg = chartData.groupWidth(groupSpace: groupSpace, barSpace: barSpace)
        print("Groupspace: \(gg)")
        barChartView.xAxis.axisMaximum = Double(startYear) + gg * Double(groupCount) //- 1
        chartData.groupBars(fromX: Double(startYear), groupSpace: groupSpace, barSpace: barSpace)
        //chartData.groupWidth(groupSpace: groupSpace, barSpace: barSpace)
        barChartView.notifyDataSetChanged()

        let limitLine = ChartLimitLine(limit: 10, label: "")
        limitLine.lineColor = UIColor.black.withAlphaComponent(0.3)
        limitLine.lineWidth = 5

        barChartView.leftAxis.addLimitLine(limitLine)
        
        xaxis.valueFormatter = formatter
        barChartView.xAxis.labelPosition = .topInside
        barChartView.xAxis.drawGridLinesEnabled = true
        barChartView.xAxis.valueFormatter = xaxis.valueFormatter
        barChartView.chartDescription?.enabled = false
        barChartView.legend.enabled = true
        barChartView.rightAxis.enabled = false
        barChartView.data = chartData






         //background color
         barChartView.backgroundColor = UIColor(red: 189/255, green: 195/255, blue: 199/255, alpha: 1)

         //chart animation
         barChartView.animate(xAxisDuration: 1.5, yAxisDuration: 1.5, easingOption: .linear)
    }
    */
}
/*
@objc(BarChartFormatter)
public class BarChartFormatter: NSObject, IAxisValueFormatter
{
    var names = [String]()

    public func stringForValue(_ value: Double, axis: AxisBase?) -> String
    {
        if Int(value) >= names.count{
            return "\(value)"
        }
        return names[Int(value)]
    }

    public func setValues(values: [String])
    {
        self.names = values
    }
}
*/
