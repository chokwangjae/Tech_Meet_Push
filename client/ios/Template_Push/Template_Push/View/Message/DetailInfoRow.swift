
//
//  DetailInfoRow.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 상세 정보 행
/// 라벨과 값을 표시하는 행 뷰
struct DetailInfoRow: View {
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.gray)
                .frame(width: 80, alignment: .leading)
            
            Text(value)
                .font(.system(size: 13))
                .foregroundColor(.black)
                .multilineTextAlignment(.leading)
            
            Spacer()
        }
    }
}
