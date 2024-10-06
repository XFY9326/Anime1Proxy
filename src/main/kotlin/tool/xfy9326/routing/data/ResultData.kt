package tool.xfy9326.routing.data

import kotlinx.serialization.Serializable
import tool.xfy9326.anime1.VideoCategory
import tool.xfy9326.anime1.VideoCategoryPost
import tool.xfy9326.anime1.VideoPost

fun VideoPost.getProxyUrl(baseUrl: String): String = "${baseUrl}/v/${id}"

fun VideoPost.getProxyCategoryUrl(baseUrl: String): String = "${baseUrl}/v/${categoryId}"

fun VideoCategoryPost.getProxyUrl(baseUrl: String): String = "${baseUrl}/c/${id}"

fun VideoCategory.getProxyUrl(baseUrl: String): String = "${baseUrl}/c/${id}"

sealed interface UrlParseResult {
    val type: String
    val id: Int
    val title: String
    val url: String
}

@Serializable
data class VideoUrlParseResult(
    override val type: String = "single",
    override val id: Int,
    override val title: String,
    override val url: String,
    val categoryUrl: String,
) : UrlParseResult

@Serializable
data class VideoCategoryUrlParseResult(
    override val type: String = "category",
    override val id: Int,
    override val title: String,
    override val url: String,
    val videos: List<Video>,
) : UrlParseResult {
    @Serializable
    data class Video(
        val id: Int,
        val title: String,
        val url: String,
    )
}

@Serializable
data class VideoCategoryResult(
    val id: Int,
    val title: String,
    val status: String,
    val year: String,
    val season: String,
    val captionGroup: String?,
    val url: String,
    val isExternal: Boolean
)
