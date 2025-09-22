//
//  MainTabView.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

/// 메인 탭 뷰
/// 홈, 장바구니, 메시지, 설정 탭을 관리하는 루트 뷰
struct MainTabView: View {
    @State private var selectedTab = 0 // 0: 홈, 1: 장바구니, 2: 메시지, 3: 설정
    @State private var selectedProduct: Product? = nil
    @State private var showingProductDetail = false
    @State private var selectedCategory: ProductCategory? = nil
    
    @StateObject private var cartManager = CartManager()
    @StateObject private var pushNavigationManager = PushNavigationManager.shared
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // 메인 컨텐츠 영역
                switch selectedTab {
                case 0:
                    HomeTabView(
                        cartManager: cartManager,
                        selectedProduct: $selectedProduct,
                        showingProductDetail: $showingProductDetail,
                        selectedCategory: $selectedCategory
                    )
                case 1:
                    CartTabView(cartManager: cartManager)
                case 2:
                    MessageTabView()
                case 3:
                    SettingsTabView(selectedTab: $selectedTab)
                default:
                    HomeTabView(
                        cartManager: cartManager,
                        selectedProduct: $selectedProduct,
                        showingProductDetail: $showingProductDetail,
                        selectedCategory: $selectedCategory
                    )
                }
                
                // 하단 탭바
                BottomTabBar(
                    selectedTab: $selectedTab,
                    cartTotalItems: cartManager.totalItems
                )
            }
        }
        .sheet(isPresented: $showingProductDetail) {
            if let product = selectedProduct {
                ProductDetailView(
                    product: product,
                    cartManager: cartManager,
                    isPresented: $showingProductDetail
                )
                .onDisappear {
                    self.selectedProduct = nil
                }
            }
        }
        .navigationBarHidden(true)
        .onChange(of: pushNavigationManager.shouldShowProductDetail) { shouldShow in
            if shouldShow {
                handlePushNotificationNavigation()
            }
        }
    }
    
    /// 푸시 알림을 통한 네비게이션 처리
    /// productId를 기반으로 해당 상품을 찾아 상세 뷰를 표시
    private func handlePushNotificationNavigation() {
        guard let productId = pushNavigationManager.targetProductId else {
            print("상품 ID가 없습니다")
            pushNavigationManager.resetProductDetailState()
            return
        }
        
        // sampleProducts에서 해당 상품 찾기
        if let product = sampleProducts.first(where: { $0.id == productId }) {
            print("상품 발견: \(product.name)")
            
            // 홈 탭으로 이동
            selectedTab = 0
            
            // 상품 상세 뷰 표시
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                selectedProduct = product
                showingProductDetail = true
                pushNavigationManager.resetProductDetailState()
            }
        } else {
            print("상품 ID \(productId)에 해당하는 상품을 찾을 수 없습니다")
            pushNavigationManager.resetProductDetailState()
        }
    }
}

struct MainTabView_Previews: PreviewProvider {
    static var previews: some View {
        MainTabView()
    }
}
