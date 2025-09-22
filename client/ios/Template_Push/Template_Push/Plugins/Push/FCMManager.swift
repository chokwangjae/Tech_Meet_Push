//
//  PushManager.swift
//
//  Created by HyeongyuIM on 3/29/24.
//

import Foundation
import UIKit

import FirebaseMessaging

protocol FCMManagerType {
    var userFCMPushKey: String { get }
    var preNotification: [String: Any]? { get }
    var fcmPushKeyChange: ((String) -> Void)? { get set }
    var pushReceiveOnActive: (() -> Void)? { get set }
    
    func setup()
    func getCurrentSavedPush() -> [String: Any]?
}

///FCMManager
///
///PushNotification Delegate를 컨트롤 하기위한 클래스 입니다
public final class FCMManager: NSObject, FCMManagerType {
    public static let shared: FCMManager = .init()
    
    private override init() {}
    
    //MARK: Dependency
    private lazy var fcmObject = Messaging.messaging()
        
    //MARK: -- Public
    ///앱이 시작될때 FCM에서 나오는 토큰을 저장합니다.
    private(set) var userFCMPushKey: String = ""
    
    ///아직 웹뷰로 전달되지않은 페이로드가 존재할때 저장. 전달되면 nil을 할당합니다
    private(set) var preNotification: [String: Any]?
    
    var fcmPushKeyChange: ((String) -> Void)?
    var pushReceiveOnActive: (() -> Void)?
    
    ///Delegate, 기본적인 푸시알림 권한요청
    public func setup() {
        let center = UNUserNotificationCenter.current()
        center.delegate = self
        center.requestAuthorization(options: [.sound, .alert, .badge]) { (granted, error) in
            if granted && error == nil {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
        fcmObject.delegate = self
    }
    
    //푸시를 가져가면 저장된 푸시 Nil
    func getCurrentSavedPush() -> [String: Any]? {
        guard let preNotification = preNotification else {
            print("🔴 저장된 푸시가 없습니다")
            return nil
        }
        defer { self.preNotification = nil }
        return preNotification
    }
}

//MARK: -- UNUserNotificationCenterDelegate
extension FCMManager: UNUserNotificationCenterDelegate {
    /// 유저가 푸시를 클릭했을때 호출되는 메서드
    /// 백그라운드 상태에 있을때 호출되는 노티피케이션 메서드, 앱이 종료됫을때도 유저가 탭하면 마찬가지로 호출
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let appState = UIApplication.shared.applicationState
        let payload = response.notification.request.content.userInfo
        print("appState \(appState) payload \(payload)")
        guard let apsData = payload["aps"] as? [String: Any],
        let alertData = apsData["alert"] as? [String: Any] else { return }
        self.preNotification = alertData
        //앱이 켜져있거나 백그라운드에서 액티브 생태로 바뀔때 푸시 전송 (앱이 켜질때는 background 라고 찍힘)
        if appState == .active || appState == .inactive {
            self.pushReceiveOnActive?()
        }
        
    }
    
    /// 알림을 받게되면 호출
    public func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let payload = notification.request.content.userInfo
        print("payload \(payload)")

        if #available(iOS 14.0, *) {
            completionHandler([.banner, .list, .sound])
        } else {
            completionHandler([.alert, .sound])
        }
    }
}

//MARK: -- fcmToken 확인
extension FCMManager: MessagingDelegate {
    ///여기서 토큰이 새로할당될경우 호출
    ///
    ///앱 첫 시작시 알림 허용시 토큰이 새로 할당
    ///
    ///최초 거절 -> 설정에서 허용: 재시작시 토큰 새로 할당
    ///
    ///동의 -> 거절 -> 동의: 최초 새로 할당 이후에 권한 변경으로는 새로 할당되지 않음
    public func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("Firebase registration token: \(fcmToken!)")
        setFCMPushKey(key: fcmToken ?? "ERROR")
    }
}

extension FCMManager {
    private func setFCMPushKey(key: String) {
        //푸시키가 비어있지 않고, 현재 푸시키와 들어온 새 키가 다를때 신호 발생
        if self.userFCMPushKey.isEmpty == false && (self.userFCMPushKey != key) {
            self.fcmPushKeyChange?(key)
        }
        self.userFCMPushKey = key
    }
}
