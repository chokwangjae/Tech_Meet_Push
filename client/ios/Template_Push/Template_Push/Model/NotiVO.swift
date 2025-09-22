//
//  NotiVO.swift
//  Template
//
//  Created by HyeongyuIM on 5/26/25.
//

import UIKit
import MatrixPush

extension MatrixPushReceiveDTO {
    var toVO: PushItem {
        return PushItem(
            id: self.pushDispatchId,
            messageType: self.messageType ?? "NOTIFICATION",
            title: self.title,
            body: self.body,
            imageUrl: self.imageUrl,
            clientStatus: self.clientStatus ?? ""
        )
    }
}

struct PushItem: Identifiable, Hashable {
    let id: String // uniqueSeq
    let messageType: String // "NOTIFICATION" 또는 "EVENT"
    let title: String?
    let body: String?
    let imageUrl: String?
    let clientStatus: String
    // sender, messagePriority, icon, timestamp 등 필요한 다른 속성 추가 가능
}
