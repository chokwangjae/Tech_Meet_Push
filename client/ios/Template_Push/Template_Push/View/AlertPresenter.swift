//
//  AlertPresenter.swift
//  Template
//
//  Created by HyeongyuIM on 5/21/25.
//

import SwiftUI
import UIKit

struct AlertPresenter: UIViewControllerRepresentable {
    let title: String
    let message: String
    let onConfirm: () -> Void
    let onCancel: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = UIViewController()
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "취소", style: .cancel, handler: { _ in
                onCancel()
            }))
            alert.addAction(UIAlertAction(title: "확인", style: .default, handler: { _ in
                onConfirm()
            }))
            controller.present(alert, animated: true)
        }
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
