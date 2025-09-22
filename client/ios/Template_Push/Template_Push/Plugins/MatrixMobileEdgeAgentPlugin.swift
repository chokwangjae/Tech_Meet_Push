

import Foundation
import Combine
import WebKit

import Matrix_Mobile
import MCommon

final class MatrixMobileEdgeAgentPlugin : MatrixMobilePlugin {
    //MARK: -- Dependency
    @Injected private var notificationManager: MCommon.NotificationManagerType
    
    private var cancelBag: Set<AnyCancellable> = .init()
    
    private let edgeAgentSSEMessageListnerKey: String = "onEdgeManagerEvent"
    
    public override func pluginInitialize() {
        print("EdgeAgentPlugin init")
    }
//    
//    public func echo(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("echo ==== \(body)")
//        callBack.success(data: body)
//    }
//    
//    /// 웹에서 사용자 로그인시 해당 메서드를 통해 유저정보를 전송
//    /// 이 시점부터 크래시 리포트에 유저정보가 저장됨
//    public func userLogIn(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("userLogIn ==== \(body)")
//        Task {
//            let loginResponse = await EdgeAgentFunctions.infoUserLogIn(requestData: body)
//            
//            print(loginResponse)
//            
//            if loginResponse.0 == true {
//                callBack.success()
//                notificationManager.postUserLoginSuccess()
//            }
//            if loginResponse.0 == false,
//               let edgeError = loginResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func userLogOut(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("userLogOut ==== \(body)")
//        Task {
//            let loginResponse = await EdgeAgentFunctions.infoUserLogOut(requestData: body)
//            
//            if loginResponse.0 == true {
//                callBack.success()
//            }
//            if loginResponse.0 == false,
//               let edgeError = loginResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func getMyNewNotification(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("getMyNewNotification ==== \(body)")
//        Task {
//            let getNotificationResponse = await EdgeAgentFunctions.getNotification(body: body, type: .NEW)
//            
//            if getNotificationResponse.0 == true {
//                let responseData: [String: Any] = getNotificationResponse.1 ?? [:]
//                let jsonString = String.convertToJSONString(array: responseData)
//                callBack.success(data: jsonString)
//            }
//            if getNotificationResponse.0 == false,
//               let edgeError = getNotificationResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func getMyNotification(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("getAllNotification ==== \(body)")
//        Task {
//            let getAllNotificationResponse = await EdgeAgentFunctions.getNotification(body: body, type: .MY)
//            
//            if getAllNotificationResponse.0 == true {
//                let responseData: [String: Any] = getAllNotificationResponse.1 ?? [:]
//                let body = responseData["body"] as? [String: Any] ?? [:]
//                let resultData = body["resultData"] as? [[String: Any]] ?? []
//                callBack.success(data: String.convertToJSONString(array: resultData))
//            }
//            if getAllNotificationResponse.0 == false,
//               let edgeError = getAllNotificationResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func setMyNotificationRead(_ callBack: CallBackManager) {
//        guard let body = callBack.command.arguments?.first as? [[String: Any]] else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("setMyNotificationRead ==== \(body)")
//        Task {
//            let setNotificationResponse = await EdgeAgentFunctions.setNotificationRead(body: body)
//            
//            if setNotificationResponse.0 == true {
//                callBack.success()
//            }
//            if setNotificationResponse.0 == false,
//               let edgeError = setNotificationResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func getAllNotification(_ callBack: CallBackManager) {
//        print("getAllNotification")
//        Task {
//            let getAllNotificationResponse = await EdgeAgentFunctions.getNotification(body: [:], type: .ALL)
//            
//            if getAllNotificationResponse.0 == true {
//                let responseData: [String: Any] = getAllNotificationResponse.1 ?? [:]
//                let body = responseData["body"] as? [String: Any] ?? [:]
//                callBack.success(data: String.convertToJSONString(array: body))
//            }
//            if getAllNotificationResponse.0 == false,
//               let edgeError = getAllNotificationResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func getUserList(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        
//        print("getUserList ==== \(body)")
//        Task {
//            let infoUserListResponse = await EdgeAgentFunctions.infoUserList(parameterData: body)
//            
//            if infoUserListResponse.0 == true {
//                callBack.success(data: infoUserListResponse.1?.convertToJsonString)
//            }
//            if infoUserListResponse.0 == false,
//               let edgeError = infoUserListResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    // EdgeAgent애서 사용가능한 Device 목록 가져오는 함수.
//    public func getDeviceList(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic,
//              let jsonData = try? JSONSerialization.data(withJSONObject: body),
//              let deviceListModel = try? JSONDecoder().decode(GetDeviceListModel.self, from: jsonData) else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        
//        print("parsingModel ==== \(deviceListModel)")
//        Task {
//            let deptCode: String? = body["deptCode"] as? String
//            let deviceType: [String]? = body["deviceType"] as? [String]
//            let deviceListResponse = await EdgeAgentFunctions.getDeviceList(deptCode: deptCode, deviceType: deviceType)
//            
//            if deviceListResponse.0 == true {
//                callBack.success(data: deviceListResponse.1?.convertToJsonString)
//            }
//            if deviceListResponse.0 == false,
//               let edgeError = deviceListResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func sendMessage(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic,
//              let jsonData = try? JSONSerialization.data(withJSONObject: body),
//              let sendMessagemodel = try? JSONDecoder().decode(SendMessageModel.self,
//                                                               from: jsonData),
//              let receivers = sendMessagemodel.receivers else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("sendMessagemodel ==== \(sendMessagemodel)")
//        Task {
//            let getDeviceListResponse = await EdgeAgentFunctions.sendMessage(receivers: receivers,
//                                                                            data: sendMessagemodel.dataToStringAny)
//            
//            if getDeviceListResponse.0 == true {
//                let jsonArrayString = getDeviceListResponse.1?.convertToJsonString ?? ""
//                callBack.success(data: jsonArrayString)
//            }
//            if getDeviceListResponse.0 == false,
//               let edgeError = getDeviceListResponse.2 {
//                callBack.error(status: .EXCEPTION, error: edgeError)
//            }
//        }
//    }
//    
//    public func sendDeviceCommand(_ callBack: CallBackManager) {
//        guard let body = callBack.argumentDic,
//              let jsonData = try? JSONSerialization.data(withJSONObject: body),
//              let sendDeviceCommandModel = try? JSONDecoder().decode(SendDeviceCommandModel.self, from: jsonData),
//              let receiver = sendDeviceCommandModel.receivers,
//              let _ = sendDeviceCommandModel.data
//              //                  let commandDataToDataType = try? commandData.encodeToData(),
//        else {
//            callBack.error(status: .INVALID_PARAM)
//            return
//        }
//        print("sendDeviceCommandModel ==== \(sendDeviceCommandModel)")
//        
//        let deviceCommand = DeviceCommandDTO(receivers: receiver, data: sendDeviceCommandModel.dataAsData)
//        let getDeviceListRelay = EdgeAgentFunctions.sendDeviceCommandFlow(tag: nil, requestDataList: [deviceCommand])
//        
//        getDeviceListRelay
//            .sink(receiveValue: { (success, response, error) in
//                if success == true {
//                    callBack.success(data: response?.convertToJsonString ?? "", keepCallback: true)
//                }
//                if success == false,
//                   let edgeError = error {
//                    callBack.error(status: .EXCEPTION, error: edgeError)
//                }
//            })
//            .store(in: &self.cancelBag)
//    }
//    
//    public func deviceEventListener(_ callBack: CallBackManager) {
//        let deviceEventFlow = EdgeAgentFunctions.registerDeviceEventFlow()
//        
//        deviceEventFlow
//            .receive(on: DispatchQueue.main)
//            .sink(receiveValue: { (bool, commandList, error) in
//                let commandListToJsonString = commandList?.convertToJsonString ?? ""
//                let javaScriptString = "$h.edgeagent.onDeviceEventFire(\(commandListToJsonString));"
//                self.webView?.evaluateJavaScript(javaScriptString)
//            })
//            .store(in: &self.cancelBag)
//        callBack.success()
//    }
}
