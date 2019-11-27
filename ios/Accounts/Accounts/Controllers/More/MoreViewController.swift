//
//  MoreViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 04/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class MoreViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var tableView: UITableView!
    var dataArray:[String] = ["Viajes", "Deudas/Prestamos", "Motivos", "Moneda", "Configuracion"]
    var imageArray:[UIImage] = [UIImage(named: "Plane")! as UIImage,
     UIImage(named: "Deudas")! as UIImage,  UIImage(named: "Motivos")! as UIImage,
     UIImage(named: "Moneda")! as UIImage, UIImage(named: "Configuracion")! as UIImage]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.delegate = self
        tableView.dataSource = self
        // Do any additional setup after loading the view.
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        //MoreTableViewCell
        let r = indexPath.row
        let cell = tableView.dequeueReusableCell(withIdentifier: "MoreTableViewCell") as! MoreTableViewCell
        cell.setCell(image: imageArray[r], string: dataArray[r])
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        switch indexPath.row {
        case 0:
            self.performSegue(withIdentifier: "TripsMainSegue", sender: nil)
            break
        case 1:
            self.performSegue(withIdentifier: "PrestamosSegue", sender: nil)
            break
        case 2:
            self.performSegue(withIdentifier: "MotivosSegue", sender: nil)
            break
        case 3:
            self.performSegue(withIdentifier: "MonedaSegue", sender: nil)
            break
        case 4:
            self.performSegue(withIdentifier: "ConfigSegue", sender: nil)
            break
        default:
            break
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if "TripsMainSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let vc = segue.destination as! TripsMainViewController
                //vc._id = dataArray[indexPath.row]["_id"] as! Int64
            }
        } else  if "ConfigSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let vc = segue.destination as! ConfigViewController
                //vc._id = dataArray[indexPath.row]["_id"] as! Int64
            }
        } else  if "MonedaSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let vc = segue.destination as! MonedaMainViewController
                //vc._id = dataArray[indexPath.row]["_id"] as! Int64
            }
        } else  if "MotivosSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let vc = segue.destination as! MotivesMainViewController
                //vc._id = dataArray[indexPath.row]["_id"] as! Int64
            }
        } else  if "PrestamosSegue" == segue.identifier {
            if let indexPath = tableView.indexPathForSelectedRow{
                let vc = segue.destination as! PrestamosMainViewController
                //vc._id = dataArray[indexPath.row]["_id"] as! Int64
            }
        }
    }
}
