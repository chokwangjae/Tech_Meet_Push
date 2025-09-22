//
//
//import Foundation
//import Matrix_Mobile
//
//public final class FCMPlugin : MatrixMobilePlugin {
//    //MARK: -- Dependency
//    private var pushManager: FCMManagerType
//    
//    init(pushManager: FCMManager = FCMManager.shared) {
//        self.pushManager = pushManager
//        super.init()
//        setClosure()
//    }
//    
//    required dynamic init() {
//        self.pushManager = FCMManager.shared
//        super.init()
//        setClosure()
//    }
//    
//    
//    public override func pluginInitialize() {
//        print("FCMPlugin plugin init")
//        
//    }
//    
//    //MARK: -- Web호출
//    public func getToken(_ callBack: CallBackManager) {
//        let userPushKey = self.pushManager.userFCMPushKey
//        callBack.success(data: userPushKey)
//    }
//    
//    //MARK: -- APP -> Web Listener
//    public func onRefreshTokenReceived(_ token: String) {
//        let evalJS = "$h.fcm.onTokenRefreshReceived(\"\(token)\")"
//        self.webView?.evaluateJavaScript(evalJS)
//    }
//    
//    //MARK: -- Web호출
//    public func getPushInfo(_ callBack: CallBackManager) {
//        let currentSavedPush = self.pushManager.getCurrentSavedPush()
//        if let currentSavedPush {
//            callBack.success(data: currentSavedPush)
//        } else {
//            let emptyData: [String: Any] = [:]
//            callBack.success(data: emptyData)
//        }
//    }
//    
//    //MARK: -- App -> Web Listner
//    public func onPushEventReceived(_ pushInfo: [String: Any]?) {
//        guard let pushInfo,
//              let pushInfoData = self.dicToData(dic: pushInfo),
//              let JSONString = String(data: pushInfoData, encoding: .utf8) else {
//            let pushInfo = "Empty"
//            let evalJS = "$h.fcm.onPushEventReceived(\(pushInfo))"
//            self.webView?.evaluateJavaScript(evalJS)
//            return
//        }
//        print("pushData --- \(JSONString)")
//        let evalJS = "$h.fcm.onPushEventReceived(\(JSONString))"
//        self.webView?.evaluateJavaScript(evalJS)
//    }
//}
//
//extension FCMPlugin {
//    private func setClosure() {
//        self.pushManager.fcmPushKeyChange = { [weak self] token in
//            self?.onRefreshTokenReceived(token)
//        }
//        self.pushManager.pushReceiveOnActive = { [weak self] in
//            let push = self?.pushManager.getCurrentSavedPush()
//            self?.onPushEventReceived(push)
//        }
//    }
//    
//    private func dicToData(dic: [AnyHashable: Any]) -> Data? {
//        do {
//            // 딕셔너리를 Data 타입으로 변환
//            let jsonData = try JSONSerialization.data(withJSONObject: dic, options: [])
//            return jsonData
//        } catch {
//            print("🔴 JSON 변환 중 오류 발생: \(error.localizedDescription)")
//        }
//        return nil
//    }
//}
