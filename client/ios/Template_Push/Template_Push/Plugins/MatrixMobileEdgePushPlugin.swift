//
//  MatrixMobileEdgePushPlugin.swift
//  Template
//
//  Created by HyeongyuIM on 7/15/25.
//

import Foundation
import Combine
import WebKit

import Matrix_Mobile
import MatrixPush
import MCommon
import MUtil

final class MatrixMobileEdgePushPlugin : MatrixMobilePlugin {
    
    func register(_ callBack: CallBackManager) {
        guard let body = callBack.argumentDic,
              let pushToken = body["pushToken"] as? String else {
            callBack.error(status: .INVALID_PARAM)
            return
        }
        print("register ==== \(body)")
        Task {
            await MatrixPushFunctions.start(pushToken: pushToken)
            callBack.success()
        }
    }
    
    func enablePush(_ callBack: CallBackManager) {
        guard let body = callBack.argumentDic else {
            callBack.error(status: .INVALID_PARAM)
            return
        }
        print("enablePush ==== \(body)")
        Task {
            let response = await MatrixPushFunctions.updateUserConsent(consented: true)
            if let response = response {
                let dic: [String: Any] = (try? response.convertToDic()) ?? [:]
                callBack.success(data: dic)
            } else {
                callBack.error(status: .EXCEPTION)
            }
        }
    }
    
    func disablePush(_ callBack: CallBackManager) {
        guard let body = callBack.argumentDic else {
            callBack.error(status: .INVALID_PARAM)
            return
        }
        print("disablePush ==== \(body)")
        Task {
            let response = await MatrixPushFunctions.updateUserConsent(consented: false)
            if let response = response {
                let dic: [String: Any] = (try? response.convertToDic()) ?? [:]
                callBack.success(data: dic)
            } else {
                callBack.error(status: .EXCEPTION)
            }
        }
    }
    
    func getMessageById(_ callBack: CallBackManager) {
        guard let body = callBack.argumentDic,
              let pushDispatchId = body["pushDispatchId"] as? String else {
            callBack.error(status: .INVALID_PARAM)
            return
        }
        print("getMessageById ==== \(body)")
        if let message = MatrixPushFunctions.getMessage(by: pushDispatchId) {
            print("\(String(describing: message))")
            let dic = (try? message.convertToDic()) ?? [:]
            callBack.success(data: dic)
        } else {
            callBack.error(status: .FILE_NOT_FOUND)
            return
        }
    }
    
    func getReceivedMessages(_ callBack: CallBackManager) {
        guard let body = callBack.argumentDic else {
            callBack.error(status: .INVALID_PARAM)
            return
        }
        print("getReceivedMessages ==== \(body)")
        let messages = MatrixPushFunctions.getAllMessages()
        print("\(String(describing: messages))")
        let dicArray = messages.compactMap { try? $0.convertToDic() }
        callBack.success(data: dicArray)
    }
    
    func isInitialized(_ callBack: CallBackManager) {
        guard let body = callBack.argumentDic else {
            callBack.error(status: .INVALID_PARAM)
            return
        }
        print("isInitialized ==== \(body)")
        let isInitialized = MatrixPushFunctions.isInitialized
        print("\(String(describing: isInitialized))")
        callBack.success(data: isInitialized)
    }
    
//    func getSDKVersion(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("getSDKVersion ==== \(body)")
//        let sdkVersion = MatrixPushFunctions.getSDKVersion()
//        print("\(String(describing: sdkVersion))")
//        callBack.success(data: sdkVersion)
//    }
}
