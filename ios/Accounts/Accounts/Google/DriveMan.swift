//
//  DriveMan.swift
//  Accounts
//
//  Created by Juan Ochoa on 9/17/19.
//  Copyright © 2019 Juan Ochoa. All rights reserved.
//

import Foundation
import GoogleSignIn
import GoogleAPIClientForREST

func getFileByName(name:String, service: GTLRService, completion: @escaping (String?) -> Void){
    let query = GTLRDriveQuery_FilesList.query()
    
    query.spaces = "drive"
    query.corpora = "user"
    query.spaces = "appDataFolder"
    query.q = "name='\(name)'"
    
    service.executeQuery(query) { (_, result, error) in
        guard error == nil else {
            completion(nil)
            return
        }
        
        let fileList = result as? GTLRDrive_FileList
        if fileList?.files?.count == 0{
            completion(nil)
        } else {
            for f in fileList!.files! {
                print("name: ", f.name)
            }
            completion(fileList?.files![0].identifier)
        }
    }
}

func getFileDataById(id: String, service: GTLRService, completion: @escaping ([Dictionary<String,Any>]?) -> Void){
    let query = GTLRDriveQuery_FilesGet.queryForMedia(withFileId: id)
    service.executeQuery(query) { (_, result, error)  in
        guard error == nil else {
            print("getFileDataById error", error ?? "default value")
            completion(nil)
            return
        }
        let file = result as! GTLRDataObject
        let dataa = file.data
        let s = String(bytes: dataa, encoding: .ascii)
        let data = s!.data(using: .utf8)!
        var jsonArray: [Dictionary<String, Any>]?
        do {
            jsonArray = try JSONSerialization.jsonObject(with: data, options : .allowFragments) as? [Dictionary<String,Any>]
            completion(jsonArray)
        } catch let error as NSError {
            print("getFileDataById error", error)
            completion(nil)
            return
        }
    }
}

func downloadFilesAndDeleteOlds(
    service: GTLRDriveService,
    completion: @escaping (String?) -> Void){
    
    let query = GTLRDriveQuery_FilesList.query()
    
    // Comma-separated list of areas the search applies to. E.g., appDataFolder, photos, drive.
    query.spaces = "drive"
    
    // Comma-separated list of access levels to search in. Some possible values are "user,allTeamDrives" or "user"
    query.corpora = "user"
    
    query.spaces = "appDataFolder"
    //query.q = "\(withName) and \(foldersOnly) and \(ownedByUser)"
    let group = DispatchGroup()
    var fileList: GTLRDrive_FileList? = nil
    group.enter()
    service.executeQuery(query) { (_, result, error) in
        guard error == nil else {
            fatalError(error!.localizedDescription)
        }
        fileList = result as? GTLRDrive_FileList
        // For brevity, assumes only one folder is returned.
        //completion(folderList.files?.first?.identifier)
        group.leave()
        print("Completion")
    }
    group.wait()
    //group.notify(queue: , execute: {
    let group2 = DispatchGroup()
    if(fileList == nil){
        completion("fill")
    } else {
        for file in fileList!.files! {
            group2.enter()
            print("File Name: ", file.name)
            downloadDataByIdAndDelete(service: service, id: file.identifier!, name: file.name!) { res in
               //print("res", res)
                
                group2.leave()
            }
            group2.wait()
            sleep(1)
        }
    }
    print("All Done");
    completion("fill")
    //})
}

func downloadDataByIdAndDelete(service: GTLRDriveService, id: String, name: String, completion: @escaping (Any?) -> Void) {
    let query = GTLRDriveQuery_FilesGet.queryForMedia(withFileId: id)//(withFileId: id)
    let group = DispatchGroup()
    print("query: ", id)
    group.enter()
    var res: Any?
    service.executeQuery(query) { (_, result, error)  in
        guard error == nil else {
            print("error", error ?? "<#default value#>")
            res = nil
            group.leave()
            fatalError(error!.localizedDescription)
        }
        res = result
        group.leave()
        
    }
    group.wait()
    let file = res as! GTLRDataObject
    let dataa = file.data
    let s = String(bytes: dataa, encoding: .ascii)
    let data = s!.data(using: .utf8)!
    var jsonArray: [Dictionary<String, Any>]?
    do {
        jsonArray = try JSONSerialization.jsonObject(with: data, options : .allowFragments) as? [Dictionary<String,Any>]
    } catch let error as NSError {
        print(error)
    }
    Database.deleteTable(table: name)
    for data in jsonArray!{
        print(data)
        var keys: Array<String> = Array()
        var values: Array<String?> = Array()
        var x = 0
        for d in data{
            keys.append(d.key)
            if let n = d.value as? Int {
                values.append(String(n))
            } else if let d = d.value as? Double{
                values.append(String(d))
            } else if let s = d.value as? String {
                values.append(s)
            } else {
                values.append(nil)
            }
            x += 1;
        }
        Database.insertIntoTable(table: name, columns: keys, values: values)
    }
    //print("res", s)
    completion(jsonArray)
}

func listFiles(service: GTLRDriveService,  completion: @escaping (Any?) -> Void) {
    let query = GTLRDriveQuery_FilesList.query()
    
    query.spaces = "drive"
    query.corpora = "user"
    query.spaces = "appDataFolder"
    
    service.executeQuery(query) { (_, result, error) in
        guard error == nil else {
            completion(nil)
            return
        }
        
        let fileList = result as? GTLRDrive_FileList
        if fileList?.files?.count == 0{
            completion(nil)
        } else {
            completion(fileList)
        }
    }
}

func deletAll(service: GTLRDriveService, completion: @escaping (Any?) -> Void) {
    
    //DispatchQueue.global(qos: .background).async {
        let group = DispatchGroup()
        listFiles(service: service) { res in
            let fileList = res as? GTLRDrive_FileList
            if fileList == nil {
                completion(true)
                return
            }
            for file in fileList!.files! {
                let query = GTLRDriveQuery_FilesDelete.query(withFileId: file.identifier!)//(withFileId: id)//(withFileId: id)
                
                print("query: ", file.name!)
                print("query: ", file.identifier!)
                group.enter()
                service.executeQuery(query) { (_, result, error)  in
                    print("ressas, ", result)
                    print("ressas err, ", error)
                    group.leave()
                }
                group.wait()
            }
            completion(true)
        }
    //}
}

func uploadFiles(service: GTLRDriveService){
        
    let tables = getAllTables()
    
    let group2 = DispatchGroup()
    for table in tables {
        group2.enter()
        print("File Name: ", table)
        //DispatchQueue.global(qos: .background).async {
            uploadDataByIdAnd(service: service, name: table) { res in
                print("complete res: ", res)
                group2.leave()
            }
        //}
        //group2.wait()
        sleep(1)
    }
    
    print("All Done");
}


func uploadDataByIdAnd(service: GTLRDriveService, name: String, completion: @escaping (Any?) -> Void) {
    
    let sData = getTablesJSONasString(name: name)
    
    let file = GTLRDrive_File()
    file.name = name
    file.parents = ["appDataFolder"]
    
    //var url = URL(fileURLWithPath: NSTemporaryDirectory(), isDirectory: true)
    

    do {
        //let fileManager = FileManager.default
        //let documentDirectory = try fileManager.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        
        //let url = documentDirectory.appendingPathComponent(name)
        //try sData.write(to: url, atomically: false, encoding: .utf8)
        let uploadParameters = GTLRUploadParameters(data: sData.data(using: .utf8)!, mimeType: "application/json")
        let q = GTLRDriveQuery_FilesCreate.query(withObject: file, uploadParameters: uploadParameters)
        
        service.executeQuery(q) { (_, result, error)  in
            guard error == nil else {
                print("error", error ?? "<#default value#>")
                completion(nil)
                return
            }
            print("Holaaa res: ", result)
            //do {
            //    try fileManager.removeItem(at: url)
            //} catch {
            //
            //}
            completion("Success")
        }
    } catch {
        completion(nil)
        return
    }

    //print("res", s)
    //completion(jsonArray)
}
