package tool.xfy9326

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import tool.xfy9326.routing.configureRouting
import java.awt.*
import java.net.URI
import javax.imageio.ImageIO


fun main() {
    val serverEngine = embeddedServer(CIO, port = 8520, host = "127.0.0.1", module = Application::module)
    launchSystemTray {
        serverEngine.stop()
    }
    serverEngine.start()
}

fun launchSystemTray(onExit: () -> Unit) {
    require(SystemTray.isSupported()) { "System tray is not supported" }

    val imageURL = Application::class.java.getResource("/images/icon.png")
    val trayIcon = TrayIcon(ImageIO.read(imageURL), "Anime1 Proxy").apply {
        isImageAutoSize = true
        popupMenu = PopupMenu()
        addActionListener {
            Desktop.getDesktop().browse(URI.create("http://127.0.0.1:8520"))
        }
    }

    val exitItem = MenuItem("Exit")
    exitItem.addActionListener {
        onExit()
        SystemTray.getSystemTray().remove(trayIcon)
    }
    trayIcon.popupMenu.add(exitItem)

    SystemTray.getSystemTray().add(trayIcon)

    trayIcon.displayMessage(
        "Anime1 Proxy",
        "Server is running: http://127.0.0.1:8520",
        TrayIcon.MessageType.INFO
    )
}

fun Application.module() {
    configurePlugins()
    configureRouting()
}
