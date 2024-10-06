package tool.xfy9326.routing.views

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.anime1.ApiClient
import tool.xfy9326.anime1.VideoPlay
import tool.xfy9326.anime1.VideoPost
import tool.xfy9326.routing.data.RouteVideo
import java.util.*

private const val MAX_POST_CACHE_SIZE = 32
private const val MAX_PLAY_CACHE_SIZE = 12

private val cacheMutex = Mutex()
private val postCache: MutableMap<Int, VideoPost> = Collections.synchronizedMap(
    object : LinkedHashMap<Int, VideoPost>(1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, VideoPost>?): Boolean = size > MAX_POST_CACHE_SIZE
    }
)
private val playCache: MutableMap<Int, VideoPlay> = Collections.synchronizedMap(
    object : LinkedHashMap<Int, VideoPlay>(1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, VideoPlay>?): Boolean = size > MAX_PLAY_CACHE_SIZE
    }
)
private val KEEP_HEADERS = setOf(
    HttpHeaders.AcceptRanges,
    HttpHeaders.ContentRange,
    HttpHeaders.ETag,
    HttpHeaders.LastModified
)

private suspend fun loadVideoPlay(postId: Int): VideoPlay {
    if (playCache.containsKey(postId)) {
        cacheMutex.withLock {
            if (playCache.containsKey(postId)) {
                playCache[postId]?.let {
                    return it
                }
            }
        }
    }
    val post = postCache[postId] ?: ApiClient.getVideoPost(postId).also { postCache[postId] = it }
    return ApiClient.getVideoPlay(post).also { playCache[postId] = it }
}

private fun ApplicationResponse.applyVideoHeaders(url: Url, headers: Headers) {
    KEEP_HEADERS.forEach { h ->
        headers.getAll(h)?.forEach { v -> header(h, v) }
    }
    header(HttpHeaders.XForwardedFor, url.toString())
    header(HttpHeaders.AccessControlAllowOrigin, "*")
}

suspend fun PipelineContext<Unit, ApplicationCall>.videoPage(params: RouteVideo.PostId) {
    val play = loadVideoPlay(params.postId)
    val range = call.request.headers[HttpHeaders.Range] ?: "bytes=0-"
    val ifRange = call.request.headers[HttpHeaders.IfRange]
    try {
        ApiClient.openVideoPlay(play, range, ifRange) {
            call.response.applyVideoHeaders(it.url, it.headers)
            call.respondBytesWriter(it.contentType, it.status, it.contentLength) {
                it.channel.copyAndClose(this)
            }
        }
    } catch (e: IOException) {
        playCache.remove(params.postId)
        throw e
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkVideoPage(params: RouteVideo.PostId) {
    val play = loadVideoPlay(params.postId)
    val range = call.request.headers[HttpHeaders.Range] ?: "bytes=0-"
    val ifRange = call.request.headers[HttpHeaders.IfRange]
    try {
        val headers = ApiClient.checkVideoPlay(play, range, ifRange)
        call.response.applyVideoHeaders(play.url, headers)
        call.respond(HttpStatusCode.OK)
    } catch (e: IOException) {
        playCache.remove(params.postId)
        throw e
    }
}
