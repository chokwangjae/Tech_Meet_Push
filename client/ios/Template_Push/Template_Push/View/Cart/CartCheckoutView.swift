
//
//  CartCheckoutView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 장바구니 결제 뷰
/// 총 금액 표시 및 결제 버튼을 포함
struct CartCheckoutView: View {
    let cartManager: CartManager
    
    var body: some View {
        VStack(spacing: 16) {
            Divider()
            
            HStack {
                Text("합계: \(cartManager.totalPrice)원")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.black)
                
                Spacer()
                
                Button(action: {
                    // 결제 로직
                    print("결제하기 버튼 탭됨")
                }) {
                    Text("결제하기")
                        .foregroundColor(.white)
                        .font(.system(size: 16, weight: .medium))
                        .frame(width: 120, height: 44)
                        .background(Color(red: 0.153, green: 0.224, blue: 0.404))
                        .cornerRadius(22)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 20)
        }
        .background(Color.white)
    }
}
