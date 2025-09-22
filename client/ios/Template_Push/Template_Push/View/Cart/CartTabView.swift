//
//  CartTabView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 장바구니 탭 화면
/// 장바구니에 담긴 상품들을 표시하고 결제 기능을 제공
struct CartTabView: View {
    let cartManager: CartManager
    
    var body: some View {
        VStack(spacing: 0) {
            // 헤더
            Text("Cart")
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 20)
            
            if cartManager.cartItems.isEmpty {
                EmptyCartView()
            } else {
                CartContentView(cartManager: cartManager)
            }
        }
        .background(Color.white)
    }
}

/// 빈 장바구니 뷰
/// 장바구니가 비어있을 때 표시되는 뷰
struct EmptyCartView: View {
    var body: some View {
        VStack {
            Spacer()
            Text("장바구니가 비어있습니다")
                .foregroundColor(.gray)
                .font(.system(size: 16))
            Spacer()
        }
    }
}

/// 장바구니 내용 뷰
/// 장바구니 아이템들과 결제 버튼을 표시
struct CartContentView: View {
    let cartManager: CartManager
    
    var body: some View {
        VStack(spacing: 0) {
            // 장바구니 아이템 목록
            ScrollView {
                LazyVStack(spacing: 16) {
                    ForEach(cartManager.cartItems) { cartItem in
                        CartItemView(cartItem: cartItem, cartManager: cartManager)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
            }
            
            // 하단 결제 영역
            CartCheckoutView(cartManager: cartManager)
        }
    }
}



struct CartTabView_Previews: PreviewProvider {
    static var previews: some View {
        CartTabView(cartManager: CartManager())
    }
}
