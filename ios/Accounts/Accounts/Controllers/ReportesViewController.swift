//
//  ReportesViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/26/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class ReportesViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    @IBOutlet weak var currency: UITextField!
    @IBOutlet weak var timeType: UITextField!
    @IBOutlet weak var timeLapse: UITextField!
    @IBOutlet weak var contentView: UIView!
    @IBOutlet weak var incomeLbl: UILabel!
    @IBOutlet weak var outcomeLbl: UILabel!
    @IBOutlet weak var totalLbl: UILabel!
    @IBOutlet weak var porcentaLbl: UILabel!
    
    var viewController:ReportesDataViewController? = nil
    let months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"]
    var monthYears:[String] = []
    var years:[String] = []
    let timeTypeData = ["Weekly", "Monthly", "Yearly"]
    let pickerMoneda = UIPickerView()
    let pickerTimeLapse = UIPickerView()
    let pickerTimeType = UIPickerView()
    var dataSource = ["1:", "2:"]
    let currentViewControllerIndex = 0
    var currentIndex = 0
    var monedas:[[String:Any?]] = []
    var selectedMoneda = 0
    var oldSelectedMoneda = 0
    var selectedType = 1
    var oldSelectedType = 1
    var selectedLapse = 0
    var oldSelectedLapse = 0
    let numberFormatter = NumberFormatter()
    let monedaTag = 0
    let lapseTag = 1
    let typeTag = 2
    
    override func viewDidLoad() {
        super.viewDidLoad()

        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        
        monedas = getMonedas()
        
        let negativo = getGastoTotalByMonedaFromCurrentMonth(moneda: Int(monedas[selectedMoneda]["_id"] as! Int64)) as NSNumber
        outcomeLbl.text = numberFormatter.string(from: negativo)
        let positivo = getIngresoTotalByMonedaFromCurrentMonth(moneda: Int(monedas[selectedMoneda]["_id"] as! Int64)) as NSNumber
        incomeLbl.text = numberFormatter.string(from: positivo)
        let total = ((positivo as! Double) - (negativo as! Double)) as NSNumber

        var porcentaje:NSNumber = 0.0
        
        if Int(truncating: total) < 0 {
            porcentaje = NSNumber(value: ((total as! Double) / -(negativo as! Double)) * 100);
            totalLbl.textColor = UIColor.red
            porcentaLbl.textColor = UIColor.red
        } else if Int(truncating: total) > 0{
            porcentaje = NSNumber(value: ((total as! Double) / (positivo as! Double)) * 100);
            totalLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            porcentaLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
        totalLbl.text = numberFormatter.string(from: total)
        porcentaLbl.text = numberFormatter.string(from: porcentaje)
        configurePickerView()
        pickerTimeType.selectRow(selectedType, inComponent: 0, animated: true)
        currency.delegate = self
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
        configurePageViewController()
        
        currency.text = monedas[selectedMoneda][Moneda.Moneda] as? String
        timeType.text = timeTypeData[selectedType]
        if selectedLapse == 0 {
            timeLapse.text = monthYears[selectedLapse]
        } else if selectedLapse == 1 {
            timeLapse.text = monthYears[selectedLapse]
        } else {
            timeLapse.text = years[selectedLapse]
        }
    }
    
    func configurePageViewController(){
        guard let pageViewController = storyboard?.instantiateViewController(withIdentifier: "PageControllerReportes")
            as? UIPageViewController else {
                return
        }
        pageViewController.delegate = self
        pageViewController.dataSource = self
        
        addChild(pageViewController)
        pageViewController.didMove(toParent: self)
        
        pageViewController.view.translatesAutoresizingMaskIntoConstraints = false
        
        contentView.addSubview(pageViewController.view)
        NSLog("Hola02")
        let views: [String: Any] = ["pageView": pageViewController.view as Any]
        
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "H:|-0-[pageView]-0-|",
                                                                 options: NSLayoutConstraint.FormatOptions(rawValue: 0),
                                                                 metrics: nil,
                                                                 views: views))
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:|-0-[pageView]-0-|",
                                                                 options: NSLayoutConstraint.FormatOptions(rawValue: 0),
                                                                 metrics: nil,
                                                                 views: views))
        
        guard let startingViewController = detailViewControllerAt(index: currentViewControllerIndex) else {
            NSLog("Hola2")
            return
        }
        NSLog("Hola03")
        pageViewController.setViewControllers([startingViewController], direction: .forward, animated: true)
    }

    func detailViewControllerAt(index: Int) -> ReportesDataViewController? {
        if index >= dataSource.count || dataSource.count == 0 {
            return nil
        }
        
        guard let dataViewController = storyboard?.instantiateViewController(withIdentifier: String(describing: ReportesDataViewController.self))
            as? ReportesDataViewController else {
            return nil
        }
        viewController = dataViewController
        dataViewController.index = index
        return dataViewController
    }

    func configurePickerView(){
        let screenSize:CGRect = UIScreen.main.bounds
        var pickerRect = pickerMoneda.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height - 400// some desired value
        pickerMoneda.frame = pickerRect
        pickerMoneda.frame.size.height = 150
        pickerMoneda.frame.size.width = screenSize.width - 20
        pickerMoneda.backgroundColor = UIColor.yellow
        pickerMoneda.setValue(UIColor.black, forKey: "textColor")
        pickerMoneda.autoresizingMask = .flexibleWidth
        pickerMoneda.contentMode = .center
        pickerMoneda.delegate = self
        pickerMoneda.dataSource = self
        
        var toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        var doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        var spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        var cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        currency.inputAccessoryView = toolbar
        currency.inputView = pickerMoneda
        
        pickerRect = pickerTimeType.frame
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
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
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
        
        pickerMoneda.tag = monedaTag
        pickerTimeLapse.tag = lapseTag
        pickerTimeType.tag = typeTag
        
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView.tag == monedaTag {
            return monedas.count
        } else if pickerView.tag == typeTag {
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
        if pickerView.tag == monedaTag {
            return monedas[row][Moneda.Moneda] as? String
        } else if pickerView.tag == typeTag {
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
        if pickerView.tag == monedaTag {
            selectedMoneda = row
            currency.text = monedas[row][Moneda.Moneda] as? String
            //return dataArrayMoneda.count
        } else if pickerView.tag == typeTag {
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
    
    func getSubviewsOfView(v:UIView) ->  [ReportesDataViewController]{
        var circleArray = [ReportesDataViewController]()

        for subview in v.subviews as! [UIView] {
            circleArray += getSubviewsOfView(v: subview)

            if subview is ReportesDataViewController {
                circleArray.append(subview as! ReportesDataViewController)
            }
        }

        return circleArray
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
        if viewController != nil{
            viewController?.moneda = monedas[selectedMoneda]["_id"] as! Int64
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
                viewController?.year = String(year)
                viewController?.month = month
                break
            default:
                viewController?.month = nil
                viewController?.year = years[selectedLapse]
                break
            }
            viewController?.updateArrays()
        }
        oldSelectedMoneda = selectedMoneda
        oldSelectedType = selectedType
        oldSelectedLapse = selectedLapse
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedMoneda = oldSelectedMoneda
        selectedType = oldSelectedType
        selectedLapse = oldSelectedLapse
        currency.text = monedas[selectedMoneda][Moneda.Moneda] as? String
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
    
    
}

extension ReportesViewController: UIPageViewControllerDelegate, UIPageViewControllerDataSource{
    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return currentViewControllerIndex
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return dataSource.count
    }
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        let dataViewController = viewController as? ReportesDataViewController
        
        guard var currentIndex = dataViewController?.index else {
            return nil
        }
        if currentIndex == 0{
            return nil
        }
        
        currentIndex -= 1
        return detailViewControllerAt(index: currentIndex)
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        let dataViewController = viewController as? ReportesDataViewController
        
        guard var currentIndex = dataViewController?.index else {
            return nil
        }
        if currentIndex == dataSource.count{
            return nil
        }
        
        currentIndex += 1
        return detailViewControllerAt(index: currentIndex)
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed {
            if let currentViewController = pageViewController.viewControllers![0] as? ReportesDataViewController {
                let index = currentViewController.index
                self.currentIndex = index!
            }
        }
    }
}


