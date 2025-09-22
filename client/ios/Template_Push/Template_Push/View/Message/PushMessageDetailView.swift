//
//  PushMessageDetailView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI
import MatrixPush

/// 푸시 메시지 상세 뷰
/// 선택된 푸시 메시지의 상세 정보를 표시
struct PushMessageDetailView: View {
    let message: PushMessage
    @Binding var isPresented: Bool
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // 상단 헤더
                MessageDetailHeader(message: message)
                
                // 메시지 내용
                ScrollView {
                    VStack(spacing: 20) {
                        // 메시지 본문
                        MessageContentSection(message: message)
                        
                        // 메시지 정보
                        MessageInfoSection(message: message)
                        
                        Spacer(minLength: 20)
                    }
                    .padding(20)
                }
                .background(Color.white)
            }
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("닫기") {
                        isPresented = false
                    }
                    .foregroundColor(.white)
                }
            }
        }
    }
}



struct PushMessageDetailView_Previews: PreviewProvider {
    static var previews: some View {
        // 미리보기용 더미 메시지 생성
//        let dummyMessage = PushMessage(
//            pushDispatchId: "preview_123",
//            messageType: "NOTIFICATION",
//            title: "미리보기 제목",
//            body: "이것은 미리보기용 메시지 내용입니다.",
//            payload: nil,
//            imageUrl: nil,
//            timestamp: "2024-12-19T10:30:00.000Z",
//            status: "READ",
//            campaignId: "campaign_001"
//        )
//        
//        PushMessageDetailView(
//            message: dummyMessage,
//            isPresented: .constant(true)
//        )
    }
}
