@file:Suppress("unused")

package tool.xfy9326.routing.data

import io.ktor.resources.*

@Resource("/p")
class RouteUrlParser(
    val url: String,
)

@Resource("/c")
class RouteVideoCategory {
    @Resource("{categoryId}")
    class CategoryId(
        val parent: RouteVideoCategory = RouteVideoCategory(),
        val categoryId: Int
    )
}

@Resource("/v")
class RouteVideo {
    @Resource("{postId}")
    class PostId(
        val parent: RouteVideo = RouteVideo(),
        val postId: Int
    )
}
