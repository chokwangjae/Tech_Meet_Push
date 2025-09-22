//
//  HomeTabView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 홈 탭 화면
/// 상품 카테고리 선택 및 추천 상품 목록을 표시
struct HomeTabView: View {
    let cartManager: CartManager
    @Binding var selectedProduct: Product?
    @Binding var showingProductDetail: Bool
    @Binding var selectedCategory: ProductCategory?
    
    var body: some View {
        VStack(spacing: 0) {
            // 헤더
            VStack(spacing: 16) {
                Text("Deals for you (Home)")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                
                // 카테고리 탭
                CategoryScrollView(selectedCategory: $selectedCategory)
            }
            .padding(.top, 20)
            
            // 추천 상품 섹션
            ProductGridView(
                selectedCategory: selectedCategory,
                cartManager: cartManager,
                selectedProduct: $selectedProduct,
                showingProductDetail: $showingProductDetail
            )
            
            Spacer()
        }
        .background(Color.white)
    }
}



struct HomeTabView_Previews: PreviewProvider {
    static var previews: some View {
        HomeTabView(
            cartManager: CartManager(),
            selectedProduct: .constant(nil),
            showingProductDetail: .constant(false),
            selectedCategory: .constant(nil)
        )
    }
}
