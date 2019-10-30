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

    @IBOutlet weak var pageTitle1: UILabel!
    @IBOutlet weak var pageTitle2: UILabel!
    @IBAction func btnSignIn(_ sender: Any) {
        GIDSignIn.sharedInstance()?.signIn()
    }
    @IBOutlet weak var contentView: UIView!
    
    let delegate = UIApplication.shared.delegate as! AppDelegate
    var dataArrayTotales:[[String:Any?]]?
    let googleDriveService = GTLRDriveService()
    var googleUser: GIDGoogleUser?
    
    lazy var viewControllers:[UIViewController] = {
        let first = storyboard?.instantiateViewController(withIdentifier: String(describing: DataViewController.self))
        let second = storyboard?.instantiateViewController(withIdentifier: String(describing: AnalisisTotalesViewController.self))
        return [first!, second!]
    }()
    
    var currentIndex:Int = 0
    var pageView:UIPageViewController? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        pageTitle1.text = "Movimientos"
        pageTitle2.text = "Analisis"
        pageTitle1.textColor = UIColor.blue
        pageTitle2.textColor = UIColor.black
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction1))
        pageTitle1.addGestureRecognizer(tap)
        pageTitle1.isUserInteractionEnabled = true
        //selectorPage1.addGestureRecognizer(tap)
        //selectorPage1.isUserInteractionEnabled = true
        let tap2 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction2))
        pageTitle2.addGestureRecognizer(tap2)
        pageTitle2.isUserInteractionEnabled = true
        
        GIDSignIn.sharedInstance()?.presentingViewController = self
        //GIDSignIn.sharedInstance()?.scopes = [kGTLRAuthScopeDrive]
        GIDSignIn.sharedInstance()?.scopes = [kGTLRAuthScopeDriveAppdata]
        // Automatically sign in the user.
        GIDSignIn.sharedInstance()?.restorePreviousSignIn()
        
        //dataArrayTotales = getTotales(inactivos: false)
        
        configurePageViewController()
    }
    @objc
    func tapFunction1(sender:UITapGestureRecognizer) {
        goToPreviousPage()
    }
    @objc
    func tapFunction2(sender:UITapGestureRecognizer) {
        goToNextPage()
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
        self.pageView = pageViewController
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
        
        guard let startingViewController = viewControllers.first else {
            return
        }
        pageViewController.setViewControllers([startingViewController], direction: .forward, animated: true)
    }
/*
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
 */
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if "createCuentasSegue" == segue.identifier {
            // Nothing really to do here, since it won't be fired unless
            // shouldPerformSegueWithIdentifier() says it's ok. In a real app,
            // this is where you'd pass data to the success view controller.
            let destination = segue.destination as! CreateCuentasViewController
        } else if "createMoveMain" == segue.identifier {
            let destination = segue.destination as! SeeMovimientoViewController
            destination._id = nil
        }
    }
    /*
    @IBAction func addBtn(_ sender: UIButton) {
        if(self.currentIndex == 0){
            self.performSegue(withIdentifier: "createCuentasSegue", sender: nil)
        } 
    }
 */
    func goToNextPage(){
        if self.currentIndex == 1 { return }
        self.currentIndex = 1
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[self.currentIndex]], direction: .forward, animated: true, completion: nil)
        pageTitle2.textColor = UIColor.blue
        pageTitle1.textColor = UIColor.black
    }


    func goToPreviousPage(){
        if(self.currentIndex == 0) { return }
        self.currentIndex = 0
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[self.currentIndex]], direction: .reverse, animated: true, completion: nil)
        pageTitle1.textColor = UIColor.blue
        pageTitle2.textColor = UIColor.black
        currentIndex = 0
    }
}

extension ViewController: UIPageViewControllerDelegate, UIPageViewControllerDataSource{

    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return 0
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return 2
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        guard let vcIndex = viewControllers.firstIndex(of: viewController) else { return nil }
        
        let previousIndex = vcIndex - 1
        
        guard previousIndex >= 0 else { return nil }
        
        guard viewControllers.count > previousIndex else { return nil }
        
        return viewControllers[previousIndex]
    }
    
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        guard let vcIndex = viewControllers.firstIndex(of: viewController) else { return nil }
        
        let nextIndex = vcIndex + 1
        
        guard nextIndex < viewControllers.count else { return nil }
        
        return viewControllers[nextIndex]
    }
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed{
            if self.currentIndex == 0 {
                pageTitle2.textColor = UIColor.blue
                pageTitle1.textColor = UIColor.black
                currentIndex = 1
            } else if self.currentIndex == 1 {
                pageTitle1.textColor = UIColor.blue
                pageTitle2.textColor = UIColor.black
                currentIndex = 0
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

