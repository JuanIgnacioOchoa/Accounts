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
    var parent: UIViewController?
    var parentType: Int = 0
    
    func setCell(fecha: String, data:[[String:Any?]], parent: UIViewController, parentType: Int){
        self.parent = parent
        self.parentType = parentType
        //"EEEE, d MMMM, yyyy"
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        let date:Date = dateFormatter.date(from: fecha)!
        dateFormatter.dateFormat = "EEEE, d MMMM, yyyy"
        fechaLbl.text = dateFormatter.string(from: date)
        var y:CGFloat = 0
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width
        content.subviews.forEach { $0.removeFromSuperview() }
        for d in data {
            let numberFormatter = NumberFormatter()
            numberFormatter.numberStyle = .decimal
            numberFormatter.minimumFractionDigits = 2
            let cantidad = d[Movimiento.Cantidad] as! NSNumber
            let idTraspaso = d[Movimiento.Traspaso]
            let id = d["_id"] as! NSNumber
            let cuenta = d[Totales.Cuenta] as! String
            var moneda = d[Moneda.Moneda] as! String
            let motivo = d[Motivo.Motivo] as! String
            if idTraspaso != nil {
                moneda = ""
            }
            let new_view:UIView! = UIView(frame: CGRect(x: 5, y: y, width: screenWidth - 20, height: 20));
           
            var w = new_view.frame.width/3
            var cantLbl = UILabel()
            var motivoLbl = UILabel()
            var cuentaLbl = UILabel()
            if parentType == 1 || parentType == 3 || parentType == 4{
                w = new_view.frame.width/3
                cantLbl = UILabel(frame: CGRect(x:w*2, y: 0, width: w, height: 20))
                motivoLbl = UILabel(frame: CGRect(x:w, y: 0, width: w, height: 20))
                cuentaLbl = UILabel(frame: CGRect(x:0, y: 0, width: w, height: 20))
                cuentaLbl.text = cuenta
                new_view.addSubview(cuentaLbl)
            } else if parentType == 2 {
                w = new_view.frame.width/2
                cantLbl = UILabel(frame: CGRect(x:w, y: 0, width: w, height: 20))
                motivoLbl = UILabel(frame: CGRect(x:0, y: 0, width: w, height: 20))
                //cuentaLbl = UILabel(frame: CGRect(x:0, y: 0, width: w, height: 20))
            }

            
            cantLbl.text = "\(numberFormatter.string(from: cantidad)!) \(moneda)"
            
            motivoLbl.text = motivo
            cantLbl.textAlignment = .right
            
            new_view.addSubview(cantLbl)
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
        if parentType == 1 {
            let vc = parent as! MovdataViewController
            vc.selectedId = sender.id
        } else if parentType == 2 {
            let vc = parent as! SeeCuentaViewController//SeeCuentaViewController
            vc.selectedId = sender.id
        } else if parentType == 3 {
            let vc = parent as! SeeMotivoViewController
            vc.selectedId = sender.id
        } else if parentType == 4 {
            let vc = parent as! SeeTripsViewController
            vc.selectedId = sender.id
        }
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
