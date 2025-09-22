//
//  PushNavigationManager.swift
//  Template_Push
//
//  Created by UAPMobile Team on 2024/12/19.
//

import Foundation
import SwiftUI

/// 푸시 알림 네비게이션 관리자
/// 푸시 알림을 통한 딥링크 처리를 담당
class PushNavigationManager: ObservableObject {
    static let shared = PushNavigationManager()
    
    @Published var shouldShowProductDetail = false
    @Published var targetProductId: String? = nil
    
    private init() {}
    
    /// 푸시 알림 페이로드 처리
    /// - Parameter userInfo: FCM에서 받은 푸시 데이터
    func handlePushNotification(userInfo: [AnyHashable: Any]) {
        print("푸시 알림 처리 시작: \(userInfo)")
        
        // payload에서 productId 추출
        if let payload = userInfo["payload"] as? String {
            print("페이로드 발견: \(payload)")
            
            // JSON 문자열을 파싱하여 productId 추출
            if let productId = extractProductId(from: payload) {
                print("상품 ID 추출 성공: \(productId)")
                
                DispatchQueue.main.async {
                    self.targetProductId = productId
                    self.shouldShowProductDetail = true
                }
            }
        }
    }
    
    /// JSON 문자열에서 productId 추출
    /// - Parameter payload: JSON 형태의 페이로드 문자열
    /// - Returns: 추출된 productId
    private func extractProductId(from payload: String) -> String? {
        // JSON 문자열을 Data로 변환
        guard let data = payload.data(using: .utf8) else {
            print("페이로드를 Data로 변환 실패")
            return nil
        }
        
        do {
            // JSON 파싱
            if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
               let productId = json["productId"] as? String {
                return productId
            }
        } catch {
            print("JSON 파싱 오류: \(error)")
        }
        
        return nil
    }
    
    /// 상품 상세 뷰 표시 상태 리셋
    func resetProductDetailState() {
        DispatchQueue.main.async {
            self.shouldShowProductDetail = false
            self.targetProductId = nil
        }
    }
}

/// 푸시 알림 처리 결과
struct PushNotificationResult {
    let shouldNavigate: Bool
    let productId: String?
    
    static let empty = PushNotificationResult(shouldNavigate: false, productId: nil)
}
