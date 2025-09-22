
//
//  MessageRowView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 메시지 행 뷰
/// 개별 메시지를 표시하는 행 뷰
struct MessageRowView: View {
    let title: String
    let content: String
    let timestamp: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // 제목
            Text(title)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.black)
            
            // 내용
            Text(content)
                .font(.system(size: 14))
                .foregroundColor(.gray)
            
            // 타임스탬프
            Text(timestamp)
                .font(.system(size: 12))
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 16)
        .padding(.vertical, 16)
        .background(Color.white)
    }
}
