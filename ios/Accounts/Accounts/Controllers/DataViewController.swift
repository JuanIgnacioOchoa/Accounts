//
//  DataViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class DataViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource {

    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var display: UILabel!
    @IBOutlet weak var inactivasSwitch: UISwitch!
    @IBOutlet weak var inactivasLbl: UILabel!
    @IBOutlet weak var positivoLbl: UILabel!
    @IBOutlet weak var minusSign: UILabel!
    @IBOutlet weak var negativoLbl: UILabel!
    @IBOutlet weak var equalsSign: UILabel!
    @IBOutlet weak var totalLbl: UILabel!
    @IBOutlet weak var monedaLbl: UILabel!
    @IBAction func switchPressed(_ sender: UISwitch) {
        dataArrayTotales = getTotales(inactivos: sender.isOn)
        tableView.reloadData()
    }
    var blurEffect = UIBlurEffect(style: UIBlurEffect.Style.light)
    var blurEffectView:UIVisualEffectView?
    
    var picker = UIPickerView()
    var displayString: String?
    var index: Int?
    var dataArrayTotales:[[String:Any?]]?
    var dataArrayMovimientos:[[String:Any?]]?
    var totalesVisible = true
    var movimientosVisible = true
    var monedas:[[String:Any?]]?
    var selectedMoneda = 0
    
    let numberFormatter = NumberFormatter()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        display.text = displayString
        dataArrayTotales = getTotales(inactivos: false)
        dataArrayMovimientos = getMovimientos()
        monedas = getMonedas()
        inactivasSwitch.isHidden = totalesVisible
        inactivasLbl.isHidden = totalesVisible
        //positivoLbl.isHidden = totalesVisible
        minusSign.isHidden = totalesVisible
        //negativoLbl.isHidden = totalesVisible
        equalsSign.isHidden = totalesVisible
        totalLbl.isHidden = totalesVisible
        
        let screenSize:CGRect = UIScreen.main.bounds
        var pickerRect = picker.frame
        pickerRect.origin.x = 5// some desired value
        pickerRect.origin.y = screenSize.size.height - 400// some desired value
        picker.frame = pickerRect
        picker.frame.size.height = 150
        picker.frame.size.width = screenSize.width - 20
        picker.backgroundColor = UIColor.yellow
        picker.setValue(UIColor.black, forKey: "textColor")
        picker.autoresizingMask = .flexibleWidth
        picker.contentMode = .center
        
        picker.delegate = self
        picker.dataSource = self
        blurEffectView = UIVisualEffectView(effect: blurEffect)
        blurEffectView!.frame = view.bounds
        blurEffectView!.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        let tap = UITapGestureRecognizer(target: self, action: #selector(tap(gestureReconizer:)))
        monedaLbl.addGestureRecognizer(tap)
        monedaLbl.isUserInteractionEnabled = true
        
        numberFormatter.numberStyle = .decimal
        numberFormatter.minimumFractionDigits = 2
        let negativo = getGastoTotalByMonedaFromCurrentMonth(moneda: Int(monedas![selectedMoneda]["_id"] as! Int64)) as NSNumber
        negativoLbl.text = numberFormatter.string(from: negativo)
        let positivo = getIngresoTotalByMonedaFromCurrentMonth(moneda: Int(monedas![selectedMoneda]["_id"] as! Int64)) as NSNumber
        positivoLbl.text = numberFormatter.string(from: positivo)
        let total = ((positivo as! Double) - (negativo as! Double)) as NSNumber
        totalLbl.text = numberFormatter.string(from: total)
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return monedas!.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return monedas![row][Moneda.Moneda] as? String
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        selectedMoneda = row
        monedaLbl.text = monedas![row][Moneda.Moneda] as? String
        self.view.endEditing(true)
        let negativo = getGastoTotalByMonedaFromCurrentMonth(moneda: Int(monedas![selectedMoneda]["_id"] as! Int64)) as NSNumber
        negativoLbl.text = numberFormatter.string(from: negativo)
        let positivo = getIngresoTotalByMonedaFromCurrentMonth(moneda: Int(monedas![selectedMoneda]["_id"] as! Int64)) as NSNumber
        positivoLbl.text = numberFormatter.string(from: positivo)
        let total = ((positivo as! Double) - (negativo as! Double)) as NSNumber
        totalLbl.text = numberFormatter.string(from: total)
        sleep(UInt32(0.5))
        blurEffectView!.removeFromSuperview()
        picker.removeFromSuperview()
    }
    
    @objc func tap(gestureReconizer: UITapGestureRecognizer) {
        view.addSubview(blurEffectView!)
        view.addSubview(picker)
        picker.selectRow(selectedMoneda, inComponent: 0, animated: true)
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        let touch = touches.first
        //if touch?.view == self.view {
            picker.removeFromSuperview()
            blurEffectView?.removeFromSuperview()
        //}
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if "seeCuentasSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let viewController = segue.destination as! SeeCuentaViewController
                let cuenta = dataArrayTotales![indexPath.row][Totales.Cuenta] as! String
                viewController.test = "Holiii----\(cuenta)"
                navigationItem.title = "Pruebaaa"
            }
        } else if "seeMovimientosSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let viewController = segue.destination as! SeeMovimientoViewController
                let cuenta = dataArrayMovimientos![indexPath.row][Movimiento.Cantidad] as! Double
                viewController.test = "Holiii----\(cuenta)"
                navigationItem.title = "Pruebaaa"
            }
        }
    }
    
}

extension DataViewController: UITableViewDelegate, UITableViewDataSource{
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if(index == 0){
            tableView.rowHeight = 50
            
            return dataArrayTotales?.count ?? 0
        } else {
            tableView.rowHeight = 100
            return dataArrayMovimientos?.count ?? 0
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        //let data = dataArray[indexPath.row]
        let row = indexPath.row
        //let cell: UITableViewCell
        if(index == 0){
            let cell = tableView.dequeueReusableCell(withIdentifier: "MainCuentasCell") as! MainCuentasTableViewCell
            cell.setCell(data: dataArrayTotales![row])
            return cell
        } else {
            let cell = tableView.dequeueReusableCell(withIdentifier: "MainMovCell") as! MainMovTableViewCell
            cell.setCell(data: dataArrayMovimientos![row])
            return cell
        }
        //cell.setCell(data: data)
        //return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //seeCuentasSegue
        //let row = indexPath.row
        if(index == 0){
            self.performSegue(withIdentifier: "seeCuentasSegue", sender: nil)
        } else if index == 1 {
            self.performSegue(withIdentifier: "seeMovimientosSegue", sender: nil)
        }
    }

}
