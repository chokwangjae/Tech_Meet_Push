
//
//  ProductGridView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 상품 그리드 뷰
/// 선택된 카테고리에 따른 상품들을 그리드로 표시
struct ProductGridView: View {
    let selectedCategory: ProductCategory?
    let cartManager: CartManager
    @Binding var selectedProduct: Product?
    @Binding var showingProductDetail: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(selectedCategory?.rawValue ?? "Recommended")
                .font(.system(size: 20, weight: .medium))
                .foregroundColor(.black)
                .padding(20)
            
            ScrollView {
                LazyVGrid(columns: [
                    GridItem(.flexible()),
                    GridItem(.flexible())
                ], spacing: 16) {
                    ForEach(filteredProducts(selectedCategory: selectedCategory)) { product in
                        ProductCardView(
                            product: product,
                            cartManager: cartManager,
                            onProductTap: {
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                                    selectedProduct = product
                                }
                                showingProductDetail = true
                            }
                        )
                    }
                }
                .padding(.horizontal, 20)
            }
        }
    }
    
    /// 선택된 카테고리에 따라 상품 필터링
    /// - Parameter selectedCategory: 선택된 카테고리 (nil이면 모든 상품 반환)
    /// - Returns: 필터링된 상품 배열
    private func filteredProducts(selectedCategory: ProductCategory?) -> [Product] {
        guard let category = selectedCategory else {
            print("Filtering: All products (\(sampleProducts.count) items)")
            return sampleProducts
        }
        let filtered = sampleProducts.filter { $0.category == category }
        print("Filtering: \(category.rawValue) - \(filtered.count) items")
        return filtered
    }
}
