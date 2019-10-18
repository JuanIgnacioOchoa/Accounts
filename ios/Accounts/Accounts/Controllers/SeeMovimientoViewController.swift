//
//  SeeMovimientoViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/20/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class SeeMovimientoViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    
    @IBAction func guardarBtn(_ sender: UIButton) {
        guardar()
    }
    @IBOutlet weak var fechaTxt: UITextField!
    @IBOutlet weak var monedaTxt: UITextField!
    @IBOutlet weak var cuentaTxt: UITextField!
    @IBOutlet weak var motivoTxt: UITextField!
    @IBOutlet weak var cantidadTxt: UITextField!
    @IBOutlet weak var cambioLbl: UILabel!
    @IBOutlet weak var cambioTxt: UITextField!
    @IBOutlet weak var commentTxt: UITextView!
    
    var _id:Int64? = 0
    let monedaTag = 1
    let totalesTag = 2
    let motivoTag = 3
    let datePicker = UIDatePicker()
    var pickerMoneda = UIPickerView()
    var pickerMotivo = UIPickerView()
    var pickerTotales = UIPickerView()
    var dataArrayMove:[[String:Any?]] = []
    var dataArrayMoneda:[[String:Any?]] = []
    var dataArrayMotivo:[[String:Any?]] = []
    var dataArrayTotales:[[String:Any?]] = []
    var cantidad:NSNumber = 0
    var idMoneda:Int64 = 1
    var idTotales:Int64 = 1
    var idMotivo:Int64 = 1
    var comment:String?
    var fecha:String = ""
    var motivo:String = ""
    var moneda:String = ""
    var totales:String = ""
    var selectedTotales = 0
    var selectedMoneda = 0
    var selectedMotivo = 0
    var oldSelectedTotales = 0
    var oldSelectedMoneda = 0
    var oldSelectedMotivo = 0
    var date:Date = Date()
    var cambio:NSNumber = 1.0
    var cambioH = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let numberFormatter = NumberFormatter()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        
        dataArrayMoneda = getMonedas()
        if _id == nil {
            dataArrayTotales = getTotales()
            dataArrayMotivo = getMotives()
            
            cantidad = 0.0
            idMoneda = dataArrayMoneda[0]["_id"] as! Int64
            idTotales = dataArrayTotales[0]["_id"] as! Int64
            idMotivo = dataArrayMotivo[0]["_id"] as! Int64
            comment = ""
            fecha = "21/09/2019"
            moneda = dataArrayMoneda[0][Moneda.Moneda] as! String
            totales = dataArrayTotales[0][Totales.Cuenta] as! String
            motivo = dataArrayMotivo[0][Motivo.Motivo] as! String
        } else {
            dataArrayMove = getMoveData(id: _id!)
            
            
            cantidad = dataArrayMove[0][Movimiento.Cantidad] as! NSNumber
            idMoneda = dataArrayMove[0][Movimiento.IdMoneda] as! Int64
            idTotales = dataArrayMove[0][Movimiento.IdTotales] as! Int64
            idMotivo = dataArrayMove[0][Movimiento.IdMotivo] as! Int64
            comment = dataArrayMove[0][Movimiento.Comment] as? String
            fecha = dataArrayMove[0][Movimiento.Fecha] as! String
            
            dataArrayMotivo = getMotives(id: idMotivo)
            dataArrayTotales = getTotales(id: idTotales)
            
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
        }
        
        preparePickerView()
        
        let monedaCuenta = dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64
        let monedaMov = dataArrayMoneda[selectedMoneda]["_id"] as? Int64
        cambioH = monedaCuenta == monedaMov
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        if !cambioH {
            if _id == nil {
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
        fechaTxt.delegate = self
        cambioTxt.text = numberFormatter.string(from: cambio)
        cantidadTxt.text = numberFormatter.string(from: cantidad)
        monedaTxt.text = moneda
        cuentaTxt.text = totales
        motivoTxt.text = motivo
        fechaTxt.text = fecha
        
    }
    
    func showDatePicker(){
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

    }

    @objc func donedatePicker(){
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        fechaTxt.text = formatter.string(from: datePicker.date)
        date = datePicker.date
        self.view.endEditing(true)
    }
    


    @objc func cancelDatePicker(){
        
        self.view.endEditing(true)
    }
    
    @objc func donePicker(_ btn: UIBarButtonItem){
        oldSelectedTotales = selectedTotales
        oldSelectedMoneda = selectedMoneda
        oldSelectedMotivo = selectedMotivo
        idTotales = dataArrayTotales[selectedTotales]["_id"] as! Int64
        idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        idMotivo = dataArrayMotivo[selectedMotivo]["_id"] as! Int64
        
        let monedaCuenta = dataArrayTotales[selectedTotales][Totales.IdMoneda] as? Int64
        let monedaMov = dataArrayMoneda[selectedMoneda]["_id"] as? Int64
        cambioH = monedaCuenta == monedaMov
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        
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
        cambioLbl.isHidden = cambioH
        cambioTxt.isHidden = cambioH
        self.view.endEditing(true)
    }
    
    func preparePickerView(){
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
        pickerMoneda.tag = monedaTag
        pickerMoneda.delegate = self
        pickerMoneda.dataSource = self
        
        pickerRect = pickerTotales.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height - 400// some desired value
        pickerTotales.frame = pickerRect
        pickerTotales.frame.size.height = 150
        pickerTotales.frame.size.width = screenSize.width - 20
        pickerTotales.backgroundColor = UIColor.yellow
        pickerTotales.setValue(UIColor.black, forKey: "textColor")
        pickerTotales.autoresizingMask = .flexibleWidth
        pickerTotales.contentMode = .center
        pickerTotales.tag = totalesTag
        pickerTotales.delegate = self
        pickerTotales.dataSource = self
        
        pickerRect = pickerMotivo.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height - 400// some desired value
        pickerMotivo.frame = pickerRect
        pickerMotivo.frame.size.height = 150
        pickerMotivo.frame.size.width = screenSize.width - 20
        pickerMotivo.backgroundColor = UIColor.yellow
        pickerMotivo.setValue(UIColor.black, forKey: "textColor")
        pickerMotivo.autoresizingMask = .flexibleWidth
        pickerMotivo.contentMode = .center
        pickerMotivo.tag = motivoTag
        pickerMotivo.delegate = self
        pickerMotivo.dataSource = self
        
        pickerRect = datePicker.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height - 400// some desired value
        datePicker.frame = pickerRect
        datePicker.frame.size.height = 150
        datePicker.frame.size.width = screenSize.width - 20
        datePicker.backgroundColor = UIColor.yellow
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
        
        showDatePicker()
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
            //return dataArrayMoneda.count
        } else if pickerView.tag == totalesTag {
            selectedTotales = row
            cuentaTxt.text = dataArrayTotales[row][Totales.Cuenta] as? String
            //return dataArrayTotales.count
        } else if pickerView.tag == motivoTag {
            selectedMotivo = row
            motivoTxt.text = dataArrayMotivo[row][Motivo.Motivo] as? String
            //return  dataArrayMotivo.count
        }
        

        //selectedMoneda = row
        
        //blurEffectView!.removeFromSuperview()
        //picker.removeFromSuperview()
        //sleep(UInt32(0.5))
        //pickerView.removeFromSuperview()
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        return false
    }

    func guardar(){
        var dcambio = Double(cambioTxt.text ?? "0.0")
        if cambioH {
            dcambio = 1.0
        } else {
            let moneda2 = dataArrayTotales[selectedTotales][Totales.IdMoneda] as! Int64
            actualizarTipoCambio(moneda1: idMoneda, moneda2: moneda2, cambio: dcambio!)
        }
        //cantidad = NSNumber(pointer: cantidadTxt.text)
        let dCantidad = Double(cantidadTxt.text ?? "0.0")
        let com:String? = commentTxt.text
        let g = "cantidad: \(dCantidad ?? 0.0), idTotales: \(idTotales), comment: \(com)), idMotivo: \(idMotivo), idMoneda: \(idMoneda), cambio: \(dcambio)"
        print(g)
        if _id == nil {
            ///Nuevo
            let x = newMove(cantidad: dCantidad ?? 0.0, idCuenta: idTotales, comment: com, idMotivo: idMotivo, idMoneda: idMoneda, cambio: dcambio!, date: date)
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
}
