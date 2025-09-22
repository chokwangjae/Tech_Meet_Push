
//
//  MessageInfoSection.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI
import MatrixPush

/// 메시지 정보 섹션
/// 메시지의 상세 정보들을 표시하는 섹션
struct MessageInfoSection: View {
    let message: PushMessage
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("메시지 정보")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.black)
            
            VStack(spacing: 8) {
                DetailInfoRow(label: "제목", value: message.title ?? "정보없음")
                DetailInfoRow(label: "메시지 ID", value: message.pushDispatchId)
                DetailInfoRow(label: "메시지 타입", value: message.messageType)
                DetailInfoRow(label: "상태", value: message.status)
                DetailInfoRow(label: "수신 시간", value: formatDetailTimestamp(message.timestamp))
                
                if let campaignId = message.campaignId {
                    DetailInfoRow(label: "캠페인 ID", value: campaignId)
                }
                
                if let imageUrl = message.imageUrl {
                    DetailInfoRow(label: "이미지 URL", value: imageUrl)
                }
                
                if let payload = message.payload {
                    DetailInfoRow(label: "페이로드", value: payload)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color.gray.opacity(0.05))
        .cornerRadius(8)
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
