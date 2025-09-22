
//
//  CampaignSettingsSection.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 캠페인 설정 섹션
/// 개별 캠페인별 알림 설정을 관리하는 섹션
struct CampaignSettingsSection: View {
    @Binding var campaigns: [Campaign]
    let onCampaignToggle: (Int, Bool) -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            // 섹션 제목
            HStack {
                Text("캠페인별 설정")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.black)
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 15)
            .background(Color.white)
            
            // 동적 캠페인 목록
            ForEach(Array(campaigns.enumerated()), id: \.element.id) { index, campaign in
                SettingRowView(
                    title: campaign.name,
                    isOn: Binding(
                        get: { campaigns[index].isEnabled },
                        set: { newValue in
                            campaigns[index].isEnabled = newValue
                            onCampaignToggle(campaign.id, newValue)
                        }
                    )
                )
            }
        }
    }
}
