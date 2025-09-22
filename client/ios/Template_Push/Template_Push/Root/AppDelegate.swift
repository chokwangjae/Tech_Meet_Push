//
//  AppDelegate.swift
//  EdgeAgentTest
//
//  Created by jihoon jang on 2023/01/09.
//
// Copyright (C) Inswave Systems, Inc - All Rights Reserved
// Unauthorized copying of this file, via any medium is strictly prohibited
// Proprietary and confidential
// Written by Inswave Systems <webmaster@inswave.com>, 2023/01/09
//

import UIKit

import FirebaseCore
import FirebaseMessaging

import MatrixPush
import MCommon
import MUtil

class AppDelegate: UIResponder, UIApplicationDelegate {
    private(set) var client: MatrixPushClient?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        UtilLog.initLogging(true)
        
        FirebaseApp.configure()
        
//        client = MatrixPushClient.builder("http://192.168.152.21:9093")
        client = MatrixPushClient.builder("http://192.168.152.21:8081")
            .appGroupName("group.matrix.push.sample")
            .debugMode(true)
            .onError({ error in
                print(error.errorDescription)
            })
            .onInitialized({
                
                print("완료이벤트")
                
                try? await Task.sleep(nanoseconds: 3_000_000_000)
                await MatrixPushFunctions.login(userId: "ss2",userName: "dd2", email: "ff2")
            })
            .build()
        
        MatrixPushFunctions.setOnNewMessageListener { pushData in
            print("푸시왔다!")
        }
        
        MatrixPushFunctions.setOnSyncMessageCompleteListener { count in
            print("미수신 메세지 콜백 count : \(count)")
        }
        
        UtilLog.e("!@# app start @@@@@")
        
        // 앱 실행 시 사용자에게 알림 허용 권한을 받음
        UNUserNotificationCenter.current().delegate = self
        
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound] // 필요한 알림 권한을 설정
        UNUserNotificationCenter.current().requestAuthorization(
            options: authOptions,
            completionHandler: { _, _ in }
        )

        // UNUserNotificationCenterDelegate를 구현한 메서드를 실행시킴
        application.registerForRemoteNotifications()

        // 파이어베이스 Meesaging 설정
        Messaging.messaging().delegate = self
        
        
        UtilLog.d("AllMessage --- \(MatrixPushFunctions.getAllMessages())")
        return true
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    
    func application(_ application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        UtilLog.d("APNS token: \(deviceToken)")
        Messaging.messaging().apnsToken = deviceToken
        
//        Messaging.messaging().token { fcmToken, error in
//            if let error = error {
//                print("❌ Error fetching FCM registration token: \(error)")
//            } else if let fcmToken = fcmToken {
//                print("✅ FCM registration token: \(fcmToken)")
//                print("🎉 FCM Token is ready to use!")
//            } else {
//                print("⚠️ FCM token is nil")
//            }
//        }
    }
    
    //백그라운드 또는 포그라운드에서 푸시를 탭하면 이벤트가 들어오게 됨.
    public func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse,
                                       withCompletionHandler completionHandler: @escaping () -> Void) {
        let appState = UIApplication.shared.applicationState
        let payload = response.notification.request.content.userInfo
        UtilLog.d("111111111 appState \(appState) payload 1 ---  \(payload)")
        
        // 푸시 알림 네비게이션 처리
        PushNavigationManager.shared.handlePushNotification(userInfo: payload)
        
        Task {
            await MatrixPushFunctions.markMessageAsConfirmed(receiveData: payload)
        }
        
        completionHandler()
    }
    
    //포그라운드 상태에서 푸시를 수신할경우 들어오게 됨
    public func userNotificationCenter(_ center: UNUserNotificationCenter,
                                       willPresent notification: UNNotification,
                                       withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
//        let appState = UIApplication.shared.applicationState
        let payload = notification.request.content.userInfo
        UtilLog.d("2222222222222 payload --- \(payload)")
        UtilLog.d("\(Date())")
        
        Task {
            // 사용 x
//            await client?.receivePublicPush(pushData: payload)
        }
        if #available(iOS 14.0, *) {
            completionHandler([.banner, .list, .sound])
        } else {
            completionHandler([.alert, .sound])
        }
    }
    
//    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
//                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
//        UtilLog.e("!@# silent push @@@@@")
//        UtilLog.d("=== 사일런트 푸시 수신 ===")
//        UtilLog.e("!@# notiextension silent push : \(userInfo["pushDispatchId"])")
//        
////        Task {
////            await MatrixPushFunctions.handlePushMessage(receiveData: userInfo)
////        }
//        
//        
//        completionHandler(.newData)
//    }
}

extension AppDelegate: MessagingDelegate {
    
    // 파이어베이스 MessagingDelegate 설정
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
      print("Firebase registration token: \(String(describing: fcmToken ?? ""))")
        UtilLog.e("!@# token arrive : \(fcmToken ?? "Empty") @@@@@")
        Task {
            if let fcmToken = fcmToken {
                await MatrixPushFunctions.start(pushToken: fcmToken)
            }
        }
    }
}
