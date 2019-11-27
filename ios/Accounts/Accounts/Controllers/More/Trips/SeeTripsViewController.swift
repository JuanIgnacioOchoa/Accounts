//
//  SeeTripsViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class SeeTripsViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate, UITableViewDelegate, UITableViewDataSource {
    @IBAction func addBtn(_ sender: UIButton) {
        selectedId = 0
        self.performSegue(withIdentifier: "verMovimiento", sender: self)
    }
    
    @IBAction func saveBtn(_ sender: UIButton) {
        guardar()
    }
    @IBOutlet weak var NombrTxt: UITextField!
    @IBOutlet weak var MonedaLbl: UILabel!
    @IBOutlet weak var FechaIniTxt: UILabel!
    @IBOutlet weak var FechaFinTxt: UILabel!
    @IBOutlet weak var DecrTxt: UITextView!
    @IBOutlet weak var tableView: UITableView!
    
    var idTrip:Int64 = 0
    let datePickerFin = UIDatePicker(), datePickerInic = UIDatePicker()
    var pickerMoneda = UIPickerView()
    var dataArrayTrip:[[String:Any?]] = [], dataArrayMoneda:[[String:Any?]] = []
    var _id:Int64 = 0, idMoneda:Int64 = 0
    var selectedMoneda = 0
    var oldSelectedMoneda = 0
    var dateInic:Date = Date(), dateFin = Date()
    let txtTypeNumber = 0, txtTypeAll = 1, txtTypeNone = 2
    let numberFormatter = NumberFormatter()
    var comment:String?
    var nombre:String = ""
    var fechaDataSource: [[String:Any?]] = []
    var selectedId:Int64 = 0
    /*

     */
    override func viewDidLoad() {
        super.viewDidLoad()

        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        
        
        
        if _id == 0 {
            dataArrayMoneda = getMonedas()
            idMoneda = dataArrayMoneda[0]["_id"] as! Int64
            comment = nil
            selectedMoneda = 0
            oldSelectedMoneda = selectedMoneda
            nombre = ""
            tableView.isHidden = true
        } else {
            dataArrayTrip = getTripsId(id: _id)
            nombre = dataArrayTrip[0][Trips.Nombre] as! String
            idMoneda = dataArrayTrip[0][Trips.IdMoneda] as! Int64
            comment = dataArrayTrip[0][Trips.Descripcion] as? String
            dataArrayMoneda = getMonedasWith(id: idMoneda)
            let fechaIni = dataArrayTrip[0][Trips.FechaInicio] as! String
            let fechaFin = dataArrayTrip[0][Trips.FechaFin] as! String
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            dateInic = dateFormatter.date(from: fechaIni)!
            dateFin = dateFormatter.date(from: fechaFin)!
            var c = 0
            for m in dataArrayMoneda {
                if m["_id"] as! Int64 == idMoneda{
                    selectedMoneda = c
                    oldSelectedMoneda = selectedMoneda
                }
                c = c + 1
            }
            dateFormatter.dateFormat = "dd-MMM-yyyy"
            FechaIniTxt.text = dateFormatter.string(from: dateInic)
            FechaFinTxt.text = dateFormatter.string(from: dateFin)
            fechaDataSource = getTripsMovimientosFecha(id: _id)
            tableView.isHidden = false
            tableView.allowsSelection = false
            tableView.dataSource = self
            tableView.delegate = self
        }
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapFechaIni))
        FechaIniTxt.addGestureRecognizer(tap)
        let tap2 = UITapGestureRecognizer(target: self, action: #selector(self.tapFechaFin))
        FechaFinTxt.addGestureRecognizer(tap2)
        FechaFinTxt.isUserInteractionEnabled = true
        FechaIniTxt.isUserInteractionEnabled = true
        MonedaLbl.text = dataArrayMoneda[selectedMoneda][Moneda.Moneda] as? String
        NombrTxt.text = nombre
        DecrTxt.text = comment
        preparePickerView()
    }
    @objc
    func tapFechaIni() {
        let txt = UITextField()
        txt.delegate = self
        self.view.addSubview(txt)
        showDatePickerIni(fechaTxt: txt)
        //txt.removeFromSuperview()
    }
    @objc
    func tapFechaFin() {
        let txt = UITextField()
        txt.delegate = self
        self.view.addSubview(txt)
        showDatePickerFin(fechaTxt: txt)
        //txt.removeFromSuperview()
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
        pickerMoneda.delegate = self
        pickerMoneda.dataSource = self
        
        
        pickerMoneda.selectRow(selectedMoneda, inComponent: 0, animated: true)

        
        //showDatePicker()
    }
    
    func showDatePickerIni(fechaTxt:UITextField){
        //Formate Date
        datePickerInic.datePickerMode = .date
        datePickerInic.date = dateInic
        //ToolBar
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donedatePickerInic));
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelDatePickerInic));

        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)

        fechaTxt.inputAccessoryView = toolbar
        fechaTxt.inputView = datePickerInic
        fechaTxt.becomeFirstResponder()
    }
    
    func showDatePickerFin(fechaTxt:UITextField){
        //Formate Date
        datePickerFin.datePickerMode = .date
        datePickerFin.date = dateFin
        //ToolBar
        let toolbar = UIToolbar();
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(title: "Done", style: .plain, target: self, action: #selector(donedatePickerFin));
        let spaceButton = UIBarButtonItem(barButtonSystemItem: UIBarButtonItem.SystemItem.flexibleSpace, target: nil, action: nil)
        let cancelButton = UIBarButtonItem(title: "Cancel", style: .plain, target: self, action: #selector(cancelDatePickerFin));

        toolbar.setItems([doneButton,spaceButton,cancelButton], animated: false)

        fechaTxt.inputAccessoryView = toolbar
        fechaTxt.inputView = datePickerFin
        fechaTxt.becomeFirstResponder()
    }
    @objc func donedatePickerInic(){
        let formatter = DateFormatter()
        formatter.dateFormat = "dd-MMM-yyyy"
        //fechaTxt.text = formatter.string(from: datePicker.date)
        FechaIniTxt.text = formatter.string(from: datePickerInic.date)
        dateInic = datePickerInic.date
        self.view.endEditing(true)
    }
    
    @objc func cancelDatePickerInic(){
        self.view.endEditing(true)
    }
    
    @objc func donedatePickerFin(){
        let formatter = DateFormatter()
        formatter.dateFormat = "dd-MMM-yyyy"
        //fechaTxt.text = formatter.string(from: datePicker.date)
        FechaFinTxt.text = formatter.string(from: datePickerFin.date)
        dateFin = datePickerFin.date
        self.view.endEditing(true)
    }
    


    @objc func cancelDatePickerFin(){
        self.view.endEditing(true)
    }
    @objc func donePicker(_ btn: UIBarButtonItem){

        idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64

        oldSelectedMoneda = selectedMoneda
        
        self.view.endEditing(true)
    }
    
    @objc func cancelPicker(){
        selectedMoneda = oldSelectedMoneda
        self.view.endEditing(true)
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
        MonedaLbl.text = dataArrayMoneda[row][Moneda.Moneda] as? String
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

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return fechaDataSource.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        //let cell: UITableViewCell
        let cell = tableView.dequeueReusableCell(withIdentifier: "FechaTableCell") as! FechaTableViewCell
        //if(index == 0){
        let fecha = fechaDataSource[row][Movimiento.Fecha] as! String
        let data = getTripsMovimientosByDate(id: _id, date: fecha)
        tableView.rowHeight = CGFloat(40 + (25 * data.count))
        cell.setCell(fecha: fecha, data:data, parent: self, parentType: 4)
        //} else {
            //cell.setCell(fecha: "ABCD", parent: tableView)
        //}
        
        return cell
    }
    
   override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
       
       if "verMovimiento" == segue.identifier {
            let viewController = segue.destination as! SeeMovimientoViewController
            viewController.title = "Ver Movimiento"
            viewController._id = selectedId
            viewController.idTrip = _id
       }
   }
    
    func guardar(){
        if NombrTxt.text == nil || NombrTxt.text!.isEmpty {
            NombrTxt.setError("Necesita Nombre", show: true)
            return
        }
        nombre = NombrTxt.text!
        comment = DecrTxt.text
        idMoneda = dataArrayMoneda[selectedMoneda]["_id"] as! Int64
        if !actualizarTrip(id: _id, name: nombre, desc: comment, fechaInic: dateInic, fechaFin: dateFin, idMoneda: idMoneda) {
            self.navigationController?.popViewController(animated: true)
        }
    }
}
