//
//  HomeViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 28/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class HomeViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    var dataTotales:[[String:Any?]] = []
    var selectedCuenta:Int = 0
    var btns:[UIButton] = []
    let buttonSize:CGFloat = 100
    let cellId = "cellId"
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        let gradient: CAGradientLayer = CAGradientLayer()

        gradient.colors = Utils.gradientColors
        gradient.locations = [0.0 , 1.0]
        gradient.startPoint = CGPoint(x: 1.0, y: 1.0)
        gradient.endPoint = CGPoint(x: 1.0, y: 0.0)
        gradient.frame = CGRect(x: 0.0, y: 0.0, width: self.view.frame.size.width, height: self.view.frame.size.height)

        self.view.layer.insertSublayer(gradient, at: 0)

        

  
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        dataTotales = getTotales(inactivos: false)
        let count = dataTotales.count
        
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width
        let screenHeigth = screenSize.height - (navigationController?.navigationBar.frame.height)! - UIApplication.shared.statusBarFrame.height
        var btnRows:Int = Int((screenHeigth - (35 + 35 )) / 140) // 45 = top and bottom margin
        //let buttonSize:CGFloat = 100
        //let x1:CGFloat = 0 + (((screenWidth/2) - buttonSize)/1.5)
        //let x2:CGFloat = screenWidth/2 + (((screenWidth/2) - buttonSize)/3)
        let x3:CGFloat = screenWidth/2 - (buttonSize/2)
        
        if count == 0 {
            let button = UIButton(type: .custom)
            button.frame = CGRect(x: x3, y: (screenHeigth/2) - 100, width: buttonSize, height: buttonSize)
            button.layer.cornerRadius = 0.5 * button.bounds.size.width
            button.clipsToBounds = true
            button.setTitle("Agregar Cuenta", for: .normal)
            button.backgroundColor = UIColor.red
            //button.setImage(UIImage(named:"thumbsUp.png"), for: .normal)
            button.addTarget(self, action: #selector(createAccount), for: .touchUpInside)
            view.addSubview(button)
        }
        else if count > ((btnRows * 2)){
            btnRows -= 1
            let y = addButtons(cantidad: btnRows * 2)
            let button = UIButton(type: .custom)
            button.frame = CGRect(x: x3, y: y, width: buttonSize, height: buttonSize)
            button.layer.cornerRadius = 0.5 * button.bounds.size.width
            button.clipsToBounds = true
            button.setTitle("MORE", for: .normal)
            button.backgroundColor = UIColor.red
            //button.setImage(UIImage(named:"thumbsUp.png"), for: .normal)
            button.addTarget(self, action: #selector(tableViewShow), for: .touchUpInside)
            view.addSubview(button)
        } else {
            let _ = addButtons(cantidad: count)
        }
    }
    
    func addButtons(cantidad:Int) -> CGFloat{
        
        
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width
        //let screenHeigth = screenSize.height
        let x1:CGFloat = 0 + (((screenWidth/2) - buttonSize)/1.5)
        let x2:CGFloat = screenWidth/2 + (((screenWidth/2) - buttonSize)/3)
        let x3:CGFloat = screenWidth/2 - (buttonSize/2)
        var y:CGFloat = (navigationController?.navigationBar.frame.height)! + UIApplication.shared.statusBarFrame.height + 45
        print(y)
        var c = 0
        var x:CGFloat
        while c < cantidad {
            if c + 1 == cantidad && c % 2 == 0 {
                x = x3
            }else if c % 2 == 0{
                x = x1
            } else {
                x = x2
            }
            let button = UIButton(type: .custom)
            button.frame = CGRect(x: x, y: y, width: buttonSize, height: buttonSize)
            button.layer.cornerRadius = 0.5 * button.bounds.size.width
            button.clipsToBounds = true
            button.setTitle(dataTotales[c][Totales.Cuenta] as? String, for: .normal)
            button.backgroundColor = UIColor.red
            //button.setImage(UIImage(named:"thumbsUp.png"), for: .normal)
            button.tag = c
            button.addTarget(self, action: #selector(btnPressed), for: .touchUpInside)
            btns.append(button)
            view.addSubview(button)
            c = c + 1
            if c % 2 == 0{
                y = y + 140
            }
        }
        return y
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if dataTotales.count % 2 == 0 {
            return dataTotales.count / 2
        } else {
            return (dataTotales.count / 2) + 1
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        //MoreTableViewCell
        let row = indexPath.row
        let cell = HomeTableViewCell(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: 140))
        //let cell = tableView.dequeueReusableCell(withIdentifier: cellId, for: indexPath) as! HomeTableViewCell
        cell.backgroundColor = UIColor.init(white: 1.0, alpha: 0.0)
        let data1 = dataTotales[row * 2]
        var data2:[String:Any?]? = nil
        if ((row * 2) + 1) < dataTotales.count {
            data2 = dataTotales[(row * 2) + 1]
        }
        cell.setCell(data1: data1, data2: data2, row: row, parent: self)
        return cell
    }
    
    @objc func tableViewShow(sender: UIButton){
        for b in btns {
            //b.removeFromSuperview()
        }
        sender.removeFromSuperview()
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width
        let screenHeigth = screenSize.height - (navigationController?.navigationBar.frame.height)! - UIApplication.shared.statusBarFrame.height
        let heigth = screenHeigth// - ((75 * 2) * 2) + 45
        //let x:CGFloat = 0 + (((screenWidth/2) - buttonSize)/1.5)
        //let y:CGFloat = (navigationController?.navigationBar.frame.height)! + UIApplication.shared.statusBarFrame.height + 75
        let tableView = UITableView(frame: CGRect(x: 0, y: 0, width: screenWidth, height: heigth))
        tableView.rowHeight = 140
        tableView.backgroundColor = UIColor(white: 1, alpha: 0.0)
        tableView.allowsSelection = false
        tableView.separatorStyle = .none
        tableView.dataSource = self
        tableView.delegate = self
        view.addSubview(tableView)
        
    }
    @objc func createAccount(sender: UIButton){
        performSegue(withIdentifier: "crearCuenta", sender: self)
        
    }
    
    @objc func btnPressed(sender:UIButton){
        selectedCuenta = (sender.tag)
        performSegue(withIdentifier: "crearMovimiento", sender: self)
    }
   override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
       
       if "crearMovimiento" == segue.identifier {
            let viewController = segue.destination as! SeeMovimientoViewController
            viewController.title = "Crear Movimiento"
            viewController._id = 0
            viewController.idTotales = dataTotales[selectedCuenta]["_id"] as! Int64
            viewController.idMoneda = dataTotales[selectedCuenta][Totales.IdMoneda] as! Int64
       }
       if "crearCuenta" == segue.identifier {
            let viewController = segue.destination as! CreateCuentasViewController
            viewController.title = "Crear Cuenta"
       }
   }
    
}
