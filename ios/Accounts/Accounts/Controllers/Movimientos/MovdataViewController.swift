//
//  MovdataViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 10/23/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MovdataViewController: UIViewController, UITableViewDelegate, UITableViewDataSource{

    @IBOutlet weak var tableView: UITableView!
    
    @IBAction func addBtn(_ sender: UIButton) {
        self.performSegue(withIdentifier: "crearMovimiento", sender: nil)
    }
    //var index: Int?
    var displayString: String?
    var fechaDataSource: [[String:Any?]] = []
    var month:String? = nil
    var year:String? = nil
    var moneda:Int64 = 0
    var selectedId: Int64 = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.delegate = self
        tableView.dataSource = self
        fechaDataSource = getMovimientosFecha()
        tableView.allowsSelection = false
    }

    func updateArrays(){
        if(year == nil){
            fechaDataSource = getMovimientosFecha()
        }
        else if month == nil {
            fechaDataSource = getMovimientosFecha(year: year!)
            //dataArrayMotives = getSumByMotivesYearly(idMoneda: moneda, year: year)
            //dataArrayTotales = getTotalesCuentasByYear(year: year)
        } else {
            fechaDataSource = getMovimientosFecha(year: year!, month: month!)
            //dataArrayMotives = getSumByMotivesMonthly(idMoneda: moneda, month: month!, year: year)
            //dataArrayTotales = getTotalesCuentasByMonth(year: year, month: month!)
        }
        tableView.reloadData()
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        //if index == 0 {
            return fechaDataSource.count
        //} else {
        //    return 1
        //}
        //return 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        //let cell: UITableViewCell
        let cell = tableView.dequeueReusableCell(withIdentifier: "FechaTableCell") as! FechaTableViewCell
        //if(index == 0){
            let fecha = fechaDataSource[row][Movimiento.Fecha] as! String
            let data = gteMovimientosByDate(date: fecha)
            tableView.rowHeight = CGFloat(40 + (25 * data.count))
            cell.setCell(fecha: fecha, data:data, parent: self)
        //} else {
            //cell.setCell(fecha: "ABCD", parent: tableView)
        //}
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //verMovimiento
        //self.performSegue(withIdentifier: "verMovimiento", sender: nil)
    }
    
       override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
           
           if "crearMovimiento" == segue.identifier {
                let viewController = segue.destination as! SeeMovimientoViewController
                viewController.title = "Crear Movimiento"
                viewController._id = 0
    /*
                   if let indexPath = tableView.indexPathForSelectedRow{
                       let viewController = segue.destination as! SeeCuentaViewController
                       let cuenta = dataArrayTotales[indexPath.row][Totales.Cuenta] as! String
                       let moneda = dataArrayTotales[indexPath.row][Moneda.Moneda] as! String
                       viewController.title = "\(cuenta) \(moneda)"
                       viewController._id = dataArrayTotales[indexPath.row]["_id"] as! Int64
                   }
     */
           } else if "verMovimiento" == segue.identifier {
                let viewController = segue.destination as! SeeMovimientoViewController
                viewController.title = "Ver Movimiento"
                viewController._id = selectedId
           } else if "verTraspaso" == segue.identifier {
                let viewController = segue.destination as! TraspasoViewController
                viewController.title = "Ver Traspaso"
                viewController._id = selectedId
           }
       }
}
