
//
//  MessageContentSection.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI
import MatrixPush

/// 메시지 내용 섹션
/// 메시지 본문을 표시하는 섹션
struct MessageContentSection: View {
    let message: PushMessage
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("메시지 내용")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.black)
            
            Text(message.body ?? "내용 없음")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .multilineTextAlignment(.leading)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color.gray.opacity(0.05))
        .cornerRadius(8)
    }
}
