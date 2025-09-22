//
//  MessageTabView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI
import MatrixPush

/// 메시지 탭 화면
/// 푸시 메시지 목록을 표시하고 메시지 상세 보기를 제공
struct MessageTabView: View {
    @State private var messages: [PushMessage] = []
    @State private var isLoading = true
    @State private var selectedMessage: PushMessage? = nil
    @State private var showingMessageDetail = false
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            
            // 상단 메시지 동기화 버튼
            MessageSyncButton {
                loadMessages()
            }
            
            // 메시지 목록 또는 로딩/빈 상태
            if isLoading {
                MessageLoadingView()
            } else if messages.isEmpty {
                EmptyMessageView()
            } else {
                MessageListView(
                    messages: messages,
                    onMessageTap: { message in
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                            selectedMessage = message
                        }
                        showingMessageDetail = true
                    }
                )
            }
            
            Spacer()
        }
        .sheet(isPresented: $showingMessageDetail) {
            if let message = selectedMessage {
                PushMessageDetailView(
                    message: message,
                    isPresented: $showingMessageDetail
                )
                .onDisappear {
                    self.selectedMessage = nil
                }
            }
        }
        .onAppear {
            loadMessages()
        }
    }
    
    /// 메시지 목록 로드
    /// MatrixPushFunctions를 통해 모든 메시지를 가져옴
    private func loadMessages() {
        isLoading = true
        
        // MatrixPushFunctions.getAllMessages() 호출
        let allMessages = MatrixPushFunctions.getAllMessages()
        
        DispatchQueue.main.async {
            self.messages = allMessages
            self.isLoading = false
        }
    }
}



struct MessageTabView_Previews: PreviewProvider {
    static var previews: some View {
        MessageTabView()
    }
}
