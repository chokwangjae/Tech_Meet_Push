
//
//  EmptyMessageView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 빈 메시지 뷰
/// 메시지가 없을 때 표시되는 뷰
struct EmptyMessageView: View {
    var body: some View {
        VStack {
            Spacer()
            Text("메시지가 없습니다")
                .foregroundColor(.gray)
                .font(.system(size: 16))
            Spacer()
        }
        .background(Color.white)
    }
}
