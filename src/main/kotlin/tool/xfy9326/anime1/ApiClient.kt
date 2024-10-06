package tool.xfy9326.anime1

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

object ApiClient {
    private val WEB_URL = Url("https://anime1.me")
    private val API_URL = Url("https://v.anime1.me/api")
    val VIDEO_CATEGORY_LIST_URL = Url("https://d1zquzjgwo9yb.cloudfront.net")

    private val CATEGORY_ID_PATTERN = "'categoryID':\\s'(.*?)'".toRegex()
    private val POST_EPISODE_PATTERN = ".*?\\[(.*?)]".toRegex()
    private val EXTERNAL_TITLE_URL_REGEX = "<a href=\"(.*?)\">(.*?)</a>".toRegex()

    private const val PROXY_EXPIRE_OFFSET_SECONDS = 5L

    private val client: HttpClient by lazy {
        HttpClient(Java) {
            engine {
                pipelining = true
                protocolVersion = java.net.http.HttpClient.Version.HTTP_2
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpCookies)
            install(HttpTimeout) {
                requestTimeoutMillis = TimeUnit.SECONDS.toMillis(10)
            }
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(maxRetries = 1)
            }
            install(ContentNegotiation) {
                json()
            }
            BrowserUserAgent()
            defaultRequest {
                header(HttpHeaders.Accept, "*/*")
                header(HttpHeaders.AcceptLanguage, "zh-CN,zh;q=0.9,en;q=0.8")
                header(HttpHeaders.Connection, "keep-alive")
                header(HttpHeaders.Origin, WEB_URL)
                header(HttpHeaders.Referrer, "$WEB_URL/")
            }
        }
    }

    private fun parseVideoPosts(url: Url, bodyElement: Element): List<VideoPost> {
        val posts = bodyElement.getElementsByTag("article").asSequence().filter {
            it.hasAttr("id")
        }.mapNotNull { article ->
            val headerNode = article.getElementsByClass("entry-header").firstOrNull() ?: error("Unknown article header")
            val contentNode = article.getElementsByClass("entry-content").firstOrNull() ?: error("Unknown article content")
            val videoNode = article.getElementsByTag("video").firstOrNull() ?: error("Unknown video")

            val allPostsId = contentNode.getElementsContainingOwnText("全集連結").firstOrNull()
                ?.attr("href")?.split("=")?.getOrNull(1) ?: error("Unknown posts id")
            val nextPostId = contentNode.getElementsContainingOwnText("下一集").firstOrNull()
                ?.attr("href")?.split("=")?.getOrNull(1)

            val postId = article.attr("id").split("-")[1]
            val postTitle = headerNode.getElementsByTag("h2").text().trim()
            val episode = POST_EPISODE_PATTERN.find(postTitle)?.groups?.get(1)?.value?.trim()
            val dateTime = headerNode.getElementsByTag("time").firstOrNull()?.attr("datetime") ?: error("Unknown article time")

            @Suppress("SpellCheckingInspection") (VideoPost(
                id = postId.toInt(),
                title = postTitle,
                url = url,
                episode = episode,
                episodeNum = episode?.toIntOrNull(),
                dateTime = dateTime,
                categoryId = allPostsId.toInt(),
                videoId = videoNode.attr("data-vid").ifEmpty { error("Unknown video id") },
                thumbnailsServer = videoNode.attr("data-tserver").ifEmpty { error("Unknown video thumbnails server") },
                apiData = videoNode.attr("data-apireq").decodeURLPart().ifEmpty { error("Unknown video api data") },
                nextPostId = nextPostId,
            ))
        }.toList()
        return if (posts.all { it.episode == null }) {
            posts.reversed()
        } else {
            posts.sorted()
        }
    }

    private fun parseVideoCategoryPost(url: Url, document: Document): VideoCategoryPost {
        val categoryId = CATEGORY_ID_PATTERN.find(
            document.getElementsByTag("script").first { "categoryID" in it.data() }.data()
        )?.groups?.get(1)?.value?.trim() ?: error("Unknown category id")
        val categoryTitle = document.getElementsByClass("page-title").firstOrNull()?.text()?.trim() ?: error("No page title found")
        val posts = parseVideoPosts(url, document.body())

        return VideoCategoryPost(
            id = categoryId.toInt(),
            title = categoryTitle,
            url = url,
            posts = posts,
        )
    }

    private fun parseVideoArticlePage(url: Url, html: String, category: Boolean? = null): VideoArticle {
        val document = Jsoup.parse(html)
        val bodyElement = document.body()
        return if (bodyElement.hasClass("category")) {
            if (category == false) error("Excepted video post, got ${bodyElement.className()}")
            parseVideoCategoryPost(url, document)
        } else if (bodyElement.hasClass("single-post")) {
            if (category == true) error("Excepted video category post, got ${bodyElement.className()}")
            parseVideoPosts(url, bodyElement).first()
        } else {
            error("Unknown article content")
        }
    }

    suspend fun getVideoPost(postId: Int): VideoPost {
        val response = client.get {
            url.takeFrom(WEB_URL).appendPathSegments(postId.toString())
        }
        return parseVideoArticlePage(response.request.url, response.bodyAsText(), false) as VideoPost
    }

    suspend fun getVideoCategoryPost(categoryId: Int): VideoCategoryPost {
        val response = client.get {
            url.takeFrom(WEB_URL).parameters.append("cat", categoryId.toString())
        }
        return parseVideoArticlePage(response.request.url, response.bodyAsText(), true) as VideoCategoryPost
    }

    suspend fun getVideoArticle(url: Url): VideoArticle {
        require(url.host.endsWith(WEB_URL.host)) { "Invalid video article URL: $url" }
        val response = client.get(url)
        return parseVideoArticlePage(response.request.url, response.bodyAsText())
    }

    suspend fun getVideoPlay(post: VideoPost): VideoPlay {
        val response = client.submitForm(parameters { append("d", post.apiData) }) {
            url.takeFrom(API_URL)
            header(HttpHeaders.CacheControl, "max-age=0")
            header(HttpHeaders.Pragma, "no-cache")
        }
        val content = response.body<JsonObject>()["s"]?.jsonArray?.first()?.jsonObject!!
        val cookies = response.setCookie()
        val expire = cookies.first { it.name == "e" }.value.toInt()
        return VideoPlay(
            url = Url("${API_URL.protocol.name}:${content["src"]?.jsonPrimitive?.content!!}"),
            type = content["type"]?.jsonPrimitive?.content!!,
            cookies = cookies,
            expire = (expire - PROXY_EXPIRE_OFFSET_SECONDS).coerceAtLeast(0)
        )
    }

    suspend fun getVideoCategories(): List<VideoCategory> {
        return client.get(VIDEO_CATEGORY_LIST_URL) {
            header(HttpHeaders.Accept, "application/json, text/javascript, */*; q=0.01")
            header(HttpHeaders.CacheControl, "max-age=0")
            header(HttpHeaders.Pragma, "no-cache")
        }.body<JsonArray>().asSequence().filterIsInstance<JsonArray>().map {
            val externalMatches = EXTERNAL_TITLE_URL_REGEX.find(it[1].jsonPrimitive.content)
            VideoCategory(
                id = it[0].jsonPrimitive.int,
                title = externalMatches?.groups?.get(2)?.value ?: it[1].jsonPrimitive.content,
                status = it[2].jsonPrimitive.content,
                year = it[3].jsonPrimitive.content,
                season = it[4].jsonPrimitive.content,
                captionGroup = it[5].jsonPrimitive.content.ifEmpty { null },
                externalUrl = externalMatches?.groups?.get(1)?.value
            )
        }.toList()
    }

    suspend fun checkVideoPlay(videoPlay: VideoPlay, range: String? = null, ifRange: String? = null): Headers {
        return client.head(videoPlay.url) {
            if (range != null) header(HttpHeaders.Range, range)
            if (ifRange != null) header(HttpHeaders.IfRange, ifRange)
            header(HttpHeaders.AcceptEncoding, "identity;q=1, *;q=0")
        }.headers
    }

    suspend fun openVideoPlay(
        videoPlay: VideoPlay, range: String? = null, ifRange: String? = null, onConnected: suspend (RemoteVideo) -> Unit
    ) {
        client.prepareGet(videoPlay.url) {
            if (range != null) header(HttpHeaders.Range, range)
            if (ifRange != null) header(HttpHeaders.IfRange, ifRange)
            header(HttpHeaders.AcceptEncoding, "identity;q=1, *;q=0")
        }.execute {
            val video = RemoteVideo(
                url = videoPlay.url,
                status = it.status,
                headers = it.headers,
                contentType = it.contentType(),
                contentLength = it.contentLength(),
                channel = it.bodyAsChannel()
            )
            onConnected(video)
        }
    }
}