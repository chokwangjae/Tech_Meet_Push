
//
//  MessageSyncButton.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 메시지 동기화 버튼
/// 미수신 메시지를 동기화하는 버튼
struct MessageSyncButton: View {
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Spacer()
                Text("미수신 메시지 동기화")
                    .foregroundColor(.white)
                    .font(.system(size: 16, weight: .medium))
                Spacer()
            }
            .frame(height: 50)
            .background(Color(red: 0.153, green: 0.224, blue: 0.404))
            .cornerRadius(30)
            .padding(10)
        }
    }
}
