
//
//  PushNotificationSection.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 푸시 알림 설정 섹션
/// 전체 푸시 수신 동의 설정을 관리하는 섹션
struct PushNotificationSection: View {
    @Binding var pushNotificationEnabled: Bool
    let onToggle: (Bool) -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            // 섹션 제목
            HStack {
                Text("푸시 수신 설정")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.black)
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 15)
            .background(Color.white)
            
            // 전체 푸시 수신 동의
            SettingRowView(
                title: "전체 푸시 수신 동의",
                isOn: $pushNotificationEnabled,
                onToggle: { isEnabled in
                    onToggle(isEnabled)
                }
            )
        }
    }
}
