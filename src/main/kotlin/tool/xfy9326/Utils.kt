package tool.xfy9326

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*

val ApplicationCall.serverUrl: String
    get() = url {
        encodedPath = ""
        parameters.clear()
    }.trimEnd('/')
