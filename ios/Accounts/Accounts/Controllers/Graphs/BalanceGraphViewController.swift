//
//  BalanceGraphViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 30/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
import Charts
class BalanceGraphViewController: UIViewController, ChartViewDelegate {

    @IBOutlet weak var barChartView: BarChartView!
    
    let months = ["Jan", "K", "Mar", "Apr", "May", "Jun", "Jul", "Ago", "Sept", "Oct", "Nov", "Dic"]
    let unitsSold = [20.0, 4.0, 6.0, 3.0, 12.0, 20.0, 4.0, 6.0, 3.0, 12.0, 3.0, 12.0]
    let unitsBought = [10.0, 14.0, 5.0, 13.0, 2.0, 20.0, 4.0, 6.0, 3.0, 12.0, 3.0, 12.0]
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setChart(dataPoints: months, values: unitsSold, values2: unitsBought)
/*
            barChartView.delegate = self
            barChartView.noDataText = "You need to provide data for the chart."
            barChartView.chartDescription?.text = "sales vs bought "


            //legend
            let legend = barChartView.legend
            legend.enabled = true
            legend.horizontalAlignment = .right
            legend.verticalAlignment = .top
            legend.orientation = .vertical
            legend.drawInside = true
            legend.yOffset = 10.0;
            legend.xOffset = 10.0;
            legend.yEntrySpace = 0.0;


            let xaxis = barChartView.xAxis
            //xaxis.valueFormatter = axisFormatDelegate
            xaxis.drawGridLinesEnabled = true
            xaxis.labelPosition = .bottom
            xaxis.centerAxisLabelsEnabled = true
            xaxis.valueFormatter = IndexAxisValueFormatter(values:self.months)
            xaxis.granularity = 1


            let leftAxisFormatter = NumberFormatter()
            leftAxisFormatter.maximumFractionDigits = 1

            let yaxis = barChartView.leftAxis
            yaxis.spaceTop = 0.35
            yaxis.axisMinimum = 0
            yaxis.drawGridLinesEnabled = false

            barChartView.rightAxis.enabled = false
            //axisFormatDelegate = self

         setChart()
 */
    }
    
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
    
}
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
