//
//  DataViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class DataViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource {

    @IBAction func addTraspaso(_ sender: UIButton) {
        retiro = false
        self.performSegue(withIdentifier: "crearTraspaso", sender: nil)
    }
    @IBAction func addRetiro(_ sender: UIButton) {
        retiro = true
        self.performSegue(withIdentifier: "crearTraspaso", sender: nil)
    }
    @IBOutlet weak var contentTotal: UIView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var inactivasSwitch: UISwitch!
    @IBOutlet weak var inactivasLbl: UILabel!
    @IBOutlet weak var monedaLbl: UILabel!
    @IBAction func switchPressed(_ sender: UISwitch) {
        dataArrayTotales = getTotales(inactivos: sender.isOn)
        tableView.reloadData()
    }
    var blurEffect = UIBlurEffect(style: UIBlurEffect.Style.light)
    var blurEffectView:UIVisualEffectView?
    var idMoneda:Int64 = 1
    var picker = UIPickerView()
    var dataArrayTotales:[[String:Any?]] = []
    var monedas:[[String:Any?]]?
    var selectedMoneda = 0
    let numberFormatter = NumberFormatter()
    var retiro = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        monedas = getMonedas()
        
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
        

    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        dataArrayTotales = getTotales(inactivos: false)
        tableView.reloadData()
        
        if monedas!.count == 0 {
            //totalLbl.text = numberFormatter.string(from: 0)
            //negativoLbl.text = numberFormatter.string(from: 0)
            //positivoLbl.text = numberFormatter.string(from: 0)
            return
        }
        updateDataTotal()
    }
    
    func updateDataTotal(){
        contentTotal.subviews.forEach { $0.removeFromSuperview() }
        let cash = getTotalesCash(idMoneda: idMoneda, inactivos: false);
        let credit = getTotalesCreditCard(idMoneda: idMoneda, inactivos: false);
        let invest = getTotalesInvests(idMoneda: idMoneda, inactivos: false);
        let deudores = getTotalesDeudores(idMoneda: idMoneda);
        let deudas = getTotalesDeudas(idMoneda: idMoneda);
        let w = contentTotal.frame.width/2
        var y:CGFloat = 0
        let labelCash = UILabel(frame: CGRect(x:0, y: y, width: w, height: 20))
        let totalCash = UILabel(frame: CGRect(x:w, y: y, width: w, height: 20))
        labelCash.text = "Debito y Efectivo"
        totalCash.text = numberFormatter.string(from: NSNumber(value: cash))
        if cash < 0 {
            labelCash.textColor = UIColor.red
            totalCash.textColor = UIColor.red
        } else if cash > 0 {
            labelCash.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            totalCash.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
        totalCash.textAlignment = .right
        contentTotal.addSubview(labelCash)
        contentTotal.addSubview(totalCash)
        y = y + 20
        if credit != 0 {
            let labelCredit = UILabel(frame: CGRect(x:0, y: y, width: w, height: 20))
            let totalCredit = UILabel(frame: CGRect(x:w, y: y, width: w, height: 20))
            labelCredit.text = "Tarjetas de Credito"
            totalCredit.text = numberFormatter.string(from: NSNumber(value: credit))
            if credit < 0 {
                labelCredit.textColor = UIColor.red
                totalCredit.textColor = UIColor.red
            } else if credit > 0 {
                labelCredit.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
                totalCredit.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            }
            totalCredit.textAlignment = .right
            contentTotal.addSubview(labelCredit)
            contentTotal.addSubview(totalCredit)
            y = y + 20
        }
        let divisor1 = UILabel(frame: CGRect(x: 0, y: y, width: w*2, height: 2))
        divisor1.backgroundColor = UIColor.black
        contentTotal.addSubview(divisor1)
        y = y + 2
        let labelCorto = UILabel(frame: CGRect(x:0, y: y, width: w, height: 22))
        let totalCorto = UILabel(frame: CGRect(x:w, y: y, width: w, height: 22))
        labelCorto.text = "Cantidad Corto Plazo"
        totalCorto.text = numberFormatter.string(from: NSNumber(value: cash + credit))
        if (cash + credit) < 0 {
            labelCorto.textColor = UIColor.red
            totalCorto.textColor = UIColor.red
        } else if (credit + cash) > 0 {
            labelCorto.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            totalCorto.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
        totalCorto.textAlignment = .right
        contentTotal.addSubview(labelCorto)
        contentTotal.addSubview(totalCorto)
        y = y + 22
        let divisor2 = UILabel(frame: CGRect(x: 0, y: y, width: w*2, height: 2))
        divisor2.backgroundColor = UIColor.black
        contentTotal.addSubview(divisor2)
        y = y + 2
        if deudas != 0 {
            let labelDeudas = UILabel(frame: CGRect(x:0, y: y, width: w, height: 20))
            let totalDeudas = UILabel(frame: CGRect(x:w, y: y, width: w, height: 20))
            labelDeudas.text = "Deudas"
            totalDeudas.text = numberFormatter.string(from: NSNumber(value: deudas))
            if deudas < 0 {
                labelDeudas.textColor = UIColor.red
                totalDeudas.textColor = UIColor.red
            } else if deudas > 0 {
                labelDeudas.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
                totalDeudas.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            }
            totalDeudas.textAlignment = .right
            contentTotal.addSubview(labelDeudas)
            contentTotal.addSubview(totalDeudas)
            y = y + 20
        }
        if deudores != 0 {
            let labelDeudores = UILabel(frame: CGRect(x:0, y: y, width: w, height: 20))
            let totalDeudores = UILabel(frame: CGRect(x:w, y: y, width: w, height: 20))
            labelDeudores.text = "Deudores"
            totalDeudores.text = numberFormatter.string(from: NSNumber(value: deudores))
            if deudores < 0 {
                labelDeudores.textColor = UIColor.red
                totalDeudores.textColor = UIColor.red
            } else if deudores > 0 {
                labelDeudores.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
                totalDeudores.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            }
            totalDeudores.textAlignment = .right
            contentTotal.addSubview(labelDeudores)
            contentTotal.addSubview(totalDeudores)
            y = y + 20
        }
        if invest != 0 {
            let labelInvest = UILabel(frame: CGRect(x:0, y: y, width: w, height: 20))
            let totalInvest = UILabel(frame: CGRect(x:w, y: y, width: w, height: 20))
            labelInvest.text = "Inversiones"
            totalInvest.text = numberFormatter.string(from: NSNumber(value: invest))
            if invest < 0 {
                labelInvest.textColor = UIColor.red
                totalInvest.textColor = UIColor.red
            } else if invest > 0 {
                labelInvest.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
                totalInvest.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            }
            totalInvest.textAlignment = .right
            contentTotal.addSubview(labelInvest)
            contentTotal.addSubview(totalInvest)
            y = y + 20
        }
        let divisor3 = UILabel(frame: CGRect(x: 0, y: y, width: w*2, height: 3))
        divisor3.backgroundColor = UIColor.black
        contentTotal.addSubview(divisor3)
        y = y + 3
        let labelTotal = UILabel(frame: CGRect(x:0, y: y, width: w, height: 22))
        let totalTotal = UILabel(frame: CGRect(x:w, y: y, width: w, height: 22))
        labelTotal.text = "Total"
        totalTotal.text = numberFormatter.string(from: NSNumber(value: cash + credit + deudores + deudas + invest))
        if (cash + credit + deudores + deudas + invest) < 0 {
            labelTotal.textColor = UIColor.red
            totalTotal.textColor = UIColor.red
        } else if (cash + credit + deudores + deudas + invest) > 0 {
            labelTotal.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            totalTotal.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
        }
        totalTotal.textAlignment = .right
        contentTotal.addSubview(labelTotal)
        contentTotal.addSubview(totalTotal)
        
        labelCorto.font = UIFont(name:"HelveticaNeue-Bold", size: 18.0)
        totalCorto.font = UIFont(name:"HelveticaNeue-Bold", size: 18.0)
        labelTotal.font = UIFont(name:"HelveticaNeue-Bold", size: 22.0)
        totalTotal.font = UIFont(name:"HelveticaNeue-Bold", size: 22.0)
        
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
        idMoneda = monedas![row]["_id"] as! Int64
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
        if "crearTraspaso" == segue.identifier {
            let vc = segue.destination as! TraspasoViewController
            vc._id = 0
            vc.retiro = retiro
        }
        else if "seeCuentasSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let viewController = segue.destination as! SeeCuentaViewController
                //let cuenta = dataArrayTotales[indexPath.row][Totales.Cuenta] as! String
                //let moneda = dataArrayTotales[indexPath.row][Moneda.Moneda] as! String
                //viewController.title = "\(cuenta) \(moneda)"
                viewController._id = dataArrayTotales[indexPath.row]["_id"] as! Int64
            }
        }
    }
    
}

extension DataViewController: UITableViewDelegate, UITableViewDataSource{
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArrayTotales.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        let cell = tableView.dequeueReusableCell(withIdentifier: "MainCuentasCell") as! MainCuentasTableViewCell
        cell.setCell(data: dataArrayTotales[row])
        return cell
    }
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.performSegue(withIdentifier: "seeCuentasSegue", sender: nil)
    }
}
