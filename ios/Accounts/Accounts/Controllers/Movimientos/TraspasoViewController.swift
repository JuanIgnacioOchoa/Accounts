//
//  TraspasoViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 29/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class TraspasoViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    @IBAction func segmentFechaChange(_ sender: UISegmentedControl) {
        let sel = sender.selectedSegmentIndex
        switch sel {
        case 0:
            date = Date()
            break
        case 1:
            date = Date()
            date = Calendar.current.date(byAdding: .day, value: -1, to: date)!
            break
        default:
            let txt = UITextField()
            txt.delegate = self
            self.view.addSubview(txt)
            showDatePicker(fechaTxt: txt)
            break
        }
    }
    @IBAction func segmentedTouchUp(_ sender: UISegmentedControl) {
        let sel = sender.selectedSegmentIndex
        switch sel {
        case 0:
            date = Date()
            break
        case 1:
            date = Date()
            date = Calendar.current.date(byAdding: .day, value: -1, to: date)!
            break
        default:
            let txt = UITextField()
            txt.delegate = self
            self.view.addSubview(txt)
            showDatePicker(fechaTxt: txt)
            break
        }
    }
    @IBAction func saveBtn(_ sender: UIButton) {
        guardar()
    }
    
    @IBOutlet weak var cantidadTxt: UITextField!
    @IBOutlet weak var fromTxt: UITextField!
    @IBOutlet weak var toTxt: UITextField!
    @IBOutlet weak var cambioTxt: UITextField!
    @IBOutlet weak var cambioLbl: UILabel!
    @IBOutlet weak var segmentFecha: UISegmentedControl!
    @IBOutlet weak var commentTxt: UITextView!
    @IBOutlet weak var monedaLbl: UILabel!
    
    var date:Date = Date()
    let datePicker = UIDatePicker()
    var fechaEdit = false, cambioH = false, retiro = false
    let totalesFromTag = 2, totalesToTag = 3
    var pickertTotalesFrom = UIPickerView(), pickerTotalesTo = UIPickerView()
    var dataArrayMove:[[String:Any?]] = [], dataArrayTotales:[[String:Any?]] = []
    var totalesFrom:String = "", moneda:String = "", totalesTo:String = ""
    var selectedTotalesTo = 0,  selectedTotalesFrom = 0
    var oldSelectedTotalesTo = 0, oldSelectedTotalesFrom = 0
    var _id:Int64 = 0, idTotalesFrom:Int64 = 0, idTotalesTo:Int64 = 0, idMotivo:Int64 = 0
    var cambio:NSNumber = 1.0
    let numberFormatter = NumberFormatter()
    let txtTypeNumber = 0, txtTypeAll = 1, txtTypeNone = 2
    
    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        
        if _id == 0 {
            dataArrayTotales = getTotales()
        } else {
            dataArrayMove = getMoveData(id: _id)

            idTotalesFrom = dataArrayMove[0][Movimiento.IdTotales] as! Int64
            idTotalesTo = dataArrayMove[0][Movimiento.Traspaso] as! Int64
            idMotivo = dataArrayMove[0][Movimiento.IdMotivo] as! Int64
            
            dataArrayTotales = getTotales(id: idTotalesFrom)//TODO
            
            if idMotivo == 1 {
                monedaLbl.text = (dataArrayTotales[selectedTotalesFrom][Moneda.Moneda] as! String)
                retiro = false;
            }
            else{
                monedaLbl.text = (dataArrayTotales[selectedTotalesTo][Moneda.Moneda] as! String)
                retiro = true;
            }
            
            var c = 0
            for t in dataArrayTotales {
                if t["_id"] as! Int64 == idTotalesFrom{
                    totalesFrom = t[Totales.Cuenta] as! String
                    selectedTotalesFrom = c
                    oldSelectedTotalesFrom = c
                }
                c = c + 1
            }
            c = 0
            for t in dataArrayTotales {
                if t["_id"] as! Int64 == idTotalesTo{
                    totalesTo = t[Totales.Cuenta] as! String
                    selectedTotalesTo = c
                    oldSelectedTotalesTo = c
                }
                c = c + 1
            }
        }
        
        preparePickerView()
        
        let monedaFrom = dataArrayTotales[selectedTotalesFrom][Totales.IdMoneda] as? Int64
        let monedaTo = dataArrayTotales[selectedTotalesFrom]["_id"] as? Int64
        cambioH = monedaFrom == monedaTo
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        if !cambioH {
            if _id == 0 {
                cambio = 1.0
            } else {
                cambio = dataArrayMove[0][Movimiento.Cambio] as! NSNumber
            }
        } else {
            cambio = 1.0
        }
        //monedaTxt.delegate = self
        toTxt.delegate = self
        fromTxt.delegate = self
        cantidadTxt.delegate = self
        cambioTxt.delegate = self
        toTxt.tag = txtTypeNone
        fromTxt.tag = txtTypeNone
        cantidadTxt.tag = txtTypeNumber
        cambioTxt.tag = txtTypeNumber
        cambioTxt.text = numberFormatter.string(from: cambio)
        toTxt.text = totalesTo
        fromTxt.text = totalesFrom
    }

    
    func showDatePicker(fechaTxt:UITextField){
        //Formate Date
        datePicker.datePickerMode = .date
        datePicker.date = date
        //ToolBar
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donedatePicker));
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelDatePicker));

        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)

        fechaTxt.inputAccessoryView = toolbar
        fechaTxt.inputView = datePicker
        fechaTxt.becomeFirstResponder()
    }

    @objc func donedatePicker(){
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        //fechaTxt.text = formatter.string(from: datePicker.date)
        segmentFecha.setTitle(formatter.string(from: datePicker.date), forSegmentAt: 2)
        date = datePicker.date
        fechaEdit = true
        self.view.endEditing(true)
    }
    
    func preparePickerView(){
        let screenSize:CGRect = UIScreen.main.bounds
        var pickerRect = pickerTotalesTo.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3// some desired value
        pickerTotalesTo.frame = pickerRect
        pickerTotalesTo.frame.size.height = screenSize.size.height/3
        pickerTotalesTo.frame.size.width = screenSize.width - 20
        pickerTotalesTo.setValue(UIColor.black, forKey: "textColor")
        pickerTotalesTo.autoresizingMask = .flexibleWidth
        pickerTotalesTo.contentMode = .center
        pickerTotalesTo.tag = totalesToTag
        pickerTotalesTo.delegate = self
        pickerTotalesTo.dataSource = self
        
        pickerRect = pickertTotalesFrom.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3// some desired value
        pickertTotalesFrom.frame = pickerRect
        pickertTotalesFrom.frame.size.height = screenSize.size.height/3
        pickertTotalesFrom.frame.size.width = screenSize.width - 20
        pickertTotalesFrom.setValue(UIColor.black, forKey: "textColor")
        pickertTotalesFrom.autoresizingMask = .flexibleWidth
        pickertTotalesFrom.contentMode = .center
        pickertTotalesFrom.tag = totalesFromTag
        pickertTotalesFrom.delegate = self
        pickertTotalesFrom.dataSource = self
        
        
        var toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        var doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        var spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        var cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        
        doneButton.tag = totalesFromTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        fromTxt.inputAccessoryView = toolbar
        fromTxt.inputView = pickertTotalesFrom
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        doneButton.tag = totalesToTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        

        toTxt.inputAccessoryView = toolbar
        toTxt.inputView = pickerTotalesTo
        
        pickertTotalesFrom.selectRow(selectedTotalesFrom, inComponent: 0, animated: true)
        pickerTotalesTo.selectRow(selectedTotalesTo, inComponent: 0, animated: true)
        
    }
    
    @objc func donePicker(_ btn: UIBarButtonItem){

        idTotalesTo = dataArrayTotales[selectedTotalesTo]["_id"] as! Int64
        idTotalesFrom = dataArrayTotales[selectedTotalesFrom]["_id"] as! Int64
        if !retiro {
            monedaLbl.text = (dataArrayTotales[selectedTotalesFrom][Moneda.Moneda] as! String)
        }
        else{
            monedaLbl.text = (dataArrayTotales[selectedTotalesTo][Moneda.Moneda] as! String)
        }
        let monedaCuentaFrom = dataArrayTotales[selectedTotalesFrom][Totales.IdMoneda] as? Int64
        let monedaCuentaTo = dataArrayTotales[selectedTotalesTo][Totales.IdMoneda] as? Int64
        cambioH = monedaCuentaFrom == monedaCuentaTo
        if !cambioH && _id == 0 {
            cambio = getCambioMoneda(idMon1: monedaCuentaFrom!, idMon2: monedaCuentaTo!)
            cambioTxt.text = numberFormatter.string(from: cambio)
        } else if _id > 0 as Int64 {
            let traspOriginal = dataArrayMove[0][Movimiento.Traspaso] as? Int64
            let totOriginal = dataArrayMove[0][Movimiento.IdTotales] as? Int64
            if !cambioH && traspOriginal != nil && totOriginal != nil && (traspOriginal != idTotalesTo || totOriginal != idTotalesFrom){
                cambio = getCambioMoneda(idMon1: monedaCuentaTo!, idMon2: monedaCuentaFrom!)
                cambioTxt.text = numberFormatter.string(from: cambio)
            }
        }
        cambioH = monedaCuentaTo == monedaCuentaFrom
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        oldSelectedTotalesTo = selectedTotalesTo
        oldSelectedTotalesFrom = selectedTotalesFrom
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedTotalesFrom = oldSelectedTotalesFrom
        selectedTotalesTo = oldSelectedTotalesTo
        //monedaTxt.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
        toTxt.text = dataArrayTotales[selectedTotalesTo][Totales.Cuenta] as? String
        fromTxt.text = dataArrayTotales[selectedTotalesFrom][Totales.Cuenta] as? String
        if !retiro {
            monedaLbl.text = (dataArrayTotales[selectedTotalesFrom][Moneda.Moneda] as! String)
        }
        else{
            monedaLbl.text = (dataArrayTotales[selectedTotalesTo][Moneda.Moneda] as! String)
        }
        let monedaCuentaFrom = dataArrayTotales[selectedTotalesFrom][Totales.IdMoneda] as? Int64
        let monedaCuentaTo = dataArrayTotales[selectedTotalesTo][Totales.IdMoneda] as? Int64
        cambioH = monedaCuentaFrom == monedaCuentaTo
        if !cambioH && _id == 0 {
            cambio = getCambioMoneda(idMon1: monedaCuentaTo!, idMon2: monedaCuentaFrom!)
        }
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        pickerTotalesTo.selectRow(selectedTotalesTo, inComponent: 0, animated: true)
        pickertTotalesFrom.selectRow(selectedTotalesFrom, inComponent: 0, animated: true)
        self.view.endEditing(true)
    }
    
    @objc func cancelDatePicker(){
        if !fechaEdit {
            segmentFecha.selectedSegmentIndex = 0
        }
        self.view.endEditing(true)
    }
    
    func guardar(){
        var dcambio = Double(cambioTxt.text ?? "0.0")
        if cambioH {
            dcambio = 1.0
        } else {
            if cambioTxt.text == nil {
                cambioTxt.setError("Error")
                return
            }
            dcambio = Double(cambioTxt.text!)
            if dcambio == nil {
                cambioTxt.setError("Error")
                return
            }
            let monedaFrom = dataArrayTotales[selectedTotalesFrom][Totales.IdMoneda] as! Int64
            let monedaTo = dataArrayTotales[selectedTotalesTo][Totales.IdMoneda] as! Int64
            if retiro {
                actualizarTipoCambio(moneda1: monedaTo, moneda2: monedaFrom, cambio: dcambio!)
            } else {
                actualizarTipoCambio(moneda1: monedaFrom, moneda2: monedaTo, cambio: dcambio!)
            }
        }
        if cantidadTxt.text == nil {
            cantidadTxt.setError("Error")
            return
        }
        var dCantidad = Double(cantidadTxt.text!)
        if dCantidad == nil {
            cantidadTxt.setError("Error")
            return
        }
        let com:String? = commentTxt.text
        var idMoneda:Int64 = 0
        if !retiro {
            idMoneda = dataArrayTotales[selectedTotalesFrom][Totales.IdMoneda] as! Int64
            monedaLbl.text = (dataArrayTotales[selectedTotalesFrom][Moneda.Moneda] as! String)
        }
        else{
            idMoneda = dataArrayTotales[selectedTotalesTo][Totales.IdMoneda] as! Int64
            monedaLbl.text = (dataArrayTotales[selectedTotalesTo][Moneda.Moneda] as! String)
        }
        let g = "cantidad: \(dCantidad ?? 0.0), idTotales: \(idTotalesFrom), comment: \(com)), idMotivo: \(idMotivo), idMoneda: \(idMoneda), cambio: \(dcambio), fecha: \(date), id: \(_id)"
        print(g)
        if _id == 0 {
            ///Nuevo
            //let x = newMove(cantidad: dCantidad ?? 0.0, idCuenta: idTotales, comment: com, idMotivo: idMotivo, idMoneda: idMoneda, cambio: dcambio!, date: date)
            //let y = newMoveCuenta(cantidad: dCantidad! * dcambio!, idCuenta: idTotales)
            //if x && y{
            //    navigationController?.popViewController(animated: true)
            //}
        } else {
            ///Actualizar
            //let x = actualizarCuentaMove(cantidad: dCantidad! * dcambio!, idCuenta: idTotales, idMove: _id!)
            //let y = actualizarMovimiento(id: _id!, cantidad: dCantidad!, idTotales: idTotales, comment: com!, idMotivo: idMotivo, idMoneda: idMoneda, cambio: dcambio!, date: date)
            //if x && y{
            //    navigationController?.popViewController(animated: true)
            //}
        }
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView.tag == totalesToTag {
            return dataArrayTotales.count
        } else if pickerView.tag == totalesFromTag{
            return  dataArrayTotales.count
        }
        return 0
    }
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView.tag == totalesToTag {
            return dataArrayTotales[row][Totales.Cuenta] as? String
        } else if pickerView.tag == totalesFromTag{
            return  dataArrayTotales[row][Totales.Cuenta] as? String
        }
        return ""
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView.tag == totalesToTag {
            selectedTotalesTo = row
            toTxt.text = dataArrayTotales[row][Totales.Cuenta] as? String
            if !retiro {
                monedaLbl.text = (dataArrayTotales[selectedTotalesFrom][Moneda.Moneda] as! String)
            }
            else{
                monedaLbl.text = (dataArrayTotales[selectedTotalesTo][Moneda.Moneda] as! String)
            }
        } else if pickerView.tag == totalesFromTag {
            selectedTotalesFrom = row
            fromTxt.text = dataArrayTotales[row][Totales.Cuenta] as? String
            if !retiro {
                monedaLbl.text = (dataArrayTotales[selectedTotalesFrom][Moneda.Moneda] as! String)
            }
            else{
                monedaLbl.text = (dataArrayTotales[selectedTotalesTo][Moneda.Moneda] as! String)
            }
        }
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
