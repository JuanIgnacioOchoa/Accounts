//
//  MovimientosViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 10/23/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MovimientosViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    @IBOutlet weak var contentView: UIView!
    @IBOutlet weak var timeLapse: UITextField!
    @IBOutlet weak var timeType: UITextField!
    @IBOutlet weak var currency: UITextField!
    @IBOutlet weak var pageTitle1: UILabel!
    @IBOutlet weak var pageTitle2: UILabel!
    @IBOutlet weak var selectorPage1: UILabel!
    @IBOutlet weak var selectorPage2: UILabel!
    
    lazy var viewControllers:[UIViewController] = {
        let first = storyboard?.instantiateViewController(withIdentifier: String(describing: MovdataViewController.self))
        let second = storyboard?.instantiateViewController(withIdentifier: String(describing: AnalisisViewController.self))
        return [first!, second!]
    }()
    
    let months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"]
    var monthYears:[String] = []
    var years:[String] = []
    let timeTypeData = ["Weekly", "Monthly", "Yearly"]
    let pickerMoneda = UIPickerView(), pickerTimeLapse = UIPickerView(), pickerTimeType = UIPickerView()
    var monedas:[[String:Any?]] = []
    var selectedMoneda = 0, oldSelectedMoneda = 0, selectedType = 1, oldSelectedType = 1, selectedLapse = 0, oldSelectedLapse = 0
    let monedaTag = 0, lapseTag = 1, typeTag = 2
    var currentIndex:Int = 0
    var pageView:UIPageViewController? = nil
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        pageTitle1.text = "Movimientos"
        pageTitle2.text = "Analisis"
        pageTitle1.textColor = UIColor.blue
        pageTitle2.textColor = UIColor.black
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction1))
        pageTitle1.addGestureRecognizer(tap)
        pageTitle1.isUserInteractionEnabled = true
        //selectorPage1.addGestureRecognizer(tap)
        //selectorPage1.isUserInteractionEnabled = true
        let tap2 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction2))
        pageTitle2.addGestureRecognizer(tap2)
        pageTitle2.isUserInteractionEnabled = true
        //selectorPage2.addGestureRecognizer(tap)
        monedas = getMonedas()
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
        if monedas.count > 0 {
            currency.text = monedas[selectedMoneda][Moneda.Moneda] as? String
        }
        timeType.text = timeTypeData[selectedType]
        if selectedLapse == 0 {
            timeLapse.text = monthYears[selectedLapse]
        } else if selectedLapse == 1 {
            timeLapse.text = monthYears[selectedLapse]
        } else {
            timeLapse.text = years[selectedLapse]
        }
        // Do any additional setup after loading the view.
        configurePageViewController()
    }
    
    @objc
    func tapFunction1(sender:UITapGestureRecognizer) {
        goToPreviousPage()
    }
    @objc
    func tapFunction2(sender:UITapGestureRecognizer) {
        goToNextPage()
    }
    func configurePageViewController(){
        guard let pageViewController = storyboard?.instantiateViewController(withIdentifier: "PageControllerMov")
            as? UIPageViewController else {
                return
        }
        pageViewController.delegate = self
        pageViewController.dataSource = self
        addChild(pageViewController)
        pageViewController.didMove(toParent: self)
        
        pageViewController.view.translatesAutoresizingMaskIntoConstraints = false
        self.pageView = pageViewController
        contentView.addSubview(pageViewController.view)
        
        let views: [String: Any] = ["pageView": pageViewController.view as Any]
        
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "H:|-0-[pageView]-0-|",
                                                                 options: NSLayoutConstraint.FormatOptions(rawValue: 0),
                                                                 metrics: nil,
                                                                 views: views))
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:|-0-[pageView]-0-|",
                                                                 options: NSLayoutConstraint.FormatOptions(rawValue: 0),
                                                                 metrics: nil,
                                                                 views: views))
        
        guard let startingViewController = viewControllers.first else {
            return
        }
        pageViewController.setViewControllers([startingViewController], direction: .forward, animated: true)
    }
/*
    func detailViewControllerAt(index: Int) -> UIViewController? {
        if index >= dataSource.count || dataSource.count == 0 {
            return nil
        }
        if(index == 0) {
            //dataViewController.totalesVisible = false
            //dataViewController.movimientosVisible = true
            guard let dataViewController = storyboard?.instantiateViewController(withIdentifier: String(describing: MovdataViewController.self))
                as? MovdataViewController else {
                return nil
            }
            currency.isUserInteractionEnabled = false
            currency.textColor = UIColor.gray
            movDataViewController = dataViewController
            return dataViewController
            //return dataViewController
        } else {
            //dataViewController.totalesVisible = true
            //dataViewController.movimientosVisible = false
            guard let dataViewController = storyboard?.instantiateViewController(withIdentifier: String(describing: AnalisisViewController.self))
                as? AnalisisViewController else {
                return nil
            }
            currency.isUserInteractionEnabled = true
            currency.textColor = UIColor.black
            return dataViewController
            //return dataViewController
        }
        
    }
*/
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
        guard let movData = viewControllers.first as? MovdataViewController else { return }
        guard let analisis = viewControllers[1] as? AnalisisViewController else { return }
        
        movData.moneda = monedas[selectedMoneda]["_id"] as! Int64
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
            movData.year = String(year)
            analisis.year = String(year)
            movData.month = month
            analisis.month = month
            break
        default:
            movData.month = nil
            analisis.month = nil
            movData.year = years[selectedLapse]
            analisis.year = years[selectedLapse]
            break
        }
        movData.updateArrays()
        analisis.updateArrays()
        oldSelectedMoneda = selectedMoneda
        oldSelectedType = selectedType
        oldSelectedLapse = selectedLapse
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedMoneda = oldSelectedMoneda
        selectedType = oldSelectedType
        selectedLapse = oldSelectedLapse
        if monedas.count > 0 {
            currency.text = monedas[selectedMoneda][Moneda.Moneda] as? String
        }
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
    
    func goToNextPage(){
        if self.currentIndex == 1 { return }
        self.currentIndex = 1
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[self.currentIndex]], direction: .forward, animated: true, completion: nil)
        pageTitle2.textColor = UIColor.blue
        pageTitle1.textColor = UIColor.black
    }


    func goToPreviousPage(){
        if(self.currentIndex == 0) { return }
        self.currentIndex = 0
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[self.currentIndex]], direction: .reverse, animated: true, completion: nil)
        pageTitle1.textColor = UIColor.blue
        pageTitle2.textColor = UIColor.black
        currentIndex = 0
    }

}
extension MovimientosViewController: UIPageViewControllerDelegate, UIPageViewControllerDataSource{

    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return 0
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return 2
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        guard let vcIndex = viewControllers.firstIndex(of: viewController) else { return nil }
        
        let previousIndex = vcIndex - 1
        
        guard previousIndex >= 0 else { return nil }
        
        guard viewControllers.count > previousIndex else { return nil }
        
        return viewControllers[previousIndex]
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        guard let vcIndex = viewControllers.firstIndex(of: viewController) else { return nil }
        
        let nextIndex = vcIndex + 1
        
        guard nextIndex < viewControllers.count else { return nil }
        
        return viewControllers[nextIndex]
    }
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed{
            if self.currentIndex == 0 {
                pageTitle2.textColor = UIColor.blue
                pageTitle1.textColor = UIColor.black
                currentIndex = 1
            } else if self.currentIndex == 1 {
                pageTitle1.textColor = UIColor.blue
                pageTitle2.textColor = UIColor.black
                currentIndex = 0
            }
        }
    }
    /*
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed {
            if let currentViewController = pageViewController.viewControllers![0] as? ReportesDataViewController {
                let index = currentViewController.index
                self.currentIndex = index!
            }
        }
    }

    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed{
            if let _ = currentViewController as? MovdataViewController  {
                pageTitle2.textColor = UIColor.blue
                pageTitle1.textColor = UIColor.black
                currentIndex = 1
            } else if let _ = currentViewController as? AnalisisViewController{
                pageTitle1.textColor = UIColor.blue
                pageTitle2.textColor = UIColor.black
                currentIndex = 0
            }
            currentViewController = detailViewControllerAt(index: currentIndex)
        }
    }
 */
}
