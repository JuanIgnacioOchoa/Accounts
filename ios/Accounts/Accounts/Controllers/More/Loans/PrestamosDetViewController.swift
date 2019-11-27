//
//  PrestamosDetViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 06/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class PrestamosDetViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    @IBAction func saveBtn(_ sender: UIButton) {
        guardar()
    }
    @IBAction func segmentedFechaChange(_ sender: UISegmentedControl) {
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
            txtFecha.becomeFirstResponder()
            break
        }
    }
    @IBAction func segmentedClick(_ sender: UISegmentedControl) {
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
            txtFecha.becomeFirstResponder()
            break
        }
    }
    @IBOutlet weak var CantidadTxt: UITextField!
    @IBOutlet weak var MonedaLbl: UILabel!

    @IBOutlet weak var CuentaLbl: UILabel!
    @IBOutlet weak var PresonaLbl: UILabel!
    @IBOutlet weak var segmentedFecha: UISegmentedControl!
    @IBOutlet weak var descripcion: UITextView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var cambioLbl: UILabel!
    @IBOutlet weak var cambioTxt: UITextField!
    
    let monedaTag = 1, totalesTag = 2, personaTag = 3
    let datePicker = UIDatePicker()
    var pickerMoneda = UIPickerView(), pickerTotales = UIPickerView(), pickerPersonas = UIPickerView()
    let txtMoneda = UITextField(), txtCuenta = UITextField(), txtFecha = UITextField(), txtPersona = UITextField()
    var _id:Int64 = 0, idMoneda:Int64 = 1, idTotales:Int64 = 1, idPersona:Int64 = 1
    var date:Date = Date()
    var dataArrayPrestamo:[[String:Any?]] = [], dataArrayMoneda:[[String:Any?]] = [], dataArrayTotales:[[String:Any?]] = [], dataArrayPersona:[[String:Any?]] = [], dataArrayDetalle:[[String:Any?]] = []
    let txtTypeNumber = 0, txtTypeAll = 1, txtTypeNone = 2
    let numberFormatter = NumberFormatter()
    var cambioH = false, fechaEdit = false, prestado = false
    var selectedTotales = 0, selectedMoneda = 0, selectedPersona = 0
    var oldSelectedTotales = 0, oldSelectedMoneda = 0, oldSelectedPersona = 0
    var cambio:NSNumber = 1.0, cantidad:NSNumber = 0.0
    var comment:String?
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        tableView.delegate = self
        tableView.dataSource = self
        dataArrayDetalle = getPrestamoDetalle(id: _id)
        self.view.addSubview(txtFecha)
        self.view.addSubview(txtMoneda)
        self.view.addSubview(txtCuenta)
        self.view.addSubview(txtPersona)
        if _id == 0 {
            dataArrayTotales = getTotales()
            //dataArrayMoneda = getMonedas()
            dataArrayPersona = getPersonas()
            tableView.isHidden = true
        } else {
            tableView.isHidden = false

            dataArrayPrestamo = getPrestamos(id: _id)
            
            cantidad = dataArrayPrestamo[0][Prestamos.Cantidad] as! NSNumber
            idMoneda = dataArrayPrestamo[0][Prestamos.IdMoneda] as! Int64
            idTotales = dataArrayPrestamo[0][Prestamos.IdTotales] as! Int64
            idPersona = dataArrayPrestamo[0][Prestamos.IdPersona] as! Int64
            comment = dataArrayPrestamo[0][Prestamos.Comment] as? String
            
            dataArrayTotales = getTotales(id: idTotales)
            dataArrayMoneda = getMonedasWith(id: idMoneda)
            dataArrayPersona = getPersonas() //TODO CREATE WITH
            
            let fecha = dataArrayPrestamo[0][Prestamos.Fecha] as! String
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            date = dateFormatter.date(from: fecha)!
            let doubleCant:Double = Double(truncating: cantidad)
            var c = 0
            for m in dataArrayMoneda {
                if m["_id"] as! Int64 == idMoneda{
                    selectedMoneda = c
                    oldSelectedMoneda = selectedMoneda
                }
                c = c + 1
            }
            c = 0
            for t in dataArrayTotales {
                if t["_id"] as! Int64 == idTotales{
                    selectedTotales = c
                    oldSelectedTotales = c
                }
                c = c + 1
            }
            c = 0
            for t in dataArrayPersona {
                if t["_id"] as! Int64 == idPersona{
                    selectedPersona = c
                    oldSelectedPersona = c
                }
                c = c + 1
            }
            CantidadTxt.text = numberFormatter.string(from: NSNumber(value: doubleCant))
            segmentedFecha.selectedSegmentIndex = 2
            dateFormatter.dateFormat = "dd-MMM-yyyy"
            //fechaTxt.text = formatter.string(from: datePicker.date)
            segmentedFecha.setTitle(dateFormatter.string(from: date), forSegmentAt: 2)
        }
        preparePickerView()
        let monedaCuenta = dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64
        let monedaMov = dataArrayMoneda[selectedMoneda]["_id"] as? Int64
        cambioH = monedaCuenta == monedaMov
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        if !cambioH {
            if _id == 0 {
                cambio = 1.0
            } else {
                cambio = dataArrayPrestamo[0][Prestamos.Cambio] as! NSNumber
            }
        } else {
            cambio = 1.0
        }
        let tapM = UITapGestureRecognizer(target: self, action: #selector(self.tapMoneda))
        MonedaLbl.addGestureRecognizer(tapM)
        MonedaLbl.isUserInteractionEnabled = true
        let tapC = UITapGestureRecognizer(target: self, action: #selector(self.tapCuenta))
        CuentaLbl.addGestureRecognizer(tapC)
        CuentaLbl.isUserInteractionEnabled = true
        let tapP = UITapGestureRecognizer(target: self, action: #selector(self.tapPersona))
        PresonaLbl.addGestureRecognizer(tapP)
        PresonaLbl.isUserInteractionEnabled = true
        //MonedaLbl.delegate = self
        //CuentaLbl.delegate = self
        CantidadTxt.delegate = self
        cambioTxt.delegate = self
        CantidadTxt.tag = txtTypeNumber
        cambioTxt.tag = txtTypeNumber
        //fechaTxt.delegate = self
        cambioTxt.text = numberFormatter.string(from: cambio)
        MonedaLbl.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
        CuentaLbl.text = dataArrayTotales[selectedTotales][Totales.Cuenta] as? String
        PresonaLbl.text = dataArrayPersona[selectedPersona][Personas.Nombre] as? String
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
        
        pickerRect = pickerTotales.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height/3// some desired value
        pickerPersonas.frame = pickerRect
        pickerPersonas.frame.size.height = screenSize.size.height/3
        pickerPersonas.frame.size.width = screenSize.width - 20
        pickerPersonas.setValue(UIColor.black, forKey: "textColor")
        pickerPersonas.autoresizingMask = .flexibleWidth
        pickerPersonas.contentMode = .center
        pickerPersonas.tag = personaTag
        pickerPersonas.delegate = self
        pickerPersonas.dataSource = self
        

        
        var toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        var doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        var spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        var cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        doneButton.tag = monedaTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        txtMoneda.inputAccessoryView = toolbar
        txtMoneda.inputView = pickerMoneda
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        doneButton.tag = totalesTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        txtCuenta.inputAccessoryView = toolbar
        txtCuenta.inputView = pickerTotales
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        doneButton.tag = personaTag
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        txtPersona.inputAccessoryView = toolbar
        txtPersona.inputView = pickerPersonas

        
        pickerMoneda.selectRow(selectedMoneda, inComponent: 0, animated: true)
        pickerTotales.selectRow(selectedTotales, inComponent: 0, animated: true)
        pickerPersonas.selectRow(selectedPersona, inComponent: 0, animated: true)
        
        showDatePicker()
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
        
    }
    
    @objc func donePicker(_ btn: UIBarButtonItem){

        idTotales = dataArrayTotales[selectedTotales]["_id"] as! Int64
        idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        idPersona = dataArrayPersona[selectedPersona]["_id"] as! Int64
        
        let monedaCuenta = dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64
        let monedaMov = dataArrayMoneda[selectedMoneda]["_id"] as? Int64
        cambioH = monedaCuenta == monedaMov
        if !cambioH && _id == 0 {
            cambio = getCambioMoneda(idMon1: monedaCuenta!, idMon2: monedaMov!)
            cambioTxt.text = numberFormatter.string(from: cambio)
        } else if _id > 0 as Int64 {
            let monOriginal = dataArrayPrestamo[0][Movimiento.IdMoneda] as? Int64
            let totOriginal = dataArrayPrestamo[0][Movimiento.IdTotales] as? Int64
            if !cambioH && monOriginal != nil && totOriginal != nil && (monOriginal != idMoneda || totOriginal != idTotales){
                cambio = getCambioMoneda(idMon1: monedaCuenta!, idMon2: monedaMov!)
                cambioTxt.text = numberFormatter.string(from: cambio)
            }
        }
        cambioH = monedaCuenta == monedaMov
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        oldSelectedTotales = selectedTotales
        oldSelectedMoneda = selectedMoneda
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedTotales = oldSelectedTotales
        selectedMoneda = oldSelectedMoneda
        selectedPersona = oldSelectedPersona
        MonedaLbl.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
        CuentaLbl.text = dataArrayTotales[selectedTotales][Totales.Cuenta] as? String
        PresonaLbl.text = dataArrayPersona[selectedPersona][Personas.Nombre] as? String
        
        let monedaCuenta = dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64
        let monedaMov = dataArrayMoneda[selectedMoneda]["_id"] as? Int64
        cambioH = monedaCuenta == monedaMov
        if !cambioH && _id == 0 {
            cambio = getCambioMoneda(idMon1: monedaCuenta!, idMon2: monedaMov!)
        }
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        pickerMoneda.selectRow(selectedMoneda, inComponent: 0, animated: true)
        pickerTotales.selectRow(selectedTotales, inComponent: 0, animated: true)
        pickerPersonas.selectRow(selectedPersona, inComponent: 0, animated: true)
        
        self.view.endEditing(true)
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
    @objc
    func tapMoneda(sender:UITapGestureRecognizer) {
        txtMoneda.becomeFirstResponder()
    }
    
    @objc
    func tapCuenta(sender:UITapGestureRecognizer) {
        txtCuenta.becomeFirstResponder()
    }
    
    @objc
    func tapPersona(sender:UITapGestureRecognizer) {
        txtPersona.becomeFirstResponder()
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
         if pickerView.tag == monedaTag {
             return dataArrayMoneda.count
         } else if pickerView.tag == totalesTag {
             return dataArrayTotales.count
         } else if pickerView.tag == personaTag {
             return dataArrayPersona.count
         }
         return 0
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView.tag == monedaTag {
            return dataArrayMoneda[row][Moneda.Moneda] as? String
        } else if pickerView.tag == totalesTag {
            return dataArrayTotales[row][Totales.Cuenta] as? String
        } else if pickerView.tag == personaTag {
            return dataArrayPersona[row][Personas.Nombre] as? String
        }
        return ""
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView.tag == monedaTag {
            selectedMoneda = row
            MonedaLbl.text = dataArrayMoneda[row][Moneda.Moneda] as? String
            //return dataArrayMoneda.count
        } else if pickerView.tag == totalesTag {
            selectedTotales = row
            CuentaLbl.text = dataArrayTotales[row][Totales.Cuenta] as? String
            //return dataArrayTotales.count
        } else if pickerView.tag == personaTag {
            selectedPersona = row
            PresonaLbl.text = dataArrayPersona[row][Personas.Nombre] as? String
            //return dataArrayTotales.count
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
    
    func guardar(){
        var dcambio = Double(cambioTxt.text ?? "0.0")
        if cambioH {
            dcambio = 1.0
        } else {
            if cambioTxt.text == nil {
                cambioTxt.setError("Error", show: true)
                return
            }
            dcambio = Double(cambioTxt.text!)
            if dcambio == nil {
                cambioTxt.setError("Error", show: true)
                return
            }
            let moneda2 = dataArrayTotales[selectedTotales][Totales.IdMoneda] as! Int64
            actualizarTipoCambio(moneda1: idMoneda, moneda2: moneda2, cambio: dcambio!)
        }
        if CantidadTxt.text == nil {
            CantidadTxt.setError("Error", show: true)
            return
        }
        var dCantidad = Double(CantidadTxt.text!.replacingOccurrences(of: ",", with: ""))
        if dCantidad == nil {
            CantidadTxt.setError("Error", show: true)
            return
        }

        var com:String? = descripcion.text
        if com?.isEmpty ?? true {
            com = nil
        }
        if _id == 0 {
            ///Nuevo
            if !prestado {
                dCantidad = dCantidad! * -1
            }
            let x = newPrestamo(cantidad: dCantidad!, idTotales: idTotales, idMoneda: idMoneda, idPersona: idPersona, comment: com, cambio: dcambio!, idMove: 0, date: date)
            let y = newMoveCuenta(cantidad: dCantidad! * dcambio!, idCuenta: idTotales)
            if x && y{
                navigationController?.popViewController(animated: true)
            }
        } else {
            ///Actualizar
            let x = actualizarCuentaPrestamo(cantidad: dCantidad! * dcambio!, idCuenta: idTotales, idPrestamo: _id)
            let y = updatePrestamo(id: _id, cantidad: dCantidad!, idTotales: idTotales, idMoneda: idMoneda, idPersona: idPersona, comment: com, cambio: dcambio!, date: date)
            if x && y{
                navigationController?.popViewController(animated: true)
            }
        }
    
    }
    
}
 
extension PrestamosDetViewController: UITableViewDelegate, UITableViewDataSource {
    
    func showDetPrestamo(data:[String:Any?]?) {
        let alert = PresatmosDetAlertViewController(title: "Add Payment", message: "", preferredStyle: .alert)
        
        alert.addAction(UIAlertAction(title: "Guardar", style: .default, handler: {(a: UIAlertAction!) in
            print("Foo")
            if data != nil {
                if alert.cantTextField.text == nil {
                    alert.cantTextField.setError("Error", show: true)
                    return
                }
                var dCantidad = Double(alert.cantTextField.text!.replacingOccurrences(of: ",", with: ""))
                if dCantidad == nil {
                    alert.cantTextField.setError("Error", show: true)
                    return
                }
                if alert.cambioTextField.text == nil {
                    alert.cambioTextField.setError("Error", show: true)
                    return
                }
                let dCambio = Double(alert.cambioTextField.text!.replacingOccurrences(of: ",", with: ""))
                if dCambio == nil {
                    alert.cambioTextField.setError("Error", show: true)
                    self.present(alert, animated: true, completion: nil)
                    return
                }
                let id = data!["_id"] as! Int64
                let oldIdTotales = data![PrestamosDet.IdTotales] as! Int64
                let oldCant = data![PrestamosDet.Cantidad] as! Double
                let idMove = self.dataArrayPrestamo[0][Prestamos.IdMovimiento]
                
                if alert.gasto {
                    dCantidad = dCantidad! * -1
                }
                
                let _ = updatePrestamoDetalle(cantidad: dCantidad!, idTotales: alert.idTotales, idMoneda: alert.idMoneda, idPrestamo: self._id, cambio: dCambio!, _id: id, date: alert.date)
                if(idMove != nil && (idMove as! Int64) != 0){
                    let _ = updateTotalesFromPrestamo(cantidad: oldCant, idCuenta: oldIdTotales)
                    let _ = updateTotalesFromPrestamo(cantidad: (dCantidad! * -1), idCuenta: alert.idTotales)
                } else {
                    let _ = updateTotalesFromPrestamo(cantidad: (oldCant * -1), idCuenta: oldIdTotales)
                    let _ = updateTotalesFromPrestamo(cantidad: dCantidad!, idCuenta: alert.idTotales)
                }
            }
            self.dataArrayDetalle = getPrestamoDetalle(id: self._id)
            self.tableView.reloadData()
        }))
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.data = data
        alert.monedaPrestamo = idMoneda
        present(alert, animated: true, completion: nil)
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArrayDetalle.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let data = dataArrayDetalle[indexPath.row]
        //let cell: UITableViewCell=
        let cell = tableView.dequeueReusableCell(withIdentifier: "PrestamosDet") as! PrestamosDetTableViewCell
        cell.setCell(data: data)
        return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let data = dataArrayDetalle[indexPath.row]
        showDetPrestamo(data:data)
    }
}
