//
//  MatrixViewModel.swift
//  SwiftUISampleProject
//
//  Created by 김창옥 on 2023/02/17.
//

import Foundation
import Combine
import UIKit
import SwiftUI
import OSLog

import Matrix_Mobile
import MatrixPush
import MUtil


final class MatrixViewModel: ObservableObject {
    
    @Published var model:MatrixModel = MatrixModel()
    @Published var webViews:[MatrixMobileWebView] = [MatrixMobileWebView]()
    
    var matrixMobile: MatrixMobile?
    
    var length: Int = 0
    var temp:[MatrixMobileWebView] = [MatrixMobileWebView]()
    
    var useServerSelectScreen: Bool {
        matrixMobile?.targetInfo.useSeverSelect ?? false
    }
    
    //MARK: -- WMatrix DelegateSubject
    private let matrixCreatedRelay = PassthroughRelay<String>()
    private let matrixStartedRelay = PassthroughRelay<String>()
    private let matrixWebViewCreatedRelay = PassthroughRelay<(tag: String, matrixWebView: MatrixMobileWebView?)>()
    private let matrixGroupSelectRelay = PassthroughRelay<ServerGroup>()
    private let dismissLoadingViewRelay = PassthroughRelay<Void>()
    private let onEventListenerRelay = PassthroughRelay<String>()
    private let matrixErrorRelay = PassthroughRelay<(tag: String, error: MatrixError)>()
    
    init() {
        self.matrixMobile = MatrixMobile(delegate: self)
        self.showServerSelect()
    }
    
    func showServerSelect() {
        if useServerSelectScreen {
            self.matrixMobile?.showServerSelect()
        } else {
            // 서버선택화면 미사용시
            // WMatrixConfig.plist에 target이름과 일치하는 Dictionary에 startServerGroup과 일치하는 serverGroup으로 시작한다.
            if let serverGroup = self.matrixMobile?.getStartServerGroupFromConfig() {
                MLog.debug(msg: "groupName:\(serverGroup.groupName), serverList:\(serverGroup.serverList)")
                self.createWMatrix(serverGroup:serverGroup)
            } else {
                MLog.debug(msg: "WMatrixConfig.plist에 startServerGroup이름을 확인하세요.")
            }
        }
    }
    
    func createWMatrix(serverGroup: ServerGroup) {
        self.length += 1
        serverGroup.serverList.forEach { serverData in
            let options = WebViewOptions()
            self.matrixMobile?.create(tag: serverData.name, serverData: serverData, webViewOptions: options)
        }
    }
    
    func pushNotificationAgreement(bool: Bool) {
        UtilLog.d("\(bool)")
        Task {
//            await bool == true ? EdgePushFunctions.enablePush() : EdgePushFunctions.disablePush()
        }
    }
}

extension MatrixViewModel: MatrixMobileProtocol {
    func onMatrixCreated(tag: String) {
        DispatchQueue.main.async {
            self.model.create = true
            self.matrixMobile?.start(tag: tag)
        }
        
    }
    
    func onMatrixStarted(tag: String) {
        DispatchQueue.main.async {
            self.model.start = true
            self.matrixMobile?.makeWebView(tag: tag)
        }
    }
    
    func onMatrixWebViewCreated(tag: String, matrixWebView: Matrix_Mobile.MatrixMobileWebView?) {
        DispatchQueue.main.async {
            if let webView = matrixWebView {
                /// web inspector on
#if DEBUG
                if #available(iOS 16.4, *) {
                    webView.isInspectable = true
                }
#endif
                self.temp.append(webView)
                if self.length == self.temp.count {
                    for v in self.temp {
                        v.loadStartPage()
                    }
                    self.model.isWebView = true
                    self.webViews.append(contentsOf: self.temp)
                }
            }
            self.model.showPushPopup = true
        }
        
        //TODO: DELETE
//        print("\(#function) CoreData Models: EdgePushFunctions.getReceivedMessages")
//        print("\(#function) CoreData Select: \(String(describing: self.edgeAgent.getReceivedMessageByUniqueSeq(uniqueSeq: "0D76609C-9320-4D0E-ACBC-13CC68C7E6CC")))")
    }
    
    func onMatrixGroupSelect(group: Matrix_Mobile.ServerGroup) {
        self.createWMatrix(serverGroup: group)
    }
    
    func onDismissLoadingView() {
        withAnimation(.easeIn(duration: 0.5)) {
            self.model.showLoading.toggle()
        }
    }
    
    func onMatrixError(tag: String, error: Matrix_Mobile.MatrixError) {
        DispatchQueue.main.async {
            self.model.error = true
            self.model.errorCode = error.errorCode
            self.model.errorMessage = error.errorMessage
        }
    }
    
    @objc private func handleAppWillTerminate() {
        
    }
}
