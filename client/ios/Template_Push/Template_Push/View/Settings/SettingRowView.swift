
//
//  SettingRowView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 설정 행 뷰
/// 개별 설정 항목을 표시하는 행 뷰 (토글 스위치 포함)
struct SettingRowView: View {
    let title: String
    @Binding var isOn: Bool
    var onToggle: ((Bool) -> Void)?
    
    var body: some View {
        HStack {
            Text(title)
                .font(.system(size: 16))
                .foregroundColor(.black)
            
            Spacer()
            
            Toggle("", isOn: $isOn)
                .toggleStyle(SwitchToggleStyle(tint: Color(red: 0.153, green: 0.224, blue: 0.404)))
                .onChange(of: isOn) { newValue in
                    onToggle?(newValue)
                }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 15)
        .background(Color.white)
        .overlay(
            Rectangle()
                .frame(height: 1)
                .foregroundColor(.gray.opacity(0.2)),
            alignment: .bottom
        )
    }
}
