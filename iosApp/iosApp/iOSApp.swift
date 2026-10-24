import SwiftUI
import Shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                // Einladungen (paarfinanz://invite/…) und Bestätigungslinks aus Supabase-Mails
                .onOpenURL { url in
                    MainViewControllerKt.handleOpenUrl(url: url.absoluteString)
                }
        }
    }
}
