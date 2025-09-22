//
//  NotificationService.swift
//  Template_Noti
//
//  Created by HyeongyuIM on 5/15/25.
//

import UserNotifications

import FirebaseMessaging

import MatrixPush

class NotificationService: UNNotificationServiceExtension {

    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?
    
    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        
        let userInfo = request.content.userInfo
        
        Task {
            await MatrixPushExternalFunctions.receivePublicPush(pushData: userInfo, appGroupName: "group.matrix.push.sample")
        }
        
        self.contentHandler = contentHandler
        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)
        self.bestAttemptContent?.title = "\(bestAttemptContent?.title ?? "")"
        
        guard let bestAttemptContent = bestAttemptContent else { return }
        FIRMessagingExtensionHelper().populateNotificationContent(bestAttemptContent, withContentHandler: contentHandler)
    }
    
    override func serviceExtensionTimeWillExpire() {
        // Called just before the extension will be terminated by the system.
        // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
        if let contentHandler = contentHandler, let bestAttemptContent =  bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }
}
