
//
//  MessageListView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI
import MatrixPush

/// 메시지 목록 뷰
/// 푸시 메시지들을 스크롤 가능한 목록으로 표시
struct MessageListView: View {
    let messages: [PushMessage]
    let onMessageTap: (PushMessage) -> Void
    
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(Array(messages.enumerated()), id: \.element.pushDispatchId) { index, message in
                    Button(action: {
                        onMessageTap(message)
                    }) {
                        MessageRowView(
                            title: message.title ?? "제목 없음",
                            content: message.body ?? "내용 없음",
                            timestamp: formatTimestamp(message.timestamp)
                        )
                    }
                    .buttonStyle(PlainButtonStyle())
                    
                    // 구분선
                    if index < messages.count - 1 {
                        Divider()
                            .background(Color.gray.opacity(0.3))
                    }
                }
            }
        }
        .background(Color.white)
    }
    
    /// 타임스탬프 포맷팅
    /// - Parameter timestamp: ISO 8601 형식의 타임스탬프 문자열
    /// - Returns: 포맷된 날짜 문자열
    private func formatTimestamp(_ timestamp: String?) -> String {
        guard let timestamp = timestamp else {
            return "날짜 정보 없음"
        }
        
        // ISO 8601 형식의 문자열을 Date로 변환
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        if let date = formatter.date(from: timestamp) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
            displayFormatter.timeZone = TimeZone.current
            return displayFormatter.string(from: date)
        } else {
            // 변환에 실패하면 원본 문자열 반환
            return timestamp
        }
    }
}
