//
//  MonedaMainViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
import SafariServices
import Photos

class MonedaMainViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet var tableView: UITableView!
    @IBAction func AgregarMonedaBtnActn(_ sender: UIButton) {
        let alert = UIAlertController(title: "Agregar Moneda", message: nil, preferredStyle: .actionSheet)
        //let alert = UIAlertController(style: .)
        
        alert.addLocalePicker(type: .currency, selection: { info in
            print("Guardar \(info?.currencyCode)!")
            let success = guardarMoneda(moneda: (info?.currencyCode)!)
            if success {
                self.dataMonedas = getMonedas()
                self.tableView.reloadData()
            }
        })
        alert.addAction(title: "Cancel", style: .cancel)
        alert.show()
    }
    var dataMonedas:[[String:Any?]] = []
    var monedaInfo:[String:LocaleInfo] = [:]
    //MonedaCell
    override func viewDidLoad() {
        super.viewDidLoad()
        dataMonedas = getMonedas()
        initLocaleInfo()
        tableView.delegate = self
        tableView.dataSource = self
        // Do any additional setup after loading the view.
    }

    func initLocaleInfo(){
        for d in dataMonedas {
            let c = d[Moneda.Moneda] as! String
            LocaleStore.getInfo(c: c, completionHandler: {info in
                self.monedaInfo[c] = info!
            })
        }
    }
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataMonedas.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        //let cell: UITableViewCell
        let cell = tableView.dequeueReusableCell(withIdentifier: "MonedaCell") as! MonedaTableViewCell
        let c = dataMonedas[row][Moneda.Moneda] as! String
        cell.setCell(data: dataMonedas[row], info: monedaInfo[c])
        return cell
    }
}
