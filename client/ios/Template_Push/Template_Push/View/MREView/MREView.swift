//
//  MREView.swift
//  Template
//
//  Created by HyeongyuIM on 5/26/25.
//

import SwiftUI
import Matrix_Mobile

struct MREView: View {
    @StateObject var matrixVM: MatrixViewModel = MatrixViewModel()
    @State private var navigateToNotificationView = false
    
    var body: some View {
        ZStack {
            if matrixVM.model.showLoading {
                LoadingView(image: Image("logo"))
                    .alert("", isPresented: $matrixVM.model.error) {
                        Button("확인") { exit(0) }
                    } message: {
                        Text("code:\(matrixVM.model.errorCode)\nmessage:\(matrixVM.model.errorMessage)" )
                    }
            } else {
                ForEach(matrixVM.webViews, id:\.self) { webView in
                    MMWebView(webView: webView)
                        .ignoresSafeArea(.keyboard)
//                        .alert(isPresented: $matrixVM.model.showPushPopup) {
//                            Alert(title: Text("푸시 수신에 동의 하시겠습니까?"),
//                                  primaryButton: .default(Text("확인"), action: {
//                                self.matrixVM.pushNotificationAgreement(bool: true)
//                            }),
//                                  secondaryButton: .cancel(Text("거부"), action: {
//                                self.matrixVM.pushNotificationAgreement(bool: false)
//                            }))
//                        }
                }
            }
        }
        .navigationTitle("MRE")
    }
}
