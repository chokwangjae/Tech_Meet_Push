//
//  TemplateSwiftUIApp.swift
//  TemplateSwiftUI
//
//  Created by UAPMobile Team on 2021/12/29.
//

import SwiftUI

@main
struct TemplateSwiftUIApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @Environment(\.scenePhase) var scenePhase
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }.onChange(of: scenePhase) { newScenePhase in
            switch newScenePhase {
                case .active:
                    print("App is active")
                case .inactive:
                    print("App is inactive")
                case .background:
                    print("App is in background")
                @unknown default:
                    print("Oh - interesting: I received an unexpected new value.")
            }
        }
    }
}
