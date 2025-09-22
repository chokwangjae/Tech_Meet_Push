//
//  MenuButton.swift
//  Template
//
//  Created by HyeongyuIM on 6/18/25.
//

import SwiftUI
import MUtil

struct MenuButton: View {
    let label: String
    let systemImage: String // SF Symbol 이름

    var body: some View {
        HStack {
            Image(systemName: systemImage)
                .font(.headline)
                .foregroundColor(Color(hex: "#6F459C"))
            
            Spacer()
            
            Text(label)
                .font(.headline)
                .foregroundColor(Color(hex: "#6F459C"))
            
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(hex: "#E9D8F1"))
        .foregroundColor(.black)
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.2), radius: 5, x: 0, y: 4)
    }
}

extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex)
        _ = scanner.scanString("#") // # 무시
        var rgb: UInt64 = 0
        scanner.scanHexInt64(&rgb)
        
        let r = Double((rgb >> 16) & 0xFF) / 255
        let g = Double((rgb >> 8) & 0xFF) / 255
        let b = Double(rgb & 0xFF) / 255
        
        self.init(red: r, green: g, blue: b)
    }
}

