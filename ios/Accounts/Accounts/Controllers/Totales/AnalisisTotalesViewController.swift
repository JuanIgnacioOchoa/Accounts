//
//  AnalisisTotalesViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 28/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class AnalisisTotalesViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var timeType: UITextField!
    @IBOutlet weak var timeLapse: UITextField!
    @IBOutlet weak var tableView: UITableView!
    let months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"]
    var monthYears:[String] = []
    var years:[String] = []
    let timeTypeData = ["Weekly", "Monthly", "Yearly"]
    let pickerTimeLapse = UIPickerView(), pickerTimeType = UIPickerView()
    let numberFormatter = NumberFormatter()
    var dataArray:[[String:Any?]] = []
    var selectedType = 1, oldSelectedType = 1, selectedLapse = 0, oldSelectedLapse = 0
    let lapseTag = 1, typeTag = 2
    var month:String? = nil, year:String? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        configurePickerView()
        pickerTimeType.selectRow(selectedType, inComponent: 0, animated: true)
        timeLapse.delegate = self
        timeType.delegate = self
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let d = getFirstDate()
        var date:Date = Date()
        let now = Date()
        if d != nil {
            date = dateFormatter.date(from: d!)!
        }
        var dateN = now
        while date < dateN{
            dateFormatter.dateFormat = "MM"
            let month = Int(dateFormatter.string(from: dateN))! - 1
            dateFormatter.dateFormat = "YYYY"
            let year = Int(dateFormatter.string(from: dateN))!
            monthYears.append(months[month] + " \(year)")
            dateN = Calendar.current.date(byAdding: .month, value: -1, to: dateN)!
        }
        dateN = now
        while date < dateN{
            dateFormatter.dateFormat = "YYYY"
            let year = Int(dateFormatter.string(from: dateN))!
            years.append("\(year)")
            dateN = Calendar.current.date(byAdding: .year, value: -1, to: dateN)!
        }
        timeType.text = timeTypeData[selectedType]
        if selectedLapse == 0 {
            timeLapse.text = monthYears[selectedLapse]
        } else if selectedLapse == 1 {
            timeLapse.text = monthYears[selectedLapse]
        } else {
            timeLapse.text = years[selectedLapse]
        }
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 28
        
        updateArrays()
        
    }
    func updateArrays(){
        dataArray = getReportesTotales(year: year, month: month)
        tableView.reloadData()
    }
    
    func configurePickerView(){
        let screenSize:CGRect = UIScreen.main.bounds
        
        var pickerRect = pickerTimeType.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height - 400// some desired value
        pickerTimeType.frame = pickerRect
        pickerTimeType.frame.size.height = 150
        pickerTimeType.frame.size.width = screenSize.width - 20
        pickerTimeType.backgroundColor = UIColor.yellow
        pickerTimeType.setValue(UIColor.black, forKey: "textColor")
        pickerTimeType.autoresizingMask = .flexibleWidth
        pickerTimeType.contentMode = .center
        pickerTimeType.delegate = self
        pickerTimeType.dataSource = self
        
        var toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        var doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        var spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        var cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        timeType.inputAccessoryView = toolbar
        timeType.inputView = pickerTimeType
        
        pickerRect = pickerTimeLapse.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height - 400// some desired value
        pickerTimeLapse.frame = pickerRect
        pickerTimeLapse.frame.size.height = 150
        pickerTimeLapse.frame.size.width = screenSize.width - 20
        pickerTimeLapse.backgroundColor = UIColor.yellow
        pickerTimeLapse.setValue(UIColor.black, forKey: "textColor")
        pickerTimeLapse.autoresizingMask = .flexibleWidth
        pickerTimeLapse.contentMode = .center
        pickerTimeLapse.delegate = self
        pickerTimeLapse.dataSource = self
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        timeLapse.inputAccessoryView = toolbar
        timeLapse.inputView = pickerTimeLapse
        
        pickerTimeLapse.tag = lapseTag
        pickerTimeType.tag = typeTag
        
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView.tag == typeTag {
            return timeTypeData.count
        } else if pickerView.tag == lapseTag{
            if selectedType == 0 {
                return monthYears.count
            } else if selectedType == 1{
                return monthYears.count
            } else {
                return years.count
            }
        }
        return 0
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView.tag == typeTag {
            return timeTypeData[row]
        } else if pickerView.tag == lapseTag{
            if selectedType == 0 {
                return monthYears[row]
            } else if selectedType == 1{
                return monthYears[row]
            } else {
                return years[row]
            }
        }
        return ""
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView.tag == typeTag {
            selectedType = row
            timeType.text = timeTypeData[row]
        } else if pickerView.tag == lapseTag {
            if selectedType == 0 {
                selectedLapse = row
                timeLapse.text = monthYears[row]
            } else if selectedType == 1{
                selectedLapse = row
                timeLapse.text = monthYears[row]
            } else {
                selectedLapse = row
                timeLapse.text = years[row]
            }
        }
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        return false
    }
    @objc func donePicker(_ btn: UIBarButtonItem){

        
        if(oldSelectedType != selectedType){
            pickerTimeLapse.reloadAllComponents()
            selectedLapse = 0
            if selectedType == 0 {
                timeLapse.text = monthYears[selectedLapse]
            } else if selectedType == 1 {
                timeLapse.text = monthYears[selectedLapse]
            } else {
                timeLapse.text = years[selectedLapse]
            }
        }
        switch selectedType {
        case 0:
            print("Weekly: ", monthYears[selectedLapse])
            break
        case 1:
            print("Monthly: ", monthYears[selectedLapse])
            
            let str = monthYears[selectedLapse] as String
            var start = str.index(str.startIndex, offsetBy: 0)
            var end = str.index(str.startIndex, offsetBy: 3)
            let monthS = str[start..<end]
            start = str.index(str.startIndex, offsetBy: 4)
            end = str.index(str.startIndex, offsetBy: 8)
            let year = str[start..<end]
            var month = "01"
            var m = 0
            while m < months.count {
                if months[m] == monthS {
                    month = "\(m + 1)"
                }
                m = m + 1
            }
            if month.count < 2{
                month = "0"+month
            }
            print(month)
            self.year = String(year)
            self.month = month
            break
        default:
            self.month = nil
            self.year = years[selectedLapse]
            break
        }
        updateArrays()
        oldSelectedType = selectedType
        oldSelectedLapse = selectedLapse
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedType = oldSelectedType
        selectedLapse = oldSelectedLapse
        timeType.text = timeTypeData[selectedType]
        switch selectedType {
        case 0:
            timeLapse.text = monthYears[selectedLapse]
            break
        case 1:
            timeLapse.text = monthYears[selectedLapse]
            break
        default:
            timeLapse.text = years[selectedLapse]
        }
        self.view.endEditing(true)
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        //let cell: UITableViewCell
        let cell = tableView.dequeueReusableCell(withIdentifier: "AnalisisTotales") as! AnalisisTotalesTableViewCell
        cell.setCell(data: dataArray[row])
        
        return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.performSegue(withIdentifier: "seeCuentasSegue", sender: nil)
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if "seeCuentasSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let vc = segue.destination as! SeeCuentaViewController
                vc._id = dataArray[indexPath.row]["_id"] as! Int64
                vc.month = month
                vc.year = year
            }
        }
    }
}
