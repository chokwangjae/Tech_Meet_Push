//
//  WMatrixWebView.swift.swift
//  TemplateSwiftUI
//
//  Created by UAPMobile Team on 2021/12/30.
//

import Foundation
import SwiftUI
import WebKit
import Matrix_Mobile

struct MMWebView: UIViewRepresentable {
    
    var webView: MatrixMobileWebView
    
    func makeUIView(context: Context) -> MatrixMobileWebView {
        return self.webView
    }
    
    func updateUIView(_ webview: MatrixMobileWebView, context: UIViewRepresentableContext<MMWebView>) {
        
    }
}
