//
//  NotiViewModel.swift
//  Template
//
//  Created by HyeongyuIM on 5/26/25.
//


import Foundation
import Combine
import UIKit
import SwiftUI
import OSLog

import Matrix_Mobile
import MatrixPush

final class NotiViewModel: ObservableObject {
    private static var isFirstLaunch: Bool = true
    
//    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
//    private var edgeAgent: EdgeAgentProviderType? {
//        return appDelegate.edgeAgent
//    }
    
    @Published var showPushAgreementAlert: Bool = false
    @Published var eventModel: [PushItem] = []
    @Published var notiModel: [PushItem] = []
    
    init() {
        loadDBData()
    }
    
    func viewDidLoad() {
        if NotiViewModel.isFirstLaunch == true {
            showPushAgreementAlert = true
        }
        NotiViewModel.isFirstLaunch = false
    }
    
    func loadDBData() {
//        let datas = EdgePushFunctions.getReceivedMessages()
//        self.eventModel = datas.filter { $0.messageType == "EVENT" }.map { $0.toVO }
//        self.notiModel = datas.filter { $0.messageType == "NOTIFICATION" }.map { $0.toVO }
    }
    
    func pushNotificationAgreement(bool: Bool) {
        Task {
//            _ = await bool == true ? EdgePushFunctions.enablePush() : EdgePushFunctions.disablePush()
        }
    }
}
