//
//  MainGraphsViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 30/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
//PageControllerGraphs
class MainGraphsViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {


    @IBOutlet weak var page1: UILabel!
    @IBOutlet weak var page2: UILabel!
    @IBOutlet weak var page3: UILabel!
    @IBOutlet weak var page4: UILabel!
    @IBOutlet weak var contentView: UIView!
    @IBOutlet weak var currency: UITextField!
    @IBOutlet weak var timeType: UITextField!
    @IBOutlet weak var timeLapse: UITextField!
    
    /*
    var dataArrayTotales:[[String:Any?]]?
    */
    lazy var viewControllers:[UIViewController] = {
        let balance = storyboard?.instantiateViewController(withIdentifier: String(describing: BalanceGraphViewController.self))
        balance?.view.tag = 0
        let gasto = storyboard?.instantiateViewController(withIdentifier: String(describing: GastoGraphViewController.self))
        gasto?.view.tag = 1
        let ingreso = storyboard?.instantiateViewController(withIdentifier: String(describing: IngresoGraphViewController.self))
        ingreso?.view.tag = 2
        let history = storyboard?.instantiateViewController(withIdentifier: String(describing: HistoryGraphViewController.self))
        history?.view.tag = 3
        return [balance!, gasto!, ingreso!, history!]
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
    var nextIndex:Int = 0
    var pageView:UIPageViewController? = nil
    var transition:Bool = false
    
    override func viewDidLoad() {
        monedas = getMonedas()
        page1.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
        page4.textColor = UIColor.black
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction1))
        page1.addGestureRecognizer(tap)
        page1.isUserInteractionEnabled = true
        let tap2 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction2))
        page2.addGestureRecognizer(tap2)
        page2.isUserInteractionEnabled = true
        let tap3 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction3))
        page3.addGestureRecognizer(tap3)
        page3.isUserInteractionEnabled = true
        let tap4 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction4))
        page4.addGestureRecognizer(tap4)
        page4.isUserInteractionEnabled = true
        
        //dataArrayTotales = getTotales(inactivos: false)
        
        configurePageViewController()
        
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
        
    }
    @objc
    func tapFunction1(sender:UITapGestureRecognizer) {
        let localIndex = 0
        if localIndex == currentIndex { return }
        goToPreviousPage(index: localIndex)
        page1.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
        page4.textColor = UIColor.black
    }
    @objc
    func tapFunction2(sender:UITapGestureRecognizer) {
        let localIndex = 1
        if localIndex == currentIndex { return }
        if currentIndex < localIndex {
            goToNextPage(index: localIndex)
        } else {
            goToPreviousPage(index: localIndex)
        }
        page2.textColor = UIColor.blue
        page1.textColor = UIColor.black
        page3.textColor = UIColor.black
        page4.textColor = UIColor.black
    }
    @objc
    func tapFunction3(sender:UITapGestureRecognizer) {
        let localIndex = 2
        if localIndex == currentIndex { return }
        if currentIndex < localIndex {
            goToNextPage(index:localIndex)
        } else {
            goToPreviousPage(index:localIndex)
        }
        page3.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page1.textColor = UIColor.black
        page4.textColor = UIColor.black
    }
    @objc
    func tapFunction4(sender:UITapGestureRecognizer) {
        let localIndex = 3
        if localIndex == currentIndex { return }
        goToNextPage(index: localIndex)
        page4.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
        page1.textColor = UIColor.black
    }
    
    func configurePageViewController(){
        guard let pageViewController = storyboard?.instantiateViewController(withIdentifier: "PageControllerGraphs")
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
    
    func configurePickerView(){
        let screenSize:CGRect = UIScreen.main.bounds
        var pickerRect = pickerMoneda.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3 // some desired value
        pickerMoneda.frame = pickerRect
        pickerMoneda.frame.size.height = screenSize.size.height/3
        pickerMoneda.frame.size.width = screenSize.width - 20
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
        pickerRect.origin.y = screenSize.size.height/3 // some desired value
        pickerTimeType.frame = pickerRect
        pickerTimeType.frame.size.height = screenSize.size.height/3
        pickerTimeType.frame.size.width = screenSize.width - 20
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
        pickerRect.origin.y = screenSize.size.height/3 // some desired value
        pickerTimeLapse.frame = pickerRect
        pickerTimeLapse.frame.size.height = screenSize.size.height/3
        pickerTimeLapse.frame.size.width = screenSize.width - 20
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
            pickerTimeLapse.selectRow(selectedLapse, inComponent: 0, animated: true)
        }
        //guard let movData = viewControllers.first as? MovdataViewController else { return }
        //guard let analisis = viewControllers[1] as? AnalisisViewController else { return }
        guard let balance = viewControllers[0] as? BalanceGraphViewController else { return }
        guard let gasto = viewControllers[1] as? GastoGraphViewController else { return }
        guard let ingreso = viewControllers[2] as? IngresoGraphViewController else { return }
        guard let history = viewControllers[3] as? HistoryGraphViewController else { return }
        
        
        //balance.moneda = monedas[selectedMoneda]["_id"] as! Int64
        gasto.moneda = monedas[selectedMoneda]["_id"] as! Int64
        ingreso.moneda = monedas[selectedMoneda]["_id"] as! Int64
        //movData.moneda = monedas[selectedMoneda]["_id"] as! Int64
        
        
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
            //balance.year = String(year)
            ingreso.year = String(year)
            gasto.year = String(year)
            history.year = String(year)
            //analisis.year = String(year)
            //balance.month = month
            gasto.month = month
            ingreso.month = month
            history.month = month
            //analisis.month = month
            break
        default:
            //balance.month = nil
            gasto.month = nil
            ingreso.month = nil
            history.month = nil
            //analisis.month = nil
            //balance.year = years[selectedLapse]
            gasto.year = years[selectedLapse]
            ingreso.year = years[selectedLapse]
            history.year = years[selectedLapse]
            //analisis.year = years[selectedLapse]
            break
        }
        //balance.()
        ingreso.setChart()
        gasto.setChart()
        history.setArrays()
        history.setChart()
        //analisis.updateArrays()
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
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
    }
    
    func goToNextPage(index:Int){
        transition = false
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[index]], direction: .forward, animated: true, completion: nil)
    }


    func goToPreviousPage(index:Int){
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[index]], direction: .reverse, animated: true, completion: nil)
    }
}

extension MainGraphsViewController: UIPageViewControllerDelegate, UIPageViewControllerDataSource{

    
    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return currentIndex
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return 4
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
    func pageViewController(_ pageViewController: UIPageViewController, willTransitionTo pendingViewControllers: [UIViewController]) {
        let itemController = pendingViewControllers[0]
        nextIndex = itemController.view.tag
    }
    func pageViewController(pageViewController: UIPageViewController, willTransitionToViewControllers pendingViewControllers: [AnyObject]) {
        // the page view controller is about to transition to a new page, so take note
        // of the index of the page it will display.  (We can't update our currentIndex
        // yet, because the transition might not be completed - we will check in didFinishAnimating:)
        
    }
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed{
            currentIndex = nextIndex
            page1.textColor = UIColor.black
            page2.textColor = UIColor.black
            page3.textColor = UIColor.black
            page4.textColor = UIColor.black
            switch currentIndex {
            case 0:
                page1.textColor = UIColor.blue
                break;
            case 1:
                page2.textColor = UIColor.blue
                break;
            case 2:
                page3.textColor = UIColor.blue
                break;
            case 3:
                page4.textColor = UIColor.blue
            default:
                break;
            }
        }
    }
    
}
public class colors {

}
