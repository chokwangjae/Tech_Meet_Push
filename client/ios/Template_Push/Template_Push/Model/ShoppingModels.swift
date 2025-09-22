//
//  ShoppingModels.swift
//  Template_Push
//
//  Created by Assistant
//

import Foundation

// MARK: - 상품 모델
struct Product: Identifiable, Codable {
    let id: String
    let name: String
    let price: Int
    let imageUrl: String?
    let description: String?
    let category: ProductCategory
}

enum ProductCategory: String, CaseIterable, Codable {
    case home = "Home"
    case electronics = "Electronics"
    case fashion = "Fashion"
    case sports = "Sports"
    case books = "Books"
    case beauty = "Beauty"
}

// MARK: - 장바구니 아이템 모델
struct CartItem: Identifiable, Codable {
    let id = UUID()
    let product: Product
    var quantity: Int
    
    var totalPrice: Int {
        return product.price * quantity
    }
}

// MARK: - 장바구니 관리 클래스
class CartManager: ObservableObject {
    @Published var cartItems: [CartItem] = []
    
    var totalPrice: Int {
        return cartItems.reduce(0) { $0 + $1.totalPrice }
    }
    
    var totalItems: Int {
        return cartItems.reduce(0) { $0 + $1.quantity }
    }
    
    func addToCart(product: Product) {
        if let existingIndex = cartItems.firstIndex(where: { $0.product.id == product.id }) {
            cartItems[existingIndex].quantity += 1
        } else {
            cartItems.append(CartItem(product: product, quantity: 1))
        }
    }
    
    func removeFromCart(cartItem: CartItem) {
        cartItems.removeAll { $0.id == cartItem.id }
    }
    
    func clearCart() {
        cartItems.removeAll()
    }
}

// MARK: - 샘플 데이터
let sampleProducts: [Product] = [
    // Electronics
    Product(id: "p1", name: "Wireless Earbuds", price: 59000, imageUrl: "https://picsum.photos/300/300", description: "고품질 무선 이어폰입니다.", category: .electronics),
    Product(id: "p2", name: "Smart Watch", price: 129000, imageUrl: "https://picsum.photos/300/301", description: "스마트 워치입니다.", category: .electronics),
    Product(id: "p3", name: "Gaming Headset", price: 89000, imageUrl: "https://picsum.photos/300/302", description: "고품질 게이밍 헤드셋입니다.", category: .electronics),
    Product(id: "p4", name: "Bluetooth Speaker", price: 75000, imageUrl: "https://picsum.photos/300/303", description: "휴대용 블루투스 스피커입니다.", category: .electronics),
    
    // Home
    Product(id: "p5", name: "Vacuum Cleaner", price: 99000, imageUrl: "https://picsum.photos/300/304", description: "강력한 진공 청소기입니다.", category: .home),
    Product(id: "p6", name: "Sofa Cushion", price: 19000, imageUrl: "https://picsum.photos/300/305", description: "편안한 소파 쿠션입니다.", category: .home),
    Product(id: "p7", name: "Coffee Maker", price: 149000, imageUrl: "https://picsum.photos/300/306", description: "자동 커피 머신입니다.", category: .home),
    Product(id: "p8", name: "Table Lamp", price: 45000, imageUrl: "https://picsum.photos/300/307", description: "모던 테이블 램프입니다.", category: .home),
    
    // Fashion
    Product(id: "p9", name: "Leather Jacket", price: 189000, imageUrl: "https://picsum.photos/300/308", description: "고급 가죽 자켓입니다.", category: .fashion),
    Product(id: "p10", name: "Sneakers", price: 85000, imageUrl: "https://picsum.photos/300/309", description: "편안한 운동화입니다.", category: .fashion),
    Product(id: "p11", name: "Backpack", price: 65000, imageUrl: "https://picsum.photos/300/310", description: "실용적인 백팩입니다.", category: .fashion),
    Product(id: "p12", name: "Sunglasses", price: 120000, imageUrl: "https://picsum.photos/300/311", description: "UV 차단 선글라스입니다.", category: .fashion),
    
    // Sports
    Product(id: "p13", name: "Yoga Mat", price: 35000, imageUrl: "https://picsum.photos/300/312", description: "논슬립 요가 매트입니다.", category: .sports),
    Product(id: "p14", name: "Tennis Racket", price: 149000, imageUrl: "https://picsum.photos/300/313", description: "프로페셔널 테니스 라켓입니다.", category: .sports),
    Product(id: "p15", name: "Running Shoes", price: 95000, imageUrl: "https://picsum.photos/300/314", description: "경량 러닝화입니다.", category: .sports),
    Product(id: "p16", name: "Fitness Tracker", price: 79000, imageUrl: "https://picsum.photos/300/315", description: "활동량 측정 밴드입니다.", category: .sports),
    
    // Books
    Product(id: "p17", name: "Programming Guide", price: 25000, imageUrl: "https://picsum.photos/300/316", description: "프로그래밍 입문서입니다.", category: .books),
    Product(id: "p18", name: "Cook Book", price: 18000, imageUrl: "https://picsum.photos/300/317", description: "홈쿠킹 레시피북입니다.", category: .books),
    
    // Beauty
    Product(id: "p19", name: "Face Cream", price: 55000, imageUrl: "https://picsum.photos/300/318", description: "보습 페이스 크림입니다.", category: .beauty),
    Product(id: "p20", name: "Hair Dryer", price: 89000, imageUrl: "https://picsum.photos/300/319", description: "이온 헤어드라이어입니다.", category: .beauty)
]
