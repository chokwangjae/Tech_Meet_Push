
//
//  CampaignLoadingView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 캠페인 로딩 뷰
/// 캠페인 목록 로딩 중 표시되는 뷰
struct CampaignLoadingView: View {
    var body: some View {
        VStack {
            ProgressView("캠페인 목록 로딩 중...")
                .foregroundColor(.gray)
                .padding(.vertical, 30)
        }
        .background(Color.white)
    }
}
