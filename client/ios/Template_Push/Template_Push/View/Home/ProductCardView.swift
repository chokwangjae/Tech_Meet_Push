
//
//  ProductCardView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 상품 카드 뷰
/// 그리드에서 사용되는 개별 상품 카드 컴포넌트
struct ProductCardView: View {
    let product: Product
    let cartManager: CartManager
    let onProductTap: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // 상품 이미지
            Button(action: onProductTap) {
                AsyncImage(url: URL(string: product.imageUrl ?? "")) { phase in
                    switch phase {
                    case .empty:
                        ZStack {
                            Color.gray.opacity(0.1)
                            ProgressView()
                        }
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    case .failure:
                        ZStack {
                            Color.gray.opacity(0.3)
                            Image(systemName: "wifi.exclamationmark")
                                .foregroundColor(.white)
                                .font(.title)
                        }
                    @unknown default:
                        EmptyView()
                    }
                }
                .frame(height: 120)
                .clipped()
                .cornerRadius(8)
            }
            
            // 상품 정보
            VStack(alignment: .leading, spacing: 4) {
                Text(product.name)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.black)
                    .lineLimit(2)
                
                Text("\(product.price)원")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color(red: 0.0, green: 0.6, blue: 0.0))
            }
            
            // Add to Cart 버튼
            Button(action: {
                cartManager.addToCart(product: product)
            }) {
                Text("Add to Cart")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 32)
                    .background(Color(red: 0.2, green: 0.3, blue: 0.4))
                    .cornerRadius(16)
            }
        }
        .padding(12)
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .gray.opacity(0.2), radius: 4, x: 0, y: 2)
    }
}
