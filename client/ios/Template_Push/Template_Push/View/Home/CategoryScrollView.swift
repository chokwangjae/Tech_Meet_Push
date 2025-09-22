
//
//  CategoryScrollView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 카테고리 스크롤 뷰
/// 상품 카테고리 선택 탭들을 가로 스크롤로 표시
struct CategoryScrollView: View {
    @Binding var selectedCategory: ProductCategory?
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 20) {
                // 전체 카테고리 버튼
                Button(action: {
                    selectedCategory = nil
                    print("Selected category: All")
                }) {
                    Text("All")
                        .font(.system(size: 16))
                        .foregroundColor(selectedCategory == nil ? .white : .gray)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(selectedCategory == nil ? Color(red: 0.153, green: 0.224, blue: 0.404) : Color.gray.opacity(0.1))
                        .cornerRadius(20)
                }
                
                ForEach(ProductCategory.allCases, id: \.self) { category in
                    Button(action: {
                        selectedCategory = category
                        print("Selected category: \(category.rawValue)")
                    }) {
                        Text(category.rawValue)
                            .font(.system(size: 16))
                            .foregroundColor(selectedCategory == category ? .white : .gray)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(selectedCategory == category ? Color(red: 0.153, green: 0.224, blue: 0.404) : Color.gray.opacity(0.1))
                            .cornerRadius(20)
                    }
                }
            }
            .padding(.horizontal, 20)
        }
    }
}
