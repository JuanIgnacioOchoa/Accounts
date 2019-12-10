//
//  SeeCuentaViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/20/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class SeeCuentaViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBAction func editBtn(_ sender: UIButton) {
        showEditAlert()
    }
    @IBOutlet weak var tableView: UITableView!
    
    var fechaDataSource:[[String:Any?]] = []
    var month:String? = nil, year:String? = nil
    var _id:Int64 = 0
    var data:[String:Any?] = [:]
    var selectedId: Int64 = 0
    var difCant: NSNumber = 0.0
    
    override func viewDidLoad() {
        data = getTotal(id: _id)
        tableView.delegate = self
        tableView.dataSource = self
        fechaDataSource = getTotalesMovFecha(id: _id, year: year, month: month)
    }
    
    func showEditAlert(){
        let alert = TotalesAlertViewController(title: "Edit Account", message: "", preferredStyle: .alert)
        let oldActiva = self.data[Totales.Activa] as! Int64
        let oldCuenta = self.data[Totales.Cuenta] as! String
        let oldCantidad = self.data[Totales.CurrentCantidad] as! Double
        let oldTipo = self.data[Totales.Tipo] as! Int64
        alert.addAction(UIAlertAction(title: "Guardar", style: .default, handler: {(a: UIAlertAction!) in
            let activa = alert.checkbox.isChecked
            let cuenta = alert.cuentaTxt.text
            let idTipo = alert.idTipo
            
            if cuenta == nil || cuenta! == "" {
                alert.cuentaTxt.setError("Error", show: true)
                self.present(alert, animated: true, completion: nil)
                return
            }
            if alert.cantTextField.text == nil {
                alert.cantTextField.setError("Error", show: true)
                self.present(alert, animated: true, completion: nil)
                return
            }
            var dCantidad = Double(alert.cantTextField.text!.replacingOccurrences(of: ",", with: ""))
            if dCantidad == nil {
                alert.cantTextField.setError("Error", show: true)
                self.present(alert, animated: true, completion: nil)
                return
            }
            
            let dif = abs(dCantidad! - oldCantidad);
            if(dif < 0.01 || dif == 0){
                print("Accoun", "Dif < 0.01 || dif == 0");
                let _ = updateTotalesInfo(cant: dCantidad!, cuenta: cuenta!, id: self._id, activa: activa, idTipo: idTipo)
            } else {
                let alert2 = UIAlertController(title: "", message: "Diferencia en cantidades conciderable, agregar como movimiento", preferredStyle: .alert)
                alert2.addAction(UIAlertAction(title: "OK", style: .default, handler: { action in
                      if(oldCantidad > dCantidad){
                        self.difCant = NSNumber(value: dif * -1)
                      } else {
                        self.difCant = NSNumber(value: dif)
                      }
                      self.performSegue(withIdentifier: "crearMovimiento", sender: nil)
                }))
                alert2.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
                self.present(alert2, animated: true, completion: nil)
            }
        }))

        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.activa = oldActiva == 1
        alert.cuenta = oldCuenta
        alert.cantidad = oldCantidad
        alert.idTipo = oldTipo
        present(alert, animated: true, completion: nil)
        
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
       } else if "crearMovimiento" == segue.identifier {
            let viewController = segue.destination as! SeeMovimientoViewController
            viewController.title = "Crear Movimiento"
            viewController._id = 0
            viewController.cantidad = difCant
            viewController.idTotales = _id
            viewController.idMoneda = data[Totales.IdMoneda] as! Int64
       }
   }
}


/*

 */
