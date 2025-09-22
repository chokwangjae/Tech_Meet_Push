//
//  Copyright (C) Inswave Systems, Inc - All Rights Reserved
//  Unauthorized copying of this file, via any medium is strictly prohibited
//  Proprietary and confidential
//  Written by Inswave Systems <webmaster@inswave.com>, April 2021
//

import Foundation
import Matrix_Mobile

final class SamplePlugin : MatrixMobilePlugin {
    func echo(_ callBack: CallBackManager) {
        if let param = callBack.command.argument(at: 0) as? String {
            callBack.success(data: param)
        } else {
            callBack.error(status: .INVALID_PARAM)
        }
    }
}
