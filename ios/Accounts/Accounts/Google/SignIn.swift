//
//  SignIn.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/12/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation
import GoogleSignIn

class SignIn{
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
        // Perform any operations on signed in user here.
        print("User:: ",user)
        
    }
    
}
