//
//  SeeMovimientoViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/20/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class SeeMovimientoViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    
    @IBAction func segmenteValueGasto(_ sender: UISegmentedControl) {
        self.gasto = (sender.selectedSegmentIndex == 0)
        if gasto {
            cantidadTxt.textColor = UIColor.red
        } else {
            cantidadTxt.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
        actualizarResulCant()
    }
    @IBAction func segmentedValueFecha(_ sender: UISegmentedControl) {
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
            //txt.removeFromSuperview()
            break
        }
    }
    
    @IBAction func guardarBtn(_ sender: UIButton) {
        guardarMov()
    }
    @IBOutlet weak var monedaTxt: UITextField!
    @IBOutlet weak var cuentaTxt: UITextField!
    @IBOutlet weak var motivoTxt: UITextField!
    @IBOutlet weak var cantidadTxt: UITextField!
    @IBOutlet weak var cambioLbl: UILabel!
    @IBOutlet weak var cambioTxt: UITextField!
    @IBOutlet weak var commentTxt: UITextView!
    @IBOutlet weak var segmentedFecha: UISegmentedControl!
    @IBOutlet weak var segmentedGasto: UISegmentedControl!
    @IBOutlet var resultLbl: UILabel!
    
    let monedaTag = 1, totalesTag = 2, motivoTag = 3
    let datePicker = UIDatePicker()
    var pickerMoneda = UIPickerView(), pickerMotivo = UIPickerView(), pickerTotales = UIPickerView()
    var dataArrayMove:[[String:Any?]] = [], dataArrayMoneda:[[String:Any?]] = [], dataArrayMotivo:[[String:Any?]] = [], dataArrayTotales:[[String:Any?]] = []
    var cantidad:NSNumber = 0.0
    var _id:Int64? = 0, idMoneda:Int64 = 0, idTotales:Int64 = 0, idMotivo:Int64 = 0, idTrip:Int64 = 0, orgTotales:Int64 = 0, orgMoneda:Int64 = 0
    var comment:String?
    var motivo:String = "", moneda:String = "", totales:String = ""
    var selectedTotales = 0, selectedMoneda = 0,  selectedMotivo = 0
    var oldSelectedTotales = 0, oldSelectedMoneda = 0, oldSelectedMotivo = 0
    var date:Date = Date()
    var cambio:NSNumber = 1.0
    var cambioH = false, gasto:Bool = true, fechaEdit = false
    let txtTypeNumber = 0, txtTypeAll = 1, txtTypeNone = 2
    let numberFormatter = NumberFormatter()
    var pickersActive = false, activeAlert = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        
        hideKeyboardWhenTappedAround()
        

    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        activeAlert = false
        if _id == 0 {

            self.gasto = true
            cantidadTxt.textColor = UIColor.red
            
            dataArrayMotivo = getMotives(active: true)
            dataArrayTotales = getTotales(id: idTotales)
            dataArrayMoneda = getMonedasWith(id: idMoneda)
            if dataArrayMotivo.count == 0{
                let alert = UIAlertController(title: "No existe motivo", message: "", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "Agregar Motivo", style: .default, handler: {(a: UIAlertAction!) in
                    self.performSegue(withIdentifier: "crearMotivo", sender: nil)
                }))
                
                alert.addAction(UIAlertAction(title: "Cancelar", style: .cancel, handler: {(a: UIAlertAction!) in
                    self.navigationController?.popViewController(animated: true)
                }))
                //if !activeAlert{
                //    activeAlert = true
                    present(alert, animated: true, completion: nil)
                //}
            } else if dataArrayMoneda.count == 0 {
                let alert = UIAlertController(title: "No existe Moneda", message: "", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "Agregar Moneda", style: .default, handler: {(a: UIAlertAction!) in
                    self.performSegue(withIdentifier: "agregarMoneda", sender: nil)
                }))
                
                alert.addAction(UIAlertAction(title: "Cancelar", style: .cancel, handler: {(a: UIAlertAction!) in
                    self.navigationController?.popViewController(animated: true)
                }))
                //if !activeAlert{
                //    activeAlert = true
                    present(alert, animated: true, completion: nil)
                //}
            } else if dataArrayTotales.count == 0{
                let alert = UIAlertController(title: "No existe Cuenta", message: "", preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "Agregar Cuenta", style: .default, handler: {(a: UIAlertAction!) in
                    self.performSegue(withIdentifier: "agregarCuenta", sender: nil)
                }))
                
                alert.addAction(UIAlertAction(title: "Cancelar", style: .cancel, handler: {(a: UIAlertAction!) in
                    self.navigationController?.popViewController(animated: true)
                }))
                present(alert, animated: true, completion: nil)
            } else {
                if idMotivo == 0 {
                    idMotivo = dataArrayMotivo[0]["_id"] as! Int64
                    motivo = dataArrayMotivo[0][Motivo.Motivo] as! String
                }
                if idMoneda == 0 {
                    idMoneda = dataArrayMoneda[0]["_id"] as! Int64
                    moneda = dataArrayMoneda[0][Moneda.Moneda] as! String
                }
                if idTotales == 0 {
                    idTotales = dataArrayTotales[0]["_id"] as! Int64
                    totales = dataArrayTotales[0][Totales.Cuenta] as! String
                }
            }
            dataArrayMotivo = getMotives(active: true)
            
            if idMoneda != 0 {
                dataArrayTotales = getTotales(id: idTotales)
                dataArrayMotivo = getMotives(active: true)
                dataArrayMoneda = getMonedasWith(id: idMoneda)
                comment = ""
                moneda = dataArrayMoneda[0][Moneda.Moneda] as! String
                totales = dataArrayTotales[0][Totales.Cuenta] as! String
                var c = 0
                for m in dataArrayMoneda {
                    if m["_id"] as! Int64 == idMoneda{
                        moneda = m[Moneda.Moneda] as! String
                        selectedMoneda = c
                        oldSelectedMoneda = selectedMoneda
                    }
                    c = c + 1
                }
                c = 0
                for t in dataArrayTotales {
                    if t["_id"] as! Int64 == idTotales{
                        totales = t[Totales.Cuenta] as! String
                        selectedTotales = c
                        oldSelectedTotales = c
                    }
                    c = c + 1
                }
            }
            if cantidad != 0.0 {
                if Int(truncating: cantidad) < 0 {
                    self.gasto = true
                    self.segmentedGasto.selectedSegmentIndex = 0
                    self.cantidadTxt.textColor = UIColor.red
                    cantidad = NSNumber(value: Int(truncating: cantidad) * -1)
                } else {
                    self.gasto = false
                    self.segmentedGasto.selectedSegmentIndex = 1
                    self.cantidadTxt.textColor = Utils.green
                }
                cantidadTxt.text = "\(cantidad)"
            }
        } else {
            dataArrayMove = getMoveData(id: _id!)
            
            
            cantidad = dataArrayMove[0][Movimiento.Cantidad] as! NSNumber
            idMoneda = dataArrayMove[0][Movimiento.IdMoneda] as! Int64
            idTotales = dataArrayMove[0][Movimiento.IdTotales] as! Int64
            idMotivo = dataArrayMove[0][Movimiento.IdMotivo] as! Int64
            comment = dataArrayMove[0][Movimiento.Comment] as? String
            dataArrayMotivo = getMotives(id: idMotivo)
            dataArrayTotales = getTotales(id: idTotales)
            dataArrayMoneda = getMonedasWith(id: idMoneda)
            
            let fecha = dataArrayMove[0][Movimiento.Fecha] as! String
            let nFecha = dataArrayMove[0]["nFecha"] as! String
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            date = dateFormatter.date(from: nFecha)!
            
            var c = 0
            for m in dataArrayMotivo {
                if m["_id"] as! Int64 == idMotivo{
                    motivo = m[Motivo.Motivo] as! String
                    selectedMotivo = c
                    oldSelectedMotivo = selectedMotivo
                }
                c = c + 1
            }
            c = 0
            for m in dataArrayMoneda {
                if m["_id"] as! Int64 == idMoneda{
                    moneda = m[Moneda.Moneda] as! String
                    selectedMoneda = c
                    oldSelectedMoneda = selectedMoneda
                }
                c = c + 1
            }
            c = 0
            for t in dataArrayTotales {
                if t["_id"] as! Int64 == idTotales{
                    totales = t[Totales.Cuenta] as! String
                    selectedTotales = c
                    oldSelectedTotales = c
                }
                c = c + 1
            }
            if Int(truncating: cantidad) < 0 {
                self.gasto = true
                self.segmentedGasto.selectedSegmentIndex = 0
                self.cantidadTxt.textColor = UIColor.red
                cantidad = NSNumber(value: Double(truncating: cantidad) * -1)
            } else {
                self.gasto = false
                self.segmentedGasto.selectedSegmentIndex = 1
                self.cantidadTxt.textColor = Utils.green
            }
            cantidadTxt.text = "\(cantidad)"
            segmentedFecha.selectedSegmentIndex = 2
            dateFormatter.dateFormat = "dd-MMM-yyyy"
            //fechaTxt.text = formatter.string(from: datePicker.date)
            segmentedFecha.setTitle(dateFormatter.string(from: date), forSegmentAt: 2)
        }
        
        orgTotales = idTotales
        orgMoneda = idMoneda
        preparePickerView()
        
        var monedaCuenta:Int64 = 0
        var monedaMov:Int64 = 0
        if dataArrayTotales.count > 0 {
            monedaCuenta = (dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64)!
            monedaMov = (dataArrayMoneda[selectedMoneda]["_id"] as? Int64)!
        }
        cambioH = monedaCuenta == monedaMov
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        if !cambioH {
            if _id == nil || _id == 0 {
                cambio = 1.0
            } else {
                cambio = dataArrayMove[0][Movimiento.Cambio] as! NSNumber
            }
        } else {
            cambio = 1.0
        }
        monedaTxt.delegate = self
        motivoTxt.delegate = self
        cuentaTxt.delegate = self
        cantidadTxt.delegate = self
        cambioTxt.delegate = self
        monedaTxt.tag = txtTypeNone
        motivoTxt.tag = txtTypeNone
        cuentaTxt.tag = txtTypeNone
        cantidadTxt.tag = txtTypeNumber
        cambioTxt.tag = txtTypeNumber
        //fechaTxt.delegate = self
        cambioTxt.text = "\(cambio)"
        monedaTxt.text = moneda
        cuentaTxt.text = totales
        motivoTxt.text = motivo
        //fechaTxt.text = fecha
        
        
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: nil, action: #selector(endEditing))

        toolbar.setItems([doneButton], animated: false)

        cantidadTxt.inputAccessoryView = toolbar
        cambioTxt.inputAccessoryView = toolbar
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
        pickersActive = true
    }

    @objc func donedatePicker(){
        let formatter = DateFormatter()
        formatter.dateFormat = "dd-MMM-yyyy"
        //fechaTxt.text = formatter.string(from: datePicker.date)
        segmentedFecha.setTitle(formatter.string(from: datePicker.date), forSegmentAt: 2)
        date = datePicker.date
        fechaEdit = true
        pickersActive = false
        self.view.endEditing(true)
    }
    
    @objc func cancelDatePicker(){
        if !fechaEdit {
            segmentedFecha.selectedSegmentIndex = 0
        }
        pickersActive = false
        self.view.endEditing(true)
    }
    
    @objc func endEditing(){
        self.view.endEditing(true)
    }
    
    @objc func donePicker(_ btn: UIBarButtonItem){

        idTotales = dataArrayTotales[selectedTotales]["_id"] as! Int64
        idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        idMotivo = dataArrayMotivo[selectedMotivo]["_id"] as! Int64
        
        let monedaCuenta = dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64
        let monedaMov = dataArrayMoneda[selectedMoneda]["_id"] as? Int64
        cambioH = monedaCuenta == monedaMov
        if !cambioH && _id == 0 {
            cambio = getCambioMoneda(idMon1: monedaCuenta!, idMon2: monedaMov!)
            cambioTxt.text = "\(cambio)"
        } else if _id! > 0 as Int64 {
            let monOriginal = dataArrayMove[0][Movimiento.IdMoneda] as? Int64
            let totOriginal = dataArrayMove[0][Movimiento.IdTotales] as? Int64
            if !cambioH && monOriginal != nil && totOriginal != nil && (monOriginal != idMoneda || totOriginal != idTotales){
                cambio = getCambioMoneda(idMon1: monedaCuenta!, idMon2: monedaMov!)
                cambioTxt.text = "\(cambio)"
            }
        }
        cambioH = monedaCuenta == monedaMov
        if cambioH {
            cambioTxt.text = "1.0"
        }
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        oldSelectedTotales = selectedTotales
        oldSelectedMoneda = selectedMoneda
        oldSelectedMotivo = selectedMotivo
        pickersActive = false
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedTotales = oldSelectedTotales
        selectedMoneda = oldSelectedMoneda
        selectedMotivo = oldSelectedMotivo
        monedaTxt.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
        cuentaTxt.text = dataArrayTotales[selectedTotales][Totales.Cuenta] as? String
        motivoTxt.text = dataArrayMotivo[selectedMotivo][Motivo.Motivo] as? String
        let monedaCuenta = dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64
        let monedaMov = dataArrayMoneda[selectedMoneda]["_id"] as? Int64
        cambioH = monedaCuenta == monedaMov
        if !cambioH && _id == 0 {
            cambio = getCambioMoneda(idMon1: monedaCuenta!, idMon2: monedaMov!)
        }
        if cambioH {
            cambioTxt.text = "1.0"
        }
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        pickersActive = false
        actualizarResulCant()
        self.view.endEditing(true)
    }
    
    func preparePickerView(){
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
        pickerMoneda.tag = monedaTag
        pickerMoneda.delegate = self
        pickerMoneda.dataSource = self
        
        pickerRect = pickerTotales.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3// some desired value
        pickerTotales.frame = pickerRect
        pickerTotales.frame.size.height = screenSize.size.height/3
        pickerTotales.frame.size.width = screenSize.width - 20
        pickerTotales.setValue(UIColor.black, forKey: "textColor")
        pickerTotales.autoresizingMask = .flexibleWidth
        pickerTotales.contentMode = .center
        pickerTotales.tag = totalesTag
        pickerTotales.delegate = self
        pickerTotales.dataSource = self
        
        pickerRect = pickerMotivo.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3// some desired value
        pickerMotivo.frame = pickerRect
        pickerMotivo.frame.size.height = screenSize.size.height/3
        pickerMotivo.frame.size.width = screenSize.width - 20
        pickerMotivo.setValue(UIColor.black, forKey: "textColor")
        pickerMotivo.autoresizingMask = .flexibleWidth
        pickerMotivo.contentMode = .center
        pickerMotivo.tag = motivoTag
        pickerMotivo.delegate = self
        pickerMotivo.dataSource = self
        
        pickerRect = datePicker.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3// some desired value
        datePicker.frame = pickerRect
        datePicker.frame.size.height = screenSize.size.height/3
        datePicker.frame.size.width = screenSize.width - 20
        datePicker.setValue(UIColor.black, forKey: "textColor")
        datePicker.autoresizingMask = .flexibleWidth
        datePicker.contentMode = .center
        
        var toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        var doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        var spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        var cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        doneButton.tag = monedaTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        monedaTxt.inputAccessoryView = toolbar
        monedaTxt.inputView = pickerMoneda
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        doneButton.tag = totalesTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        cuentaTxt.inputAccessoryView = toolbar
        cuentaTxt.inputView = pickerTotales
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        doneButton.tag = motivoTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        

        motivoTxt.inputAccessoryView = toolbar
        motivoTxt.inputView = pickerMotivo
        
        pickerMoneda.selectRow(selectedMoneda, inComponent: 0, animated: true)
        pickerTotales.selectRow(selectedTotales, inComponent: 0, animated: true)
        pickerMotivo.selectRow(selectedMotivo, inComponent: 0, animated: true)
        
        //showDatePicker()
    }
    
    func actualizarResulCant(){
        var cantidadActual = dataArrayTotales[selectedTotales][Totales.CurrentCantidad] as! Double
        let tmpIdTotales = dataArrayTotales[selectedTotales]["_id"] as! Int64
        let tmpIdMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        if (_id > 0) {
            if tmpIdTotales == orgTotales {
                cantidadActual -= (dataArrayMove[0][Movimiento.Cantidad] as! Double) * (dataArrayMove[0][Movimiento.Cambio] as! Double)
            }
        }
        if orgMoneda == tmpIdMoneda && !cambioH {
            resultLbl.text = ""
            return
        }
        var newCantidad:Double = 0.0
        var cambio:Double? = 1.0
        
        if cantidadTxt.text == nil {
            //cantidadTxt.setError("Error", show: true)
            return
        }
        let dCantidad = Double(cantidadTxt.text!)
        if dCantidad != nil {
            newCantidad = dCantidad!
        }
        cambio = Double(cambioTxt.text!)
        if cambio == nil {
            cambio = 1.0
        }
        newCantidad = newCantidad * cambio!
        var totalCantidad:Double = cantidadActual + newCantidad
        if gasto {
            totalCantidad = cantidadActual - newCantidad
        }
        resultLbl.text = numberFormatter.string(from: NSNumber(value: totalCantidad))
        if totalCantidad > 0 {
            resultLbl.textColor = Utils.green
        } else if totalCantidad < 0 {
            resultLbl.textColor = UIColor.red
        } else {
            resultLbl.textColor = UIColor.black
        }
    }

    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView.tag == monedaTag {
            return dataArrayMoneda.count
        } else if pickerView.tag == totalesTag {
            return dataArrayTotales.count
        } else if pickerView.tag == motivoTag{
            return  dataArrayMotivo.count
        }
        return 0
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView.tag == monedaTag {
            return dataArrayMoneda[row][Moneda.Moneda] as? String
        } else if pickerView.tag == totalesTag {
            return dataArrayTotales[row][Totales.Cuenta] as? String
        } else if pickerView.tag == motivoTag{
            return  dataArrayMotivo[row][Motivo.Motivo] as? String
        }
        return ""
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView.tag == monedaTag {
            selectedMoneda = row
            monedaTxt.text = dataArrayMoneda[row][Moneda.Moneda] as? String
            actualizarResulCant()
            //return dataArrayMoneda.count
        } else if pickerView.tag == totalesTag {
            selectedTotales = row
            cuentaTxt.text = dataArrayTotales[row][Totales.Cuenta] as? String
            actualizarResulCant()
            //return dataArrayTotales.count
        } else if pickerView.tag == motivoTag {
            selectedMotivo = row
            motivoTxt.text = dataArrayMotivo[row][Motivo.Motivo] as? String
            //return  dataArrayMotivo.count
        }
        
        pickersActive = true
        //selectedMoneda = row
        
        //blurEffectView!.removeFromSuperview()
        //picker.removeFromSuperview()
        //sleep(UInt32(0.5))
        //pickerView.removeFromSuperview()
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        
        if textField.tag == txtTypeNone {
            pickersActive = false
            return false
        } else if textField.tag == txtTypeNumber {
            guard CharacterSet(charactersIn: "0123456789.").isSuperset(of: CharacterSet(charactersIn: string)) else {
                return false
            }
            textField.rightView?.isHidden = true
        }
        
        return true
    }

    func textFieldDidChangeSelection(_ textField: UITextField) {
        if textField.tag == txtTypeNumber {
            actualizarResulCant()
        }
    }
/*
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        return false
    }
*/
    func guardarMov(){
        var dcambio = Double(cambioTxt.text ?? "0.0")
        if cambioH {
            dcambio = 1.0
        } else {
            if cambioTxt.text == nil {
                cambioTxt.setError("Error", show: true)
                return
            }
            dcambio = Double(cantidadTxt.text!)
            if dcambio == nil {
                cambioTxt.setError("Error", show: true)
                return
            }
            let moneda2 = dataArrayTotales[selectedTotales][Totales.IdMoneda] as! Int64
            actualizarTipoCambio(moneda1: idMoneda, moneda2: moneda2, cambio: dcambio!)
        }
        if cantidadTxt.text == nil {
            cantidadTxt.setError("Error", show: true)
            return
        }
        var dCantidad = Double(cantidadTxt.text!)
        if dCantidad == nil {
            cantidadTxt.setError("Error", show: true)
            return
        }
        if gasto {
            dCantidad = dCantidad! * -1
        }
        let com:String? = commentTxt.text
        let g = "cantidad: \(dCantidad ?? 0.0), idTotales: \(idTotales), comment: \(com)), idMotivo: \(idMotivo), idMoneda: \(idMoneda), cambio: \(dcambio), fecha: \(date), id: \(_id)"
        //print(g)
        if _id == 0 {
            ///Nuevo
            let x = newMove(cantidad: dCantidad ?? 0.0, idCuenta: idTotales, comment: com, idMotivo: idMotivo, idMoneda: idMoneda, cambio: dcambio!, date: date, idViaje: idTrip)
            let y = newMoveCuenta(cantidad: dCantidad! * dcambio!, idCuenta: idTotales)
            if x && y{
                navigationController?.popViewController(animated: true)
            }
        } else {
            ///Actualizar
            let x = actualizarCuentaMove(cantidad: dCantidad! * dcambio!, idCuenta: idTotales, idMove: _id!)
            let y = actualizarMovimiento(id: _id!, cantidad: dCantidad!, idTotales: idTotales, comment: com!, idMotivo: idMotivo, idMoneda: idMoneda, cambio: dcambio!, date: date)
            if x && y{
                navigationController?.popViewController(animated: true)
            }
        }
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
       
        if "crearMotivo" == segue.identifier {
            let viewController = segue.destination as! MotivesMainViewController
            viewController.title = "Motivos"
        } else if "agregarMoneda" == segue.identifier {
            let viewController = segue.destination as! MonedaMainViewController
            viewController.title = "Monedas"
        } else if "agregarCuenta" == segue.identifier {
            let viewController = segue.destination as! CreateCuentasViewController
            viewController.title = "Crear Cuenta"
        }
    }
}

extension SeeMovimientoViewController {
    func hideKeyboardWhenTappedAround() {
        let tapGesture = UITapGestureRecognizer(target: self,
                         action: #selector(hideKeyboard))
        view.addGestureRecognizer(tapGesture)
    }

    @objc func hideKeyboard() {
        if !pickersActive {
            view.endEditing(true)
        }
    }
}

extension UITextField {
    // Add/remove error message
    func setError(_ string: String, show: Bool) {
        let errorImg = UIImageView (frame: CGRect(x: 0, y: 0, width: 20, height: 20))
        errorImg.image = UIImage(named: "errorIcon")
        errorImg.contentMode = UIView.ContentMode.left

        self.layer.borderWidth = 1.0
        self.layer.borderColor = UIColor(red:0.93, green:0.93, blue:0.93, alpha:1.0).cgColor
        self.layer.cornerRadius = self.frame.size.height / 2
        self.layer.masksToBounds = true

        let gapLabel = UIView (frame: CGRect(x: 60, y: 0, width: 10, height: 40))
        self.leftView = gapLabel
        self.leftViewMode = UITextField.ViewMode.always

         /* Display that one when validation is failed */
        self.rightView = errorImg;
        self.rightViewMode = UITextField.ViewMode.always
        self.rightView?.isHidden = false
        
        self.text = ""
        self.placeholder = string
    }
    
    
}