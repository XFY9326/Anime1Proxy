package tool.xfy9326.routing.views

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import tool.xfy9326.anime1.ApiClient
import tool.xfy9326.anime1.VideoCategoryPost
import tool.xfy9326.anime1.VideoPost
import tool.xfy9326.routing.data.*
import tool.xfy9326.serverUrl

suspend fun PipelineContext<Unit, ApplicationCall>.urlParserPage(params: RouteUrlParser) {
    val baseUrl = call.serverUrl
    try {
        val url = Url(params.url)
        val article = ApiClient.getVideoArticle(url)
        val result = when (article) {
            is VideoCategoryPost -> VideoCategoryUrlParseResult(
                id = article.id,
                title = article.title,
                url = article.getProxyUrl(baseUrl),
                videos = article.posts.map {
                    VideoCategoryUrlParseResult.Video(
                        id = it.id,
                        title = it.title,
                        url = it.getProxyUrl(baseUrl),
                    )
                }
            )

            is VideoPost -> VideoUrlParseResult(
                id = article.id,
                title = article.title,
                url = article.getProxyUrl(baseUrl),
                categoryUrl = article.getProxyCategoryUrl(baseUrl),
            )
        }
        call.response.header(HttpHeaders.XForwardedFor, article.url.toString())
        call.respond(result)
    } catch (e: URLParserException) {
        call.respond(HttpStatusCode.BadRequest, "Url parse error: ${e.message}")
    }
}