//
//  AppDelegate.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/11/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import UIKit
import GoogleSignIn
import GoogleAPIClientForREST

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, GIDSignInDelegate {

    var window: UIWindow?
    var user:GIDGoogleUser? = nil

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
         _ = Database()
        
        let pageControl = UIPageControl.appearance()
        pageControl.currentPageIndicatorTintColor = .black
        pageControl.pageIndicatorTintColor = .lightGray
        
        GIDSignIn.sharedInstance().clientID = "852881704240-1eevk5oju0t4uhb28kajqitem1tjqibl.apps.googleusercontent.com"
        GIDSignIn.sharedInstance().delegate = self
        
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }

    @available(iOS 9.0, *)
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any]) -> Bool {
        return GIDSignIn.sharedInstance().handle(url)
    }
    
    func application(_ application: UIApplication,
                     open url: URL, sourceApplication: String?, annotation: Any) -> Bool {
        return GIDSignIn.sharedInstance().handle(url)
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
        self.user = user
        // Perform any operations on signed in user here.
        let fullName = user.profile.name
        print(fullName)
        let lastSync = getLastSync()
        let googleDriveService = GTLRDriveService()
        googleDriveService.authorizer = user!.authentication.fetcherAuthorizer()
        DispatchQueue.global(qos: .background).async {
            getFileByName(name: Config.Table, service: googleDriveService) { result in
                if let configId = result {
                    print("config result: ", configId)
                    if lastSync == nil {
                        print("Update Local with Drive 1")
                        DispatchQueue.global(qos: .background).async {
                            downloadFilesAndDeleteOlds(service: googleDriveService) { result in
                                print("Result: ", result)
                            }
                        }
                    } else {
                        getFileDataById(id: configId, service: googleDriveService) { result in
                            print("Last Sync: ", result![Config.LastSync-1])
                            let configLastSync = result![Config.LastSync-1][Config.ValueCode] as? String
                            let dateFormatter = DateFormatter()
                            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
                            dateFormatter.timeZone = TimeZone.current
                            dateFormatter.locale = Locale.current
                            let configLastSyncDate = dateFormatter.date(from: configLastSync!)
                            let lastSyncDate = dateFormatter.date(from: lastSync!)
                            if lastSyncDate == configLastSyncDate{
                                //No changes
                                print("No changes")
                            } else if  (lastSyncDate! < configLastSyncDate!) {
                                //Update Local with Drive
                                print("Update Local with Drive 2")
                                DispatchQueue.global(qos: .background).async {
                                    downloadFilesAndDeleteOlds(service: googleDriveService) { result in
                                        print("Result: ", result)
                                    }
                                }
                            } else if lastSyncDate! > configLastSyncDate! {
                                //Update drive with local
                                print("Update drive with local 1")
                                DispatchQueue.global(qos: .background).async {
                                    deletAll(service: googleDriveService) { res in
                                        uploadFiles(service: googleDriveService)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    DispatchQueue.global(qos: .background).async {
                        deletAll(service: googleDriveService) { res in
                            uploadFiles(service: googleDriveService)
                        }
                    }
                }
            }
            /*getFolderID(service: googleDriveService) { data in
                DispatchQueue.main.async {
                    print("doSomethingInTheUIWithValue(kidId)")
                }
            }*/
        }
        
    }
    
    func sign(_ signIn: GIDSignIn!, didDisconnectWith user: GIDGoogleUser!,
              withError error: Error!) {
        // Perform any operations when the user disconnects from app here.
        // ...
    }
}

