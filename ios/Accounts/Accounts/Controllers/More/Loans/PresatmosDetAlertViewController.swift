//
//  PresatmosDetAlertViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 13/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class PresatmosDetAlertViewController: UIAlertController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    let txtTypeNumber = 0, txtTypeAll = 1, txtTypeNone = 2
    let pickerTotales = UIPickerView()
    let datePicker = UIDatePicker()
    var cuentaLbl = UILabel()
    var monedaLbl = UILabel()
    var cantTextField = UITextField()
    var cambioTextField = UITextField()
    var segmentedFecha = UISegmentedControl()
    var segmentedWhoPays = UISegmentedControl()
    let txtCuenta = UITextField()
    let txtFecha = UITextField()
    let numberFormatter = NumberFormatter()
    var dataArrayTotalesDet:[[String:Any?]] = []
    var data:[String:Any?]? = nil
    var monedaPrestamo:Int64 = 0, cantidadOriginal:Int64 = 0, cuentaOriginal:Int64 = 0
    var cambioH = false, gasto = false
    var date:Date = Date()
    var selectedTotales = 0
    var oldSelectedTotales = 0
    var idTotales:Int64 = 0, idMoneda:Int64 = 0, idMonedaCuenta:Int64 = 0
    var oldCantidad:NSNumber = 0.0
    var totalPaid = 0.0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let widthConstraints = self.view.constraints.filter({ return $0.firstAttribute == .width })
        self.view.removeConstraints(widthConstraints)
        // Here you can enter any width that you want
        let newWidth = UIScreen.main.bounds.width * 0.90
        // Adding constraint for alert base view
        let widthConstraint = NSLayoutConstraint(item: self.view!,
                                                 attribute: .width,
                                                 relatedBy: .equal,
                                                 toItem: nil,
                                                 attribute: .notAnAttribute,
                                                 multiplier: 1,
                                                 constant: newWidth)
        self.view.addConstraint(widthConstraint)
        let firstContainer = self.view.subviews[0]
        // Finding first child width constraint
        let constraint = firstContainer.constraints.filter({ return $0.firstAttribute == .width && $0.secondItem == nil })
        firstContainer.removeConstraints(constraint)
        // And replacing with new constraint equal to alert.view width constraint that we setup earlier
        self.view.addConstraint(NSLayoutConstraint(item: firstContainer,
                                                    attribute: .width,
                                                    relatedBy: .equal,
                                                    toItem: self.view,
                                                    attribute: .width,
                                                    multiplier: 1.0,
                                                    constant: 0))
        // Same for the second child with width constraint with 998 priority
        let innerBackground = firstContainer.subviews[0]
        let innerConstraints = innerBackground.constraints.filter({ return $0.firstAttribute == .width && $0.secondItem == nil })
        innerBackground.removeConstraints(innerConstraints)
        firstContainer.addConstraint(NSLayoutConstraint(item: innerBackground,
                                                        attribute: .width,
                                                        relatedBy: .equal,
                                                        toItem: firstContainer,
                                                        attribute: .width,
                                                        multiplier: 1.0,
                                                        constant: 0))
        
        let height:NSLayoutConstraint = NSLayoutConstraint(item: self.view!, attribute: NSLayoutConstraint.Attribute.height, relatedBy: NSLayoutConstraint.Relation.equal, toItem: nil, attribute: NSLayoutConstraint.Attribute.notAnAttribute, multiplier: 1, constant: 300)
        self.view.addConstraint(height);

        let marginX:CGFloat = 10.0
        let marginY:CGFloat = 65
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width - (marginX * 2) - (screenSize.width * 0.1)
        let rect = CGRect(x: marginX, y: marginY, width: screenWidth, height: 150)
        let customView = UIView(frame: rect)

        cuentaLbl = UILabel(frame: CGRect(x:0, y:0, width: customView.frame.width/2, height: 25))
        monedaLbl = UILabel(frame: CGRect(x: (customView.frame.width/2), y: 0, width: customView.frame.width/2, height: 25))
        cantTextField = UITextField(frame: CGRect(x: 0, y: 30, width: customView.frame.width/2, height: 25))
        cambioTextField = UITextField(frame: CGRect(x: customView.frame.width/2, y: 30, width: customView.frame.width/2, height: 25))
        segmentedFecha = UISegmentedControl(frame: CGRect(x: 0, y: 65, width: customView.frame.width, height: 40))
        segmentedWhoPays = UISegmentedControl(frame: CGRect(x: 0, y: 115, width: customView.frame.width, height: 40))
        
        cantTextField.delegate = self
        cambioTextField.delegate = self
        pickerTotales.delegate = self
        
        cambioTextField.tag = txtTypeNumber
        cantTextField.tag = txtTypeNumber
        
        segmentedFecha.insertSegment(withTitle: "Hoy", at: 0, animated: false)
        segmentedFecha.insertSegment(withTitle: "Ayer", at: 1, animated: false)
        segmentedFecha.insertSegment(withTitle: "Otro", at: 2, animated: false)
        
        segmentedWhoPays.insertSegment(withTitle: "Pago Yo", at: 0, animated: false)
        segmentedWhoPays.insertSegment(withTitle: "Paga El", at: 1, animated: false)
        
        segmentedWhoPays.tintColor = .blue
        
        customView.addSubview(cuentaLbl)
        customView.addSubview(monedaLbl)
        customView.addSubview(cantTextField)
        customView.addSubview(cambioTextField)
        customView.addSubview(segmentedFecha)
        customView.addSubview(segmentedWhoPays)
        
        let tapC = UITapGestureRecognizer(target: self, action: #selector(self.tapCuenta))
        cuentaLbl.addGestureRecognizer(tapC)
        cuentaLbl.isUserInteractionEnabled = true
        
        self.view.addSubview(customView)
        //cuentaLbl.text = "AAA"
        
        
        showDatePicker()
        
        segmentedFecha.addTarget(self, action: #selector(segmentActionFechaValueChange(_:)), for: UIControl.Event.allTouchEvents)
        
        segmentedFecha.addTarget(self, action: #selector(segmentActionFechaValueChange(_:)), for: .valueChanged)
        
        segmentedWhoPays.addTarget(self, action: #selector(segmentActionWhoValueChange(_:)), for: .valueChanged)
        
        setValues()
        preparePickerView()
    }
    
    @objc func segmentActionFechaValueChange(_ segmentedControl: UISegmentedControl) {
        let sel = segmentedControl.selectedSegmentIndex
        switch sel {
        case 0:
            date = Date()
            break
        case 1:
            date = Date()
            date = Calendar.current.date(byAdding: .day, value: -1, to: date)!
            break
        default:
            txtFecha.becomeFirstResponder()
            break
        }
    }
    
    @objc func segmentActionWhoValueChange(_ segmentedControl: UISegmentedControl) {
        let sel = segmentedControl.selectedSegmentIndex
        switch sel {
        case 0:
            gasto = true
            break
        case 1:
            gasto = false
            break
        default:
            break
        }
    }
    
    func showDatePicker(){
        //Formate Date
        datePicker.datePickerMode = .date
        //ToolBar
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donedatePicker));
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelDatePicker));

        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)

        txtFecha.inputAccessoryView = toolbar
        txtFecha.inputView = datePicker
        
        self.view.addSubview(txtFecha)
        
    }
    
    func preparePickerView(){
        let screenSize:CGRect = UIScreen.main.bounds
        var pickerRect = pickerTotales.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3 // some desired value
        
        pickerRect = pickerTotales.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3// some desired value
        pickerTotales.frame = pickerRect
        pickerTotales.frame.size.height = screenSize.size.height/3
        pickerTotales.frame.size.width = screenSize.width - 20
        pickerTotales.setValue(UIColor.black, forKey: "textColor")
        pickerTotales.autoresizingMask = .flexibleWidth
        pickerTotales.contentMode = .center
        //pickerTotales.tag = totalesTag
        pickerTotales.delegate = self
        pickerTotales.dataSource = self
        
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        txtCuenta.inputAccessoryView = toolbar
        txtCuenta.inputView = pickerTotales
        
        pickerTotales.selectRow(selectedTotales, inComponent: 0, animated: true)
        
        self.view.addSubview(txtCuenta)
    }
    @objc func donedatePicker(){
        let formatter = DateFormatter()
        formatter.dateFormat = "dd-MMM-yyyy"
        //fechaTxt.text = formatter.string(from: datePicker.date)
        segmentedFecha.setTitle(formatter.string(from: date), forSegmentAt: 2)
        date = datePicker.date
        self.view.endEditing(true)
    }
    
    @objc func cancelDatePicker(){
        self.view.endEditing(true)
    }
    @objc func donePicker(_ btn: UIBarButtonItem){
        idMoneda = (dataArrayTotalesDet[selectedTotales][Totales.IdMoneda] as? Int64)!
        idTotales = dataArrayTotalesDet[selectedTotales]["_id"] as! Int64
        cambioH = idMoneda == monedaPrestamo
        if !cambioH && data == nil {
            let cambio = getCambioMoneda(idMon1: idMoneda, idMon2: monedaPrestamo)
            cambioTextField.text = numberFormatter.string(from: cambio)
        } else {
            //let monOriginal = dataArrayPrestamo[0][Movimiento.IdMoneda] as? Int64
            //let totOriginal = dataArrayPrestamo[0][Movimiento.IdTotales] as? Int64
            if !cambioH && cuentaOriginal != nil && (cuentaOriginal != idTotales){
                let cambio = getCambioMoneda(idMon1: idMoneda, idMon2: monedaPrestamo)
                cambioTextField.text = numberFormatter.string(from: cambio)
                
            }
        }
        cambioTextField.isHidden = cambioH
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedTotales = oldSelectedTotales
        monedaLbl.text = dataArrayTotalesDet[selectedTotales][Moneda.Moneda] as? String
        cuentaLbl.text = dataArrayTotalesDet[selectedTotales][Totales.Cuenta] as? String
        pickerTotales.selectRow(selectedTotales, inComponent: 0, animated: true)
        self.view.endEditing(true)
    }
    
    func setValues(){
        if data == nil {
            dataArrayTotalesDet = getTotales(id: idTotales)
            var c = 0
            print(idTotales)
            for t in dataArrayTotalesDet {
                if t["_id"] as! Int64 == idTotales {
                    selectedTotales = c
                    oldSelectedTotales = c
                }
                c = c + 1
            }
            //totalPaid = getTotalPaid(id: data![PrestamosDet.IdPrestamo] as! Int64)
            var cantidad:Double = Double(truncating: oldCantidad) - totalPaid
            if cantidad <= 0 {
                cantidad = cantidad * -1
                segmentedWhoPays.selectedSegmentIndex = 0
                gasto = true
            } else {
                segmentedWhoPays.selectedSegmentIndex = 1
            }
            idMoneda = dataArrayTotalesDet[selectedTotales][Totales.IdMoneda] as! Int64
            cantTextField.text = "\(cantidad)"
            cuentaLbl.text = dataArrayTotalesDet[selectedTotales][Totales.Cuenta] as! String
            monedaLbl.text = dataArrayTotalesDet[selectedTotales][Moneda.Moneda] as! String
            segmentedFecha.selectedSegmentIndex = 0
            let cambio:NSNumber = 1.0
            cambioTextField.text = numberFormatter.string(from: cambio)
            cambioH = idMoneda == monedaPrestamo
            cambioTextField.isHidden = cambioH
        } else {
            let cuenta = data![Totales.Cuenta] as! String
            idTotales = data![PrestamosDet.IdTotales] as! Int64
            let moneda = data![Moneda.Moneda] as! String
            let cantidad = data![PrestamosDet.Cantidad] as! Double
            let fecha = data!["Fecha"] as! String
            var cambio = data![PrestamosDet.Cambio] as! NSNumber
            //print(fecha)
            idMoneda = data![Totales.IdMoneda] as! Int64
            //oldCantidad = cantidad
            dataArrayTotalesDet = getTotales(id: idTotales)
            cuentaLbl.text = cuenta
            monedaLbl.text = moneda
            //cantTextField.text = "\(cantidad)"
            segmentedFecha.selectedSegmentIndex = 2
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            date = dateFormatter.date(from: fecha)!
            dateFormatter.dateFormat = "dd-MMM-yy"
            segmentedFecha.setTitle(dateFormatter.string(from: date), forSegmentAt: 2)
            segmentedFecha.setTitle(fecha, forSegmentAt: 2)
            if cantidad > 0 {
                segmentedWhoPays.selectedSegmentIndex = 1
            } else {
                segmentedWhoPays.selectedSegmentIndex = 0
                gasto = true
            }
            var c = 0
            print(idTotales)
            for t in dataArrayTotalesDet {
                print(t["_id"])
                if t["_id"] as! Int64 == idTotales {
                    selectedTotales = c
                    oldSelectedTotales = c
                }
                c = c + 1
            }
            cambioTextField.text = numberFormatter.string(from: cambio)
            cambioH = idMoneda == monedaPrestamo
            cambioTextField.isHidden = cambioH
            //totalPaid = getTotalPaid(id: data![PrestamosDet.IdPrestamo] as! Int64)
            let tmpCant = cantidad
            cantTextField.text = tmpCant < 0 ? "\(tmpCant * -1)": "\(tmpCant)"
        }
        pickerTotales.selectRow(selectedTotales, inComponent: 0, animated: true)
    }
    @objc
    func tapCuenta(sender:UITapGestureRecognizer) {
        txtCuenta.becomeFirstResponder()
    }
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
           return 1
       }
       
       func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
            return dataArrayTotalesDet.count
       }
       
       func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
           return dataArrayTotalesDet[row][Totales.Cuenta] as! String
       }
       func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
           selectedTotales = row
        cuentaLbl.text = dataArrayTotalesDet[row][Totales.Cuenta] as? String
        monedaLbl.text = dataArrayTotalesDet[row][Moneda.Moneda] as? String
       }
       
       
       func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
           
           if textField.tag == txtTypeNone {
               return false
           } else if textField.tag == txtTypeNumber {
               guard CharacterSet(charactersIn: "0123456789.").isSuperset(of: CharacterSet(charactersIn: string)) else {
                   return false
               }
               textField.rightView?.isHidden = true
           }
           
           return true
       }
}
