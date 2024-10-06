package tool.xfy9326.routing

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import tool.xfy9326.routing.data.RouteUrlParser
import tool.xfy9326.routing.data.RouteVideo
import tool.xfy9326.routing.data.RouteVideoCategory
import tool.xfy9326.routing.views.*

fun Application.configureRouting() {
    routing {
        staticResources("/", "assets")
        get<RouteUrlParser> {
            urlParserPage(it)
        }
        get("l") {
            videoCategoryListPage()
        }
        get<RouteVideoCategory.CategoryId> {
            videoCategoryPage(it)
        }
        get<RouteVideo.PostId> {
            videoPage(it)
        }
        head<RouteVideo.PostId> {
            checkVideoPage(it)
        }
        options<RouteVideo.PostId> {
            checkVideoPage(it)
        }
        get<RouteVideo.PostId> {
            videoPage(it)
        }
    }
}
