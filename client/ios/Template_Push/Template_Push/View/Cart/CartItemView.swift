
//
//  CartItemView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 장바구니 아이템 뷰
/// 개별 장바구니 아이템을 표시하는 뷰
struct CartItemView: View {
    let cartItem: CartItem
    let cartManager: CartManager
    
    var body: some View {
        HStack(spacing: 12) {
            // 상품 이미지
            AsyncImage(url: URL(string: cartItem.product.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Rectangle()
                    .fill(Color.gray.opacity(0.3))
            }
            .frame(width: 60, height: 60)
            .clipped()
            .cornerRadius(8)
            
            // 상품 정보
            VStack(alignment: .leading, spacing: 4) {
                Text(cartItem.product.name)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.black)
                    .lineLimit(2)
                
                Text("\(cartItem.product.price)원 x \(cartItem.quantity)")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
            }
            
            Spacer()
            
            // 삭제 버튼
            Button(action: {
                cartManager.removeFromCart(cartItem: cartItem)
            }) {
                Text("삭제")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.white)
                    .frame(width: 50, height: 28)
                    .background(Color(red: 0.153, green: 0.224, blue: 0.404))
                    .cornerRadius(14)
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .gray.opacity(0.1), radius: 2, x: 0, y: 1)
    }
}
