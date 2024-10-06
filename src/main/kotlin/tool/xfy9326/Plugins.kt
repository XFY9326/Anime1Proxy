package tool.xfy9326

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import kotlinx.serialization.json.Json

fun Application.configurePlugins() {
    install(CallLogging)
    install(Resources)
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
            }
        )
    }
}
