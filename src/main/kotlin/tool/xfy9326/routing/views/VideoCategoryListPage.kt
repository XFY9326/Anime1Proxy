package tool.xfy9326.routing.views

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import tool.xfy9326.anime1.ApiClient
import tool.xfy9326.routing.data.VideoCategoryResult
import tool.xfy9326.routing.data.getProxyUrl
import tool.xfy9326.serverUrl

suspend fun PipelineContext<Unit, ApplicationCall>.videoCategoryListPage() {
    val baseUrl = call.serverUrl
    val categories = ApiClient.getVideoCategories()
    val result = categories.map {
        VideoCategoryResult(
            id = it.id,
            title = it.title,
            status = it.status,
            year = it.year,
            season = it.season,
            captionGroup = it.captionGroup,
            url = it.externalUrl ?: it.getProxyUrl(baseUrl),
            isExternal = it.externalUrl != null
        )
    }
    call.response.header(HttpHeaders.XForwardedFor, ApiClient.VIDEO_CATEGORY_LIST_URL.toString())
    call.respond(result)
}