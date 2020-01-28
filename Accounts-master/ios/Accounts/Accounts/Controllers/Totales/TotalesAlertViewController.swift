//
//  TotalesAlertViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 28/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class TotalesAlertViewController: UIAlertController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate {

    let txtTypeNumber = 0, txtTypeAll = 1, txtTypeNone = 2
    let pickerTipo = UIPickerView()
    var cantTextField = UITextField(), cuentaTxt = UITextField(), idTipoTxt = UITextField()
    var checkbox = CheckBox()
    var activeLbl = UILabel(), cuentaLbl = UILabel(), cantidadLbl = UILabel(), tipoLbl = UILabel(), idTipoLbl = UILabel()
    let numberFormatter = NumberFormatter()
    var dataArrayTipos:[[String:Any?]] = []
    var selectedTipo = 0
    var oldSelectedTipo = 0
    var idTipo:Int64 = 0
    var cuenta = ""
    var cantidad:Double = 0.0
    var activa:Bool = true
    
    override func viewDidLoad() {
        super.viewDidLoad()
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        dataArrayTipos = getTiposCuentas()
        
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
        
        let height:NSLayoutConstraint = NSLayoutConstraint(item: self.view!, attribute: NSLayoutConstraint.Attribute.height, relatedBy: NSLayoutConstraint.Relation.equal, toItem: nil, attribute: NSLayoutConstraint.Attribute.notAnAttribute, multiplier: 1, constant: 250)
        self.view.addConstraint(height);

        let marginX:CGFloat = 10.0
        let marginY:CGFloat = 65
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width - (marginX * 2) - (screenSize.width * 0.1)
        let rect = CGRect(x: marginX, y: marginY, width: screenWidth, height: 120)
        let customView = UIView(frame: rect)

        checkbox = CheckBox(frame: CGRect(x:0, y:0, width: 25, height: 25))
        activeLbl = UILabel(frame: CGRect(x:30, y:0, width: customView.frame.width - 30, height:25))
        cuentaLbl = UILabel(frame: CGRect(x:0, y:30, width: customView.frame.width/2, height: 25))
        cuentaTxt = UITextField(frame: CGRect(x:customView.frame.width/2, y:30, width: customView.frame.width/2, height: 25))
        cantidadLbl = UILabel(frame: CGRect(x:0, y:60, width: customView.frame.width/2, height: 25))
        cantTextField = UITextField(frame: CGRect(x: customView.frame.width/2, y: 60, width: customView.frame.width/2, height: 25))
        tipoLbl = UILabel(frame: CGRect(x:0, y:90, width: customView.frame.width/2, height: 25))
        idTipoLbl = UILabel(frame: CGRect(x: customView.frame.width/2, y: 90, width: customView.frame.width/2, height: 25))
        
        cantTextField.delegate = self
        cuentaTxt.delegate = self
        
        cuentaTxt.tag = txtTypeAll
        cantTextField.tag = txtTypeNumber
        
        
        customView.addSubview(checkbox)
        customView.addSubview(activeLbl)
        customView.addSubview(cuentaLbl)
        customView.addSubview(cuentaTxt)
        customView.addSubview(cantidadLbl)
        customView.addSubview(cantTextField)
        customView.addSubview(tipoLbl)
        customView.addSubview(idTipoLbl)
        
        let tapT = UITapGestureRecognizer(target: self, action: #selector(self.tapTipo))
        idTipoLbl.addGestureRecognizer(tapT)
        idTipoLbl.isUserInteractionEnabled = true
        
        self.view.addSubview(customView)
        //cuentaLbl.text = "AAA"
        
        
        
        setValues()
        
        Utils.preparePickerView(picker: pickerTipo)
        
        pickerTipo.delegate = self
        pickerTipo.dataSource = self
        
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donePicker(_:)))
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelPicker));
        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)
        idTipoTxt.inputAccessoryView = toolbar
        idTipoTxt.inputView = pickerTipo
        
        pickerTipo.selectRow(selectedTipo, inComponent: 0, animated: true)
        
        checkbox.addTarget(self, action: #selector(cbPressed), for: .touchUpInside)
        
        self.view.addSubview(idTipoTxt)
    }
    
    @objc func cbPressed(sender:CheckBox){
        sender.isChecked = !sender.isChecked
    }
    
    @objc func donePicker(_ btn: UIBarButtonItem){
        idTipo = dataArrayTipos[selectedTipo][TiposCuentas.Tipo] as! Int64
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedTipo = oldSelectedTipo
        idTipoLbl.text = dataArrayTipos[selectedTipo][TiposCuentas.Tipo] as? String
        pickerTipo.selectRow(selectedTipo, inComponent: 0, animated: true)
        self.view.endEditing(true)
    }
    
    func setValues(){
        activeLbl.text = "Active Account"
        cuentaLbl.text = "Account Title"
        cantidadLbl.text = "Current Ammount"
        tipoLbl.text = "Account Type"
        var c = 0
        for t in dataArrayTipos {
            if t["_id"] as! Int64 == idTipo {
                selectedTipo = c
                oldSelectedTipo = c
            }
            c = c + 1
        }
        cantTextField.text = numberFormatter.string(from: NSNumber(value: cantidad))
        checkbox.isChecked = activa
        cuentaTxt.text = cuenta
        idTipoLbl.text = dataArrayTipos[selectedTipo][TiposCuentas.Tipo] as! String
        pickerTipo.selectRow(selectedTipo, inComponent: 0, animated: true)
    }
    @objc
    func tapTipo(sender:UITapGestureRecognizer) {
        idTipoTxt.becomeFirstResponder()
    }
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }

    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return dataArrayTipos.count
    }

    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return dataArrayTipos[row][TiposCuentas.Tipo] as! String
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        selectedTipo = row
        idTipoLbl.text = dataArrayTipos[row][TiposCuentas.Tipo] as? String
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
