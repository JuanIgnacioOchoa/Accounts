//
//  PrestamosMainViewController.swift
//  Accounts
//
//  Created by Juan Ochoa on 05/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit

class PrestamosMainViewController: UIViewController {

    @IBAction func createBtn(_ sender: UIButton) {
        showGetUserName()
    }
    @IBOutlet weak var page1: UILabel!
    @IBOutlet weak var page2: UILabel!
    @IBOutlet weak var page3: UILabel!
    @IBOutlet weak var contentView: UIView!
    @IBAction func switchChange(_ sender: UISwitch) {
        changeZero()
    }
    @IBOutlet weak var switchLbl: UILabel!
    @IBOutlet weak var switchZero: UISwitch!
    
    lazy var viewControllers:[UIViewController] = {
        let totales = storyboard?.instantiateViewController(withIdentifier: String(describing: PrestamosPeopleViewController.self))
        totales?.view.tag = 0
        let deudores = storyboard?.instantiateViewController(withIdentifier: String(describing: PrestamosPlusViewController.self))
        deudores?.view.tag = 1
        let deudas = storyboard?.instantiateViewController(withIdentifier: String(describing: PrestamosMinusViewController.self))
        deudas?.view.tag = 2
        return [totales!, deudores!, deudas!]
    }()
    var currentIndex:Int = 0
    var nextIndex:Int = 0
    var pageView:UIPageViewController? = nil
    var zero = false, prestado = false
    var selected:Int64 = 0;
    override func viewDidLoad() {
        super.viewDidLoad()

        page1.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
        let tap = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction1))
        page1.addGestureRecognizer(tap)
        page1.isUserInteractionEnabled = true
        let tap2 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction2))
        page2.addGestureRecognizer(tap2)
        page2.isUserInteractionEnabled = true
        let tap3 = UITapGestureRecognizer(target: self, action: #selector(self.tapFunction3))
        page3.addGestureRecognizer(tap3)
        page3.isUserInteractionEnabled = true
        let tapSwitch = UITapGestureRecognizer(target: self, action: #selector(self.tapSwitch))
        switchLbl.addGestureRecognizer(tapSwitch)
        switchLbl.isUserInteractionEnabled = true
        configurePageViewController()
        
    }
    
    func changeZero(){
        zero = !zero
        
        let totals = viewControllers[0] as! PrestamosPeopleViewController
        let plus = viewControllers[1] as! PrestamosPlusViewController
        let minus = viewControllers[2] as! PrestamosMinusViewController
        totals.updateArray(zero: !zero)
        plus.updateArray(zero: !zero)
        minus.updateArray(zero: !zero)
    }
    @objc
    func tapSwitch(sender:UITapGestureRecognizer) {
        changeZero()
        switchZero.setOn(zero, animated: true)
    }

    @objc
    func tapFunction1(sender:UITapGestureRecognizer) {
        let localIndex = 0
        if localIndex == currentIndex { return }
        goToPreviousPage(index: localIndex)
        page1.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page3.textColor = UIColor.black
    }
    @objc
    func tapFunction2(sender:UITapGestureRecognizer) {
        let localIndex = 1
        if localIndex == currentIndex { return }
        if currentIndex < localIndex {
            goToNextPage(index: localIndex)
        } else {
            goToPreviousPage(index: localIndex)
        }
        page2.textColor = UIColor.blue
        page1.textColor = UIColor.black
        page3.textColor = UIColor.black
    }
    @objc
    func tapFunction3(sender:UITapGestureRecognizer) {
        let localIndex = 2
        if localIndex == currentIndex { return }
        goToNextPage(index:localIndex)
        page3.textColor = UIColor.blue
        page2.textColor = UIColor.black
        page1.textColor = UIColor.black
    }

    func configurePageViewController(){
        guard let pageViewController = storyboard?.instantiateViewController(withIdentifier: "PageControllerPrestamos")
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

    func goToNextPage(index:Int){
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[index]], direction: .forward, animated: true, completion: nil)
    }


    func goToPreviousPage(index:Int){
        pageView?.dataSource = nil;
        pageView?.dataSource = self;
        pageView?.setViewControllers([viewControllers[index]], direction: .reverse, animated: true, completion: nil)
    }
    
}

extension PrestamosMainViewController: UIPageViewControllerDelegate, UIPageViewControllerDataSource{

    
    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return currentIndex
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return 3
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
            page1.textColor = UIColor.black
            page2.textColor = UIColor.black
            page3.textColor = UIColor.black
            switch currentIndex {
            case 0:
                page1.textColor = UIColor.blue
                break;
            case 1:
                page2.textColor = UIColor.blue
                break;
            case 2:
                page3.textColor = UIColor.blue
                break;
            default:
                break;
            }
        }
    }
    func showGetUserName() {

        let alertController = UIAlertController(title: "Loans", message: "Choose an Option", preferredStyle: .alert)

        alertController.addAction(UIAlertAction(title: "Pedir Prestado", style: UIAlertAction.Style.default, handler: Prestado(alert:)))
        alertController.addAction(UIAlertAction(title: "Cancel", style: UIAlertAction.Style.cancel, handler: nil))
        alertController.addAction(UIAlertAction(title: "Prestar", style: UIAlertAction.Style.default, handler: Prestar(alert:)))


        self.present(alertController, animated: true, completion: nil)
    }
    
    func Prestado(alert: UIAlertAction!) {
        prestado = false
        selected = 0
        self.performSegue(withIdentifier: "verPrestamo", sender: nil)
    }
    
    func Prestar(alert: UIAlertAction!) {
        prestado = true
        selected = 0
        self.performSegue(withIdentifier: "verPrestamo", sender: nil)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if "verPrestamo" == segue.identifier {
            let vc = segue.destination as! PrestamosDetViewController
            vc._id = selected
            vc.prestado = prestado
        }
    }
}
