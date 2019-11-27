//
//  MotivesMainViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MotivesMainViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBAction func addMotive(_ sender: UIButton) {
        if newMotive.text == nil {
            return
        }
        let _ = guardarMotivo(motivo: newMotive.text!)
        newMotive.text = nil
        for c in edit{
            let a = dataMotivos[c.key][Motivo.Active] as! Int64
            var m = ""
            if c.value.motivo == nil {
                m = dataMotivos[c.key][Motivo.Motivo] as! String
            } else {
                m = c.value.motivo!.text!
            }
            let id = dataMotivos[c.key]["_id"] as! Int64
            print(dataMotivos[c.key])
            let _ = updateMotive(motivo: m, active: a, id: id)
        }
        
        dataMotivos = getMotives(active: false)
        edit = [:]
        tableView.reloadData()
    }
    @IBAction func GuardarBtn(_ sender: UIButton) {
        for c in edit{
            let a = dataMotivos[c.key][Motivo.Active] as! Int64
            var m = ""
            if c.value.motivo == nil {
                m = dataMotivos[c.key][Motivo.Motivo] as! String
            } else {
                m = c.value.motivo!.text!
            }
            let id = dataMotivos[c.key]["_id"] as! Int64
            print(dataMotivos[c.key])
            let _ = updateMotive(motivo: m, active: a, id: id)
        }
        edit = [:]
        dataMotivos = getMotives(active: false)
        tableView.reloadData()
    }
    
    
    @IBOutlet var tableView: UITableView!
    @IBOutlet var newMotive: UITextField!
    
    var dataMotivos:[[String:Any?]] = []
    var edit:[Int:MotiveData] = [:]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        dataMotivos = getMotives(active: false)
        tableView.delegate = self
        tableView.dataSource = self
    }
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataMotivos.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let row = indexPath.row
        //let cell: UITableViewCell
        let cell = tableView.dequeueReusableCell(withIdentifier: "MotivosCell") as! MotivosTableViewCell
        cell.setCell(data: dataMotivos[row], parent: self, row: row)
        return cell
    }
}
class MotiveData {
    var motivo: UITextField?
    var active: Bool
    init(motivo:UITextField?, active:Bool) {
        self.motivo = motivo
        self.active = active
    }
}
