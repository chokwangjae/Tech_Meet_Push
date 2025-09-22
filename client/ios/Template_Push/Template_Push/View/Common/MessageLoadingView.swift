
//
//  MessageLoadingView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 메시지 로딩 뷰
/// 메시지 로딩 중 표시되는 뷰
struct MessageLoadingView: View {
    var body: some View {
        VStack {
            Spacer()
            ProgressView("메시지 로딩 중...")
                .foregroundColor(.gray)
            Spacer()
        }
        .background(Color.white)
    }
}
