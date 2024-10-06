package tool.xfy9326.routing.views

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import tool.xfy9326.anime1.ApiClient
import tool.xfy9326.anime1.VideoCategoryPost
import tool.xfy9326.routing.data.RouteVideoCategory
import tool.xfy9326.routing.data.getProxyUrl
import tool.xfy9326.serverUrl

private fun VideoCategoryPost.buildM3U8(baseUrl: String): String = buildString {
    appendLine("#EXTM3U")
    for (videoPost in posts) {
        appendLine("#EXTINF:-1,${videoPost.title}")
        appendLine(videoPost.getProxyUrl(baseUrl))
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.videoCategoryPage(params: RouteVideoCategory.CategoryId) {
    val baseUrl = call.serverUrl
    val post = ApiClient.getVideoCategoryPost(params.categoryId)
    call.response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment.withParameter("filename", "${post.title}.m3u8").toString()
    )
    call.response.header(HttpHeaders.XForwardedFor, post.url.toString())
    call.respondText(
        post.buildM3U8(baseUrl),
        ContentType.parse("application/x-mpegURL"),
        HttpStatusCode.OK
    )
}