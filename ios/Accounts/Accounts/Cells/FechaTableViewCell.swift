//
//  FechaTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 10/23/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class FechaTableViewCell: UITableViewCell{

    @IBOutlet weak var fechaLbl: UILabel!
    @IBOutlet weak var content: UIView!
    var data:[[String:Any?]] = []
    var parent: MovdataViewController?
    
    func setCell(fecha: String, data:[[String:Any?]], parent: MovdataViewController){
        self.parent = parent
        fechaLbl.text = fecha
        var y:CGFloat = 0
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width
        content.subviews.forEach { $0.removeFromSuperview() }
        for d in data {
            let new_view:UIView! = UIView(frame: CGRect(x: 5, y: y, width: screenWidth - 20, height: 20));
            //new_view.backgroundColor = UIColor.red;
            let w = new_view.frame.width/3
            let cantLbl = UILabel(frame: CGRect(x:w*2, y: 0, width: w, height: 20))
            let motivoLbl = UILabel(frame: CGRect(x:w, y: 0, width: w, height: 20))
            let cuentaLbl = UILabel(frame: CGRect(x:0, y: 0, width: w, height: 20))
            let numberFormatter = NumberFormatter()
            numberFormatter.numberStyle = .decimal
            numberFormatter.minimumFractionDigits = 2
            let cantidad = d[Movimiento.Cantidad] as! NSNumber
            let idTraspaso = d[Movimiento.Traspaso]
            let id = d["_id"] as! NSNumber
            let cuenta = d[Totales.Cuenta] as! String
            //let moneda = d[Moneda.Moneda] as! String
            let motivo = d[Motivo.Motivo] as! String
            
            cantLbl.text = numberFormatter.string(from: cantidad)
            cuentaLbl.text = cuenta
            motivoLbl.text = motivo
            cantLbl.textAlignment = .right
            
            new_view.addSubview(cantLbl)
            new_view.addSubview(cuentaLbl)
            new_view.addSubview(motivoLbl)
            if idTraspaso != nil{
                cantLbl.textColor = UIColor.init(red: 255/255, green: 119/255, blue: 0/255, alpha: 1.0)
                motivoLbl.textColor = UIColor.init(red: 255/255, green: 119/255, blue: 0/255, alpha: 1.0)
                cuentaLbl.textColor = UIColor.init(red: 255/255, green: 119/255, blue: 0/255, alpha: 1.0)
            }
            else if Int(truncating: cantidad) < 0{
                cantLbl.textColor = UIColor.red
                motivoLbl.textColor = UIColor.red
                cuentaLbl.textColor = UIColor.red
            } else if Int(truncating: cantidad) > 0 {
                cantLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
                motivoLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
                cuentaLbl.textColor = UIColor.init(red: 13/255, green: 72/255, blue: 4/255, alpha: 1.0)
            }
            let tap = MyTapGesture(target: self, action: #selector(self.tapFunction1))
            tap.id = Int64(truncating: id)
            tap.traspaso = idTraspaso as Any?
            new_view.addGestureRecognizer(tap)
            content.addSubview(new_view)
            y += 25
        }
    }
    @objc
    func tapFunction1(sender:MyTapGesture) {
        parent!.selectedId = sender.id
        if sender.traspaso == nil {
            parent!.performSegue(withIdentifier: "verMovimiento", sender: self)
        } else {
            parent!.performSegue(withIdentifier: "verTraspaso", sender: self)
        }
    }
}

class MyTapGesture: UITapGestureRecognizer {
    var id: Int64 = 0
    var traspaso: Any?
}
