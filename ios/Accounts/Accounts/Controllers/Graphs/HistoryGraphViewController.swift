//
//  HistoryGraphViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 30/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
import Charts

typealias Pair = (Double, Double)

class HistoryGraphViewController: UIViewController,  UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet weak var lineChartView: LineChartView!
    @IBOutlet weak var tableView: UITableView!
    
    var dataArray:[[String:Any?]] = []
    var month:String? = nil
    var year:String? = nil
    var startYear:String = ""
    var startMonth:String = ""
    var startDay:Int = 0
    var moneda:Int64 = 1
    var total: Double = 0.0
    var now: String = ""
    let numberFormatter = NumberFormatter()
    
    let dollars1 = [10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0]
    let dollars2 = [20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0]
    let dollars3 = [30.0, 31.0, 32.0, 33.0, 34.0, 35.0, 36.0]
    let months = ["Jan" , "Feb", "Mar", "Apr", "May", "June", "July"]
    
    var indexMap:[String:CuentaData] = [:]
    var totales:[[String:Any?]] = []
    
    var arrayList:[String] = []
    override func viewDidLoad() {
        super.viewDidLoad()

        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        //tableView.rowHeight = 25.0
        tableView.delegate = self
        tableView.dataSource = self
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        
        let date:Date = Date()
        now = dateFormatter.string(from: date)
        let calendar = Calendar.current
        self.year = "\(calendar.component(.year, from: date))"
        self.month = "\(calendar.component(.month, from: date))"
        startYear = year!
        startMonth = month!
        startDay = calendar.component(.day, from:date)
        totales = getTotalesLineGraph()
        var x = 0
        for i in 0..<totales.count{
            let idTemp = totales[i]["_id"]
            let cantidad = totales[i][Totales.CurrentCantidad] as! Double
            let cuenta = totales[i][Totales.Cuenta] as! String
            if idTemp as? String != nil {
                let idt = totales[i]["_id"] as! String
                
                if idt.count > 3 {
                    let totalId = String(idt.suffix(3))
                    if indexMap[totalId] == nil{
                        indexMap[totalId] = CuentaData(name: "Total \(totalId)", id: totalId, selected: (x<=0), cantidadActual: cantidad, cantidad: cantidad, color: Utils.colors[x%Utils.colors.count])
                        arrayList.append(totalId)
                        x += 1
                    } else {
                        indexMap[totalId]!.cantidadActual += cantidad
                    }
                    if indexMap[idt] == nil {
                        indexMap[idt] = CuentaData(name: cuenta, id: idt, selected: (x<=0), cantidadActual: cantidad, cantidad: cantidad, color: Utils.colors[x%Utils.colors.count])
                        arrayList.append(idt)
                        x += 1
                    }
                }
            } else if idTemp as? Int64 != nil {
                let id = "\(idTemp as! Int64)"
                if(indexMap[id] == nil){
                    indexMap[id] = CuentaData(name: cuenta, id: id, selected: false,  cantidadActual: cantidad, cantidad: cantidad, color: Utils.colors[x%Utils.colors.count])
                    arrayList.append(id)
                    x += 1
                 }
            }
        }
        dataArray = getTotalsHistory(daily: true, diference: -1, now: now)
        setArrays()
        setChart()
        // Do any additional setup after loading the view.
    }

    func setArrays() {
        if (month == nil) {
            var mdf = 0
            var tmpYear = Int(year!)!
            while(Int(startYear) != tmpYear){
                tmpYear += 1
                mdf -= 1
            }
            dataArray = getTotalsHistory(daily: false, diference: mdf, now: now)
            if(dataArray.count <= 0){
                return
            }
            var lm = dataArray[0]["mo"] as! String
            for (_, value) in indexMap.enumerated() {
                value.value.entries = []
                value.value.cantidad = value.value.cantidadActual
                if year == startYear {
                    let m = Int(startMonth)!
                    value.value.entries.append(Pair(Double(m) + 0.1, Double(value.value.cantidadActual)))
                }
            }
            for i in 0..<dataArray.count{
                let idTemp = dataArray[i]["_id"]
                let cantidad = dataArray[i]["Cantidad"] as! Double
                let y = dataArray[i]["y"] as! String
                let m = dataArray[i]["mo"] as! String
                var id = ""
                var totalId = ""
                if idTemp as? String != nil {
                    id = idTemp as! String
                } else if idTemp as? Int64 != nil {
                    id = "\(idTemp as! Int64)"
                    if indexMap[id] == nil {
                        continue
                    }
                }
                if id.count > 3 {
                    totalId = String(id.suffix(3))
                    if indexMap[totalId] == nil {
                        continue
                    }
                    indexMap[totalId]!.cantidad = cantidad + indexMap[totalId]!.cantidad
                }
                
                if Int(lm) != Int(m) && year == y {
                    for (_, value) in indexMap.enumerated() {
                        if value.key == id || totalId == value.key {
                            value.value.entries.append(Pair(Double(Int(m)!), (Double(value.value.cantidad - cantidad))))
                        } else {
                            value.value.entries.append(Pair(Double(Int(m)!), Double(value.value.cantidad)))
                        }
                    }
                    lm = m
                }
            }
        } else {
            var mdf = 0
            var tmpMonth = Int(month!)!
            var tmpYear = Int(year!)!
            while(Int(startYear) != tmpYear || Int(startMonth) != tmpMonth){
                if(tmpMonth >= 12){
                    tmpMonth = 0
                    tmpYear += 1
                }
                tmpMonth += 1
                mdf -= 1
            }
            dataArray = getTotalsHistory(daily: true, diference: mdf, now: now)



            if dataArray.count <= 0 {
                return
            }
            var ld = dataArray[0]["dd"] as! String//c.getString(c.getColumnIndex("dd"))


            var firstDay = 32
            var lastDay = 0
            var x = 0
            
            for (_, value) in indexMap.enumerated() {
                value.value.entries = []
                value.value.cantidad = value.value.cantidadActual
                //value.end = false
                //value.last = lastDay
                //value.first = firstDay
                //if year == startYear {
                //    let m = Int(startMonth)!
                //    value.value.entries.append(Pair(Double(m) + 0.1, Double(value.value.cantidadActual)))
                //}
            }
            var tmpStart = startDay
            while Int(tmpStart) > Int(ld)! {
                for (_, value) in indexMap.enumerated() {
                    if year == startYear && month == startMonth {
                        value.value.entries.append(Pair(Double(tmpStart), Double(value.value.cantidadActual)))
                    }
                }
                tmpStart -= 1
            }

            for i in 0..<dataArray.count{
                let idTemp = dataArray[i]["_id"]
                let cantidad = dataArray[i]["Cantidad"] as! Double
                let y = dataArray[i]["y"] as! String
                let mo = dataArray[i]["mo"] as! String
                let d = dataArray[i]["dd"] as! String
                var id = ""
                var totalId = ""
                if idTemp as? String != nil {
                    id = idTemp as! String
                    if indexMap[id] == nil {
                        continue
                    }
                } else if idTemp as? Int64 != nil {
                    id = "\(idTemp as! Int64)"
                    if indexMap[id] == nil {
                        continue
                    }
                }
                if id.count > 3 {
                    totalId = String(id.suffix(3))
                    if indexMap[totalId] == nil {
                        continue
                    }
                    indexMap[totalId]!.cantidad = cantidad + indexMap[totalId]!.cantidad
                }
                indexMap[id]!.cantidad += cantidad + indexMap[id]!.cantidad
                if Int(y) == Int(year!) && Int(mo) == Int(month!) {
                    /*
                    if(firstDay > d.toInt())
                        firstDay = d.toInt()
                    if(lastDay < d.toInt()){
                        lastDay = d.toInt()
                    }
                    if(indexMap[id]!!.first > d.toInt()){
                        indexMap[id]!!.first = d.toInt()
                    }
                     
                     if(!indexMap[id]!!.end){
                         indexMap[id]!!.end = true
                         if(mdf != 0){
                             //indexMap[id]!!.entries.add(Entry(lastDay.toFloat()+1, indexMap[id]!!.cantidad.toFloat() - cantdidad.toFloat()))
                         } else {
                             if(d.toInt() == startDay){
                             //    indexMap[id]!!.entries.add(Entry(lastDay.toFloat() + 1, indexMap[id]!!.cantidadActual.toFloat()))
                             } else {
                             //    indexMap[id]!!.entries.add(Entry(lastDay.toFloat() + 1, indexMap[id]!!.cantidad.toFloat() - cantdidad.toFloat()))
                             }
                         }

                     }
                    */
                    if Int(ld) != Int(d) {
                        for (_, value) in indexMap.enumerated() {
                            if value.key == id || totalId == value.key {
                                value.value.entries.append(Pair(Double(Int(d)!), (Double(value.value.cantidad - cantidad))))
                            } else {
                                value.value.entries.append(Pair(Double(Int(d)!), Double(value.value.cantidad)))
                            }
                        }
                    }
                }
                
            }
            
            for (_, value) in indexMap.enumerated() {
                value.value.entries.append(Pair(0.0, Double(value.value.cantidad)))
            }
        }
    }
    func setChart(){
        var dataSets : [LineChartDataSet] = [LineChartDataSet]()
        for (_, value) in indexMap.enumerated(){
            if(value.value.selected) {
                var vals : [ChartDataEntry] = [ChartDataEntry]()
                value.value.entries.reverse()
                print(value.value.name + " --- " + value.value.id)
                for i in 0..<value.value.entries.count {
                    print("x: \(value.value.entries[i].0)")
                    vals.append(ChartDataEntry(x:value.value.entries[i].0, y: value.value.entries[i].1))
                }
                value.value.entries.reverse()
                let set: LineChartDataSet = LineChartDataSet(entries: vals, label: value.value.name)
                set.axisDependency = .left // Line will correlate with left axis values
                set.setColor(value.value.color.withAlphaComponent(0.5))
                set.setCircleColor(value.value.color)
                set.lineWidth = 2.0
                set.circleRadius = 4.0
                set.fillAlpha = 65 / 255.0
                set.fillColor = value.value.color
                set.highlightColor = UIColor.red
                set.drawCircleHoleEnabled = true
                dataSets.append(set)
            }
        }
        let lineChartData = LineChartData(dataSets: dataSets)
        self.lineChartView.data = lineChartData
        tableView.reloadData()
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return arrayList.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let r = indexPath.row
        let cuenta = indexMap[arrayList[r]]
        let cell = tableView.dequeueReusableCell(withIdentifier: "HistoryGraphCell") as! HistoryTableViewCell
        cell.setCell(cuenta: cuenta!)
        return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let r = indexPath.row
        let cuenta = indexMap[arrayList[r]]
        cuenta?.selected = !cuenta!.selected
        //setArrays()
        setChart()
        tableView.reloadData()
    }
}
//val cuenta: String, val id: String, var selected: Boolean, val entries:ArrayList<Pair<Float, Float>>,
//var cantidadActual:Double, var cantidad:Double, val color:Int, var end:Boolean, var last:Int, var first:Int, var cantLast:Double
class CuentaData {
    var name: String
    var id: String
    var selected:Bool
    var entries:[Pair]
    var cantidadActual:Double
    var cantidad:Double
    var color:UIColor
    init(name:String, id:String, selected:Bool, cantidadActual:Double, cantidad:Double, color:UIColor) {
        self.name = name
        self.id = id
        self.selected = selected
        self.cantidadActual = cantidadActual
        self.cantidad = cantidad
        entries = []
        self.color = color
    }
}
