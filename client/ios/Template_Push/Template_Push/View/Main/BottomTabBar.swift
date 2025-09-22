
//
//  BottomTabBar.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import SwiftUI

// MARK: - 하단 탭바

/// 하단 탭바
/// 메인 네비게이션을 위한 하단 탭바 컴포넌트
struct BottomTabBar: View {
    @Binding var selectedTab: Int
    let cartTotalItems: Int
    
    var body: some View {
        HStack {
            // 홈 탭
            TabBarButton(
                icon: "house.fill",
                title: "홈",
                isSelected: selectedTab == 0,
                action: { selectedTab = 0 }
            )
            
            // 장바구니 탭
            TabBarButton(
                icon: "cart.fill",
                title: "장바구니",
                isSelected: selectedTab == 1,
                badge: cartTotalItems > 0 ? "\(cartTotalItems)" : nil,
                action: { selectedTab = 1 }
            )
            
            // 메시지 탭
            TabBarButton(
                icon: "message.fill",
                title: "알림",
                isSelected: selectedTab == 2,
                action: { selectedTab = 2 }
            )
            
            // 설정 탭
            TabBarButton(
                icon: "gearshape.fill",
                title: "설정",
                isSelected: selectedTab == 3,
                action: { selectedTab = 3 }
            )
        }
        .frame(height: 80)
        .background(Color.white)
        .overlay(
            Rectangle()
                .frame(height: 1)
                .foregroundColor(.gray.opacity(0.3)),
            alignment: .top
        )
    }
}

/// 탭바 버튼 컴포넌트
/// 개별 탭 버튼을 구성하는 컴포넌트 (아이콘, 제목, 뱃지 지원)
struct TabBarButton: View {
    let icon: String
    let title: String
    let isSelected: Bool
    let badge: String?
    let action: () -> Void
    
    init(icon: String, title: String, isSelected: Bool, badge: String? = nil, action: @escaping () -> Void) {
        self.icon = icon
        self.title = title
        self.isSelected = isSelected
        self.badge = badge
        self.action = action
    }
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 4) {
                ZStack {
                    Image(systemName: icon)
                        .foregroundColor(isSelected ? .blue : .gray)
                        .font(.system(size: 24))
                    
                    if let badge = badge {
                        Text(badge)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.white)
                            .frame(minWidth: 16, minHeight: 16)
                            .background(Color.red)
                            .clipShape(Circle())
                            .offset(x: 12, y: -12)
                    }
                }
                
                Text(title)
                    .foregroundColor(isSelected ? .blue : .gray)
                    .font(.system(size: 12))
            }
            .frame(maxWidth: .infinity)
        }
    }
}
