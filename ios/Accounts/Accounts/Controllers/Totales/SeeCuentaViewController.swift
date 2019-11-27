//
//  SeeCuentaViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/20/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class SeeCuentaViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    
    var fechaDataSource:[[String:Any?]] = []
    var month:String? = nil, year:String? = nil
    var _id:Int64 = 0
    var selectedId: Int64 = 0
    
    override func viewDidLoad() {
        tableView.delegate = self
        tableView.dataSource = self
        fechaDataSource = getTotalesMovFecha(id: _id, year: year, month: month)
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
        let data = getTotalesMovimientosByDate(id: _id, date: fecha)
        tableView.rowHeight = CGFloat(40 + (25 * data.count))
        cell.setCell(fecha: fecha, data:data, parent: self, parentType: 2)
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //verMovimiento
        //self.performSegue(withIdentifier: "verMovimiento", sender: nil)
    }
   override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
       
       if "verMovimiento" == segue.identifier {
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