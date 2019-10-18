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
        guardar()
    }
    @IBOutlet weak var nameTxt: UITextField!
    @IBOutlet weak var cantidadTxt: UITextField!
    @IBOutlet weak var monedaTxt: UITextField!
    var pickerMoneda = UIPickerView()
    var dataArrayMoneda:[[String:Any?]] = []
    var selectedMoneda = 0
    var oldSelectedMoneda = 0
    var idMoneda:Int64 = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "Crear Cuenta"
        
        dataArrayMoneda = getMonedas()
        
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
        
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        monedaTxt.inputAccessoryView = toolbar
        monedaTxt.inputView = pickerMoneda
        monedaTxt.delegate = self
        
        monedaTxt.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
    }
    
    func guardar(){
        let dCant = Double(cantidadTxt.text ?? "0.0")
        if newTotales(cantidad: dCant!, cuenta: nameTxt.text!, idMoneda: idMoneda) {
            navigationController?.popViewController(animated: true)
        }
    }
    
    @objc func donePicker(_ btn: UIBarButtonItem){
        oldSelectedMoneda = selectedMoneda
        idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedMoneda = oldSelectedMoneda
        monedaTxt.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
        self.view.endEditing(true)
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        return false
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return dataArrayMoneda.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return dataArrayMoneda[row][Moneda.Moneda] as? String
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        selectedMoneda = row
        monedaTxt.text = dataArrayMoneda[row][Moneda.Moneda] as? String
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
