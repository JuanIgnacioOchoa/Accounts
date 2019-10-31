//
//  MainGraphsViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 30/10/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
//PageControllerGraphs
class MainGraphsViewController: UIViewController {


    @IBOutlet weak var page1: UILabel!
    @IBOutlet weak var page2: UILabel!
    @IBOutlet weak var page3: UILabel!
    @IBOutlet weak var page4: UILabel!
    @IBOutlet weak var contentView: UIView!
    /*
    var dataArrayTotales:[[String:Any?]]?
    */
    lazy var viewControllers:[UIViewController] = {
        let balance = storyboard?.instantiateViewController(withIdentifier: String(describing: BalanceGraphViewController.self))
        balance?.view.tag = 0
        let gasto = storyboard?.instantiateViewController(withIdentifier: String(describing: GastoGraphViewController.self))
        gasto?.view.tag = 1
        let ingreso = storyboard?.instantiateViewController(withIdentifier: String(describing: IngresoGraphViewController.self))
        ingreso?.view.tag = 2
        let history = storyboard?.instantiateViewController(withIdentifier: String(describing: HistoryGraphViewController.self))
        history?.view.tag = 3
        return [balance!, gasto!, ingreso!, history!]
    }()
    
    var currentIndex:Int = 0
    var nextIndex:Int = 0
    var pageView:UIPageViewController? = nil
    
    override func viewDidLoad() {
        page1.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
        page4.textColor = UIColor.black
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction1))
        page1.addGestureRecognizer(tap)
        page1.isUserInteractionEnabled = true
        let tap2 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction2))
        page2.addGestureRecognizer(tap2)
        page2.isUserInteractionEnabled = true
        let tap3 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction3))
        page3.addGestureRecognizer(tap3)
        page3.isUserInteractionEnabled = true
        let tap4 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction4))
        page4.addGestureRecognizer(tap4)
        page4.isUserInteractionEnabled = true
        
        //dataArrayTotales = getTotales(inactivos: false)
        
        configurePageViewController()
    }
    @objc
    func tapFunction1(sender:UITapGestureRecognizer) {
        let localIndex = 0
        while currentIndex > localIndex {
            currentIndex = currentIndex - 1
            goToPreviousPage()
        }
        page1.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
        page4.textColor = UIColor.black
    }
    @objc
    func tapFunction2(sender:UITapGestureRecognizer) {
        let localIndex = 1
        if currentIndex < localIndex {
            while currentIndex < localIndex {
                currentIndex = currentIndex + 1
                goToNextPage()
            }
        } else {
            while currentIndex > localIndex {
                currentIndex = currentIndex - 1
                goToPreviousPage()
            }
        }
        page2.textColor = UIColor.blue
        page1.textColor = UIColor.black
        page3.textColor = UIColor.black
        page4.textColor = UIColor.black
    }
    @objc
    func tapFunction3(sender:UITapGestureRecognizer) {
        let localIndex = 2
        if currentIndex < localIndex {
            while currentIndex < localIndex {
                currentIndex = currentIndex + 1
                goToNextPage()
            }
        } else {
            while currentIndex > localIndex {
                currentIndex = currentIndex - 1
                goToPreviousPage()
            }
        }
        page3.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page1.textColor = UIColor.black
        page4.textColor = UIColor.black
    }
    @objc
    func tapFunction4(sender:UITapGestureRecognizer) {
        let localIndex = 3
        while currentIndex < localIndex {
            currentIndex = currentIndex + 1
            goToNextPage()
        }
        page4.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
        page1.textColor = UIColor.black
    }
    
    func configurePageViewController(){
        guard let pageViewController = storyboard?.instantiateViewController(withIdentifier: "PageControllerGraphs")
            as? UIPageViewController else {
                return
        }
        pageViewController.delegate = self
        pageViewController.dataSource = self
        
        addChild(pageViewController)
        pageViewController.didMove(toParent: self)
        
        pageViewController.view.translatesAutoresizingMaskIntoConstraints = false
        self.pageView = pageViewController
        contentView.addSubview(pageViewController.view)
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
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
    }
    
    func goToNextPage(){
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[self.currentIndex]], direction: .forward, animated: true, completion: nil)
    }


    func goToPreviousPage(){
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[self.currentIndex]], direction: .reverse, animated: true, completion: nil)
    }
}

extension MainGraphsViewController: UIPageViewControllerDelegate, UIPageViewControllerDataSource{

    
    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return currentIndex
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return 4
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
    func pageViewController(_ pageViewController: UIPageViewController, willTransitionTo pendingViewControllers: [UIViewController]) {
        let itemController = pendingViewControllers[0]
        nextIndex = itemController.view.tag
    }
    func pageViewController(pageViewController: UIPageViewController, willTransitionToViewControllers pendingViewControllers: [AnyObject]) {
        // the page view controller is about to transition to a new page, so take note
        // of the index of the page it will display.  (We can't update our currentIndex
        // yet, because the transition might not be completed - we will check in didFinishAnimating:)
        
    }
    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if completed{
            currentIndex = nextIndex
        }
    }
}
