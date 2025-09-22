//
//  SettingsTabView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI
import MatrixPush

/// 캠페인 데이터 모델
/// 개별 캠페인의 정보와 동의 상태를 관리
struct Campaign: Identifiable {
    let id: Int
    let name: String
    var isEnabled: Bool
}

/// 설정 탭 화면
/// 푸시 알림 설정 및 캠페인별 설정을 관리
struct SettingsTabView: View {
    @State private var pushNotificationEnabled = true
    @State private var campaigns: [Campaign] = []
    @State private var isLoading = true
    @Binding var selectedTab: Int
    
    var body: some View {
        VStack(spacing: 0) {
            // 설정 목록
            VStack(spacing: 0) {
                // 푸시 수신 설정 섹션
                PushNotificationSection(
                    pushNotificationEnabled: $pushNotificationEnabled,
                    onToggle: onPushNotificationToggle
                )
                
                // 캠페인별 설정 섹션
                if isLoading {
                    CampaignLoadingView()
                } else if !campaigns.isEmpty {
                    CampaignSettingsSection(
                        campaigns: $campaigns,
                        onCampaignToggle: onCampaignNotificationToggle
                    )
                }
                
                Spacer()
            }
            .background(.white)
        }
        .background(.white)
        .onAppear {
            loadCampaigns()
        }
    }
    
    // MARK: - Functions
    
    /// 캠페인 목록 로드
    /// MatrixPushFunctions를 통해 캠페인 목록을 가져와서 UI에 반영
    private func loadCampaigns() {
        isLoading = true
        
        Task {
            do {
                let campaignList = await MatrixPushFunctions.getCampaignList()
                
                let dummyCampaigns = campaignList.map { campaign in
                    Campaign(id: campaign.campaignId, name: campaign.campaignName, isEnabled: campaign.consented)
                }
                await MainActor.run {
                    self.campaigns = dummyCampaigns
                    self.isLoading = false
                }
            }
        }
    }
    
    /// 전체 푸시 알림 토글 처리
    /// - Parameter isEnabled: 푸시 알림 활성화 여부
    func onPushNotificationToggle(_ isEnabled: Bool) {
        print("푸시 알림 설정 변경: \(isEnabled)")
        
        Task {
            let response = await MatrixPushFunctions.updateUserConsent(consented: isEnabled)
            if let response = response {
                print("전체 푸시 수신 설정 성공: \(response)")
            } else {
                print("전체 푸시 수신 설정 실패")
            }
        }
    }
    
    /// 캠페인별 알림 토글 처리
    /// - Parameters:
    ///   - campaignId: 캠페인 ID
    ///   - isEnabled: 알림 활성화 여부
    func onCampaignNotificationToggle(_ campaignId: Int, _ isEnabled: Bool) {
        print("캠페인 알림 설정 변경 - ID: \(campaignId), 상태: \(isEnabled)")
        
        Task {
            let response = await MatrixPushFunctions.updateCampaignConsent(campaignId: campaignId, consented: isEnabled)
            if let response = response {
                print("캠페인 설정 성공: \(response)")
            } else {
                print("캠페인 설정 실패")
            }
        }
    }
}



struct SettingsTabView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsTabView(selectedTab: .constant(3))
    }
}
