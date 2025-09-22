
//
//  MessageDetailHeader.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI
import MatrixPush

/// 메시지 상세 헤더
/// 메시지 제목과 타임스탬프를 표시하는 상단 헤더
struct MessageDetailHeader: View {
    let message: PushMessage
    
    var body: some View {
        VStack(spacing: 16) {
            // 제목
            Text(message.title ?? "제목 없음")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 20)
            
            // 타임스탬프
            Text(formatDetailTimestamp(message.timestamp))
                .font(.system(size: 14))
                .foregroundColor(.white.opacity(0.8))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .background(Color(red: 0.153, green: 0.224, blue: 0.404))
    }
    
    /// 상세 타임스탬프 포맷팅
    /// - Parameter timestamp: ISO 8601 형식의 타임스탬프 문자열
    /// - Returns: 상세 포맷된 날짜 문자열
    private func formatDetailTimestamp(_ timestamp: String) -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        if let date = formatter.date(from: timestamp) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateFormat = "yyyy년 MM월 dd일 HH:mm:ss"
            displayFormatter.timeZone = TimeZone.current
            return displayFormatter.string(from: date)
        } else {
            return timestamp
        }
    }
}
