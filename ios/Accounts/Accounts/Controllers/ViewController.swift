//
//  ViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
import GoogleSignIn
import GoogleAPIClientForREST
import GTMSessionFetcher

class ViewController: UIViewController {

    @IBAction func btnClick(_ sender: UIButton) {
        print("Sign In")
        
        GIDSignIn.sharedInstance()?.signIn()
    }
    @IBOutlet weak var contentView: UIView!
    
    let delegate = UIApplication.shared.delegate as! AppDelegate
    var dataSource = ["Accounts:", "Moves:"]
    let currentViewControllerIndex = 0
    var dataArrayTotales:[[String:Any?]]?
    var dataArrayMovimientos:[[String:Any?]]?
    let googleDriveService = GTLRDriveService()
    var googleUser: GIDGoogleUser?
    var currentIndex = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NSLog("Hola0")
        // Do any additional setup after loading the view.
        dataSource[0] = NSLocalizedString("Accounts: ", comment: "")
        dataSource[1] = NSLocalizedString("Moves: ", comment: "")
        
        _ = Database()
        dataArrayTotales = getTotales(inactivos: false)
        dataArrayMovimientos = getMovimientos()
        
        GIDSignIn.sharedInstance()?.presentingViewController = self
        //GIDSignIn.sharedInstance()?.scopes = [kGTLRAuthScopeDrive]
        GIDSignIn.sharedInstance()?.scopes = [kGTLRAuthScopeDriveAppdata]
        // Automatically sign in the user.
        GIDSignIn.sharedInstance()?.restorePreviousSignIn()
        
        dataArrayTotales = getTotales(inactivos: false)
        
        configurePageViewController()
    }

    func configurePageViewController(){
        NSLog("Hola01")
        guard let pageViewController = storyboard?.instantiateViewController(withIdentifier: "PageControllerOne")
            as? UIPageViewController else {
                NSLog("Hola1")
                return
        }
        pageViewController.delegate = self
        pageViewController.dataSource = self
        
        addChild(pageViewController)
        pageViewController.didMove(toParent: self)
        
        pageViewController.view.translatesAutoresizingMaskIntoConstraints = false
        
        contentView.addSubview(pageViewController.view)
        NSLog("Hola02")
        let views: [String: Any] = ["pageView": pageViewController.view as Any]
        
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "H:|-0-[pageView]-0-|",
                                                                 options: NSLayoutConstraint.FormatOptions(rawValue: 0),
                                                                 metrics: nil,
                                                                 views: views))
        contentView.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:|-0-[pageView]-0-|",
                                                                 options: NSLayoutConstraint.FormatOptions(rawValue: 0),
                                                                 metrics: nil,
                                                                 views: views))
        
        guard let startingViewController = detailViewControllerAt(index: currentViewControllerIndex) else {
            NSLog("Hola2")
            return
        }
        NSLog("Hola03")
        pageViewController.setViewControllers([startingViewController], direction: .forward, animated: true)
    }

    func detailViewControllerAt(index: Int) -> DataViewController? {
        if index >= dataSource.count || dataSource.count == 0 {
            return nil
        }
        
        guard let dataViewController = storyboard?.instantiateViewController(withIdentifier: String(describing: DataViewController.self))
            as? DataViewController else {
            return nil
        }
        dataViewController.index = index
        dataViewController.displayString = dataSource[index]
        
        if(index == 0) {
            dataViewController.totalesVisible = false
            dataViewController.movimientosVisible = true
        } else {
            dataViewController.totalesVisible = true
            dataViewController.movimientosVisible = false
        }
        //dataViewController.tag = 1
        return dataViewController
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if "createCuentasSegue" == segue.identifier {
            // Nothing really to do here, since it won't be fired unless
            // shouldPerformSegueWithIdentifier() says it's ok. In a real app,
            // this is where you'd pass data to the success view controller.
            let destination = segue.destination as! CreateCuentasViewController
            destination.test = "Adios"
        } else if "createMovimientoSegue" == segue.identifier {
            let destination = segue.destination as! CreteMovimientoViewController
            destination.test = "Adios"
        }
    }
    
    @IBAction func addBtn(_ sender: UIButton) {
        if(self.currentIndex == 0){
            self.performSegue(withIdentifier: "createCuentasSegue", sender: nil)
        } else if(self.currentIndex == 1){
            self.performSegue(withIdentifier: "createMovimientoSegue", sender: nil)
        }
    }
    
}

extension ViewController: UIPageViewControllerDelegate, UIPageViewControllerDataSource{
    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return currentViewControllerIndex
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return dataSource.count
    }
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        let dataViewController = viewController as? DataViewController
        
        guard var currentIndex = dataViewController?.index else {
            return nil
        }
        if currentIndex == 0{
            return nil
        }
        
        currentIndex -= 1
        return detailViewControllerAt(index: currentIndex)
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        let dataViewController = viewController as? DataViewController
        
        guard var currentIndex = dataViewController?.index else {
            return nil
        }
        if currentIndex == dataSource.count{
            return nil
        }
        
        currentIndex += 1
        return detailViewControllerAt(index: currentIndex)
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed {
            if let currentViewController = pageViewController.viewControllers![0] as? DataViewController {
                let index = currentViewController.index
                self.currentIndex = index!
            }
        }
    }
    
    func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!,
    withError error: Error!) {
        if let error = error {
        if (error as NSError).code == GIDSignInErrorCode.hasNoAuthInKeychain.rawValue {
        print("The user has not signed in before or they have since signed out.")
        } else {
        print("\(error.localizedDescription)")
        }
        return
        }
    }
    
}

