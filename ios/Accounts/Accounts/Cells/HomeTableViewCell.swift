//
//  HomeTableViewCell.swift
//  Accounts
//
//  Created by Juan Ochoa on 04/12/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class HomeTableViewCell: UITableViewCell {

    var cellButton1: UIButton!
    var cellButton2: UIButton!
    var parent: HomeViewController!
    
    init(frame: CGRect, title: String) {
        super.init(style: UITableViewCell.CellStyle.default, reuseIdentifier: "cellId")

        //self.backgroundColor = UIColor.red//UIColor(white: 1, alpha: 0.0)

        //addSubview(cellButton1)
        //addSubview(cellButton2)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    func setCell(data1: [String:Any?], data2:[String:Any?]?, row:Int, parent:HomeViewController) {
        self.parent = parent
        let screenSize = UIScreen.main.bounds
        let screenWidth = screenSize.width
        let buttonSize:CGFloat = 100.0
        let x1:CGFloat = 0 + (((screenWidth/2) - buttonSize)/1.5)
        let x2:CGFloat = screenWidth/2 + (((screenWidth/2) - buttonSize)/3)
        cellButton1 = UIButton(type: .custom)
        cellButton1.frame = CGRect(x: x1, y: 20, width: 100, height: 100)
        cellButton1.layer.cornerRadius = 0.5 * cellButton1.bounds.size.width
        cellButton1.clipsToBounds = true
        cellButton1.setTitle(data1[Totales.Cuenta] as? String, for: .normal)
        cellButton1.backgroundColor = UIColor.red
        //button.setImage(UIImage(named:"thumbsUp.png"), for: .normal)
        cellButton1.tag = row*2
        cellButton1.addTarget(self, action: #selector(btnPressed), for: .touchUpInside)
        //btns.append(button)
        addSubview(cellButton1)
        
        if data2 != nil {
            cellButton2 = UIButton(type: .custom)
            cellButton2.frame = CGRect(x: x2, y: 20, width: 100, height: 100)
            cellButton2.layer.cornerRadius = 0.5 * cellButton1.bounds.size.width
            cellButton2.clipsToBounds = true
            cellButton2.setTitle(data2![Totales.Cuenta] as? String, for: .normal)
            cellButton2.backgroundColor = UIColor.red
            //button.setImage(UIImage(named:"thumbsUp.png"), for: .normal)
            cellButton2.tag = (row*2) + 1
            cellButton2.addTarget(self, action: #selector(btnPressed), for: .touchUpInside)
            //btns.append(button)
            addSubview(cellButton2)
        }
        
    }
    
    @objc func btnPressed(sender:UIButton){
        parent.selectedCuenta = (sender.tag)
        parent.performSegue(withIdentifier: "crearMovimiento", sender: self)
    }

}
