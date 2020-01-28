//
//  CreateCuentasViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/19/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class CreateCuentasViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    @IBAction func cancelBtn(_ sender: UIButton) {
        navigationController?.popViewController(animated: true)
    }
    @IBAction func addBtn(_ sender: UIButton) {
        guardarCuenta()
    }
    @IBOutlet weak var nameTxt: UITextField!
    @IBOutlet weak var cantidadTxt: UITextField!
    @IBOutlet var tipoLbl: UILabel!
    @IBOutlet var monedaLbl: UILabel!
    
    let monedaTxt = UITextField(), tipoTxt = UITextField()
    var pickerMoneda = UIPickerView(), pickerTipo = UIPickerView()
    var dataArrayTipo:[[String:Any?]] = [], dataArrayMoneda:[[String:Any?]] = []
    var selectedMoneda = 0, selectedTipo = 0
    var oldSelectedMoneda = 0, oldSelectedTipo = 0
    var idMoneda:Int64 = 0, idTipo:Int64 = 0
    let monedaTag = 0, tipoTag = 1
    let txtTypeNumber = 0, txtTypeAll = 1, txtTypeNone = 2
    var pickersActive = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        hideKeyboardWhenTappedAround()
        
        title = "Crear Cuenta"
        
        dataArrayTipo = getTiposCuentas()
        Utils.preparePickerView(picker: pickerMoneda)
        Utils.preparePickerView(picker: pickerTipo)
        pickerMoneda.delegate = self
        pickerMoneda.dataSource = self
        pickerTipo.delegate = self
        pickerTipo.dataSource = self
        pickerMoneda.tag = monedaTag
        pickerTipo.tag = tipoTag
        
        var toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        var doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        monedaTxt.inputAccessoryView = toolbar
        monedaTxt.inputView = pickerMoneda
        tipoTxt.inputAccessoryView = toolbar
        tipoTxt.inputView = pickerTipo
        
        self.view.addSubview(monedaTxt)
        self.view.addSubview(tipoTxt)
        
        let tapM = UITapGestureRecognizer(target: self, action: #selector(self.tapMoneda))
        monedaLbl.addGestureRecognizer(tapM)
        monedaLbl.isUserInteractionEnabled = true
        let tapT = UITapGestureRecognizer(target: self, action: #selector(self.tapTipo))
        tipoLbl.addGestureRecognizer(tapT)
        tipoLbl.isUserInteractionEnabled = true
        
        
        tipoLbl.text = dataArrayTipo[selectedTipo][TiposCuentas.Tipo] as? String
        idTipo = dataArrayTipo[selectedTipo]["_id"] as! Int64
        
        toolbar = UIToolbar();
        toolbar.sizeToFit()
        doneButton = UIBarButtonItem(title: "Done", style: .plain, target: nil, action: #selector(endEditing))

        toolbar.setItems([doneButton], animated: false)

        cantidadTxt.inputAccessoryView = toolbar
        nameTxt.inputAccessoryView = toolbar
        
        cantidadTxt.tag = txtTypeNumber
        nameTxt.tag = txtTypeAll
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        dataArrayMoneda = getMonedas()
        if dataArrayMoneda.count <= 0 {
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
        } else {
            monedaLbl.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
            idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        }
    }
    @objc func endEditing(){
        self.view.endEditing(true)
    }
    @objc
    func tapMoneda(sender:UITapGestureRecognizer) {
        monedaTxt.becomeFirstResponder()
    }
    
    @objc
    func tapTipo(sender:UITapGestureRecognizer) {
        tipoTxt.becomeFirstResponder()
    }
    
    func guardarCuenta(){
        if cantidadTxt.text == nil {
            cantidadTxt.setError("Error", show: true)
            return
        }
        var dCant = Double(cantidadTxt.text!)
        if dCant == nil {
            cantidadTxt.setError("Error", show: true)
            return
        }
        if nameTxt.text == nil || nameTxt.text!.isEmpty{
            nameTxt.setError("Empty String", show: true)
            return
        }
        if newTotales(cantidad: dCant!, cuenta: nameTxt.text!, idMoneda: idMoneda, tipoCuenta: idTipo) {
            navigationController?.popViewController(animated: true)
        }
    }
    
    @objc func donePicker(_ btn: UIBarButtonItem){
        oldSelectedMoneda = selectedMoneda
        oldSelectedTipo = selectedTipo
        idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        idTipo = dataArrayTipo[selectedTipo]["_id"] as! Int64
        pickersActive = false
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedMoneda = oldSelectedMoneda
        selectedTipo = oldSelectedTipo
        monedaLbl.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
        tipoLbl.text = dataArrayTipo[selectedTipo][TiposCuentas.Tipo] as? String
        pickersActive = false
        self.view.endEditing(true)
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
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView.tag == monedaTag {
            return dataArrayMoneda.count
        } else if pickerView.tag == tipoTag {
            return dataArrayTipo.count
        } else {
            return 0
        }
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView.tag == monedaTag {
            return dataArrayMoneda[row][Moneda.Moneda] as? String
        } else if pickerView.tag == tipoTag {
            return dataArrayTipo[row][TiposCuentas.Tipo] as? String
        } else {
            return "Unknown"
        }
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView.tag == monedaTag {
            selectedMoneda = row
            monedaLbl.text = dataArrayMoneda[row][Moneda.Moneda] as? String
        } else if pickerView.tag == tipoTag {
            selectedTipo = row
            tipoLbl.text = dataArrayTipo[row][TiposCuentas.Tipo] as? String
        }
        pickersActive = true
    }
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
       
        if "agregarMoneda" == segue.identifier {
            let viewController = segue.destination as! MonedaMainViewController
            viewController.title = "Monedas"
        }
    }
}
extension CreateCuentasViewController {
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
