
//
//  ProductDetailView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 상품 상세 뷰
/// 상품의 상세 정보를 표시하는 모달 뷰
struct ProductDetailView: View {
    let product: Product
    let cartManager: CartManager
    @Binding var isPresented: Bool
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // 상품 이미지
                    ProductDetailImageView(product: product)
                    
                    // 상품 정보 및 버튼들
                    ProductDetailInfoView(product: product, cartManager: cartManager)
                }
            }
            .navigationTitle("Product Detail")
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("닫기") {
                        isPresented = false
                    }
                }
            }
        }
    }
}

/// 상품 상세 이미지 뷰
/// 상품 상세 화면의 이미지 영역
struct ProductDetailImageView: View {
    let product: Product
    
    var body: some View {
        AsyncImage(url: URL(string: product.imageUrl ?? "")) { image in
            image
                .resizable()
                .aspectRatio(contentMode: .fill)
        } placeholder: {
            ProgressView()
        }
        .frame(height: 250)
        .clipped()
        .cornerRadius(12)
        .padding(.horizontal, 20)
    }
}

/// 상품 상세 정보 뷰
/// 상품 상세 화면의 정보 및 버튼 영역
struct ProductDetailInfoView: View {
    let product: Product
    let cartManager: CartManager
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            VStack(alignment: .leading, spacing: 8) {
                Text("ID: \(product.id)")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
                
                Text(product.name)
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.black)
                
                Text("\(product.price)원")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(Color(red: 0.0, green: 0.6, blue: 0.0))
            }
            
            // 버튼들
            ProductDetailButtonsView(product: product, cartManager: cartManager)
            
            // 상세 설명
            ProductDetailDescriptionView(product: product)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 20)
    }
}

/// 상품 상세 버튼들 뷰
/// 장바구니 담기 및 구매하기 버튼
struct ProductDetailButtonsView: View {
    let product: Product
    let cartManager: CartManager
    
    var body: some View {
        VStack(spacing: 12) {
            Button(action: {
                cartManager.addToCart(product: product)
            }) {
                Text("장바구니 담기")
                    .foregroundColor(.white)
                    .font(.system(size: 16, weight: .medium))
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(Color(red: 0.153, green: 0.224, blue: 0.404))
                    .cornerRadius(22)
            }
            
            Button(action: {
                // 구매하기 로직
                print("구매하기 버튼 탭됨")
            }) {
                Text("구매하기")
                    .foregroundColor(.white)
                    .font(.system(size: 16, weight: .medium))
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(Color(red: 0.153, green: 0.224, blue: 0.404))
                    .cornerRadius(22)
            }
        }
    }
}

/// 상품 상세 설명 뷰
/// 상품의 상세 설명을 표시하는 영역
struct ProductDetailDescriptionView: View {
    let product: Product
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("상세 설명")
                .font(.system(size: 18, weight: .medium))
                .foregroundColor(.black)
            
            Text(product.description ?? "이 영역에 상품의 상세 스펙과 설명이 들어간다.")
                .font(.system(size: 14))
                .foregroundColor(.gray)
                .multilineTextAlignment(.leading)
        }
    }
}
