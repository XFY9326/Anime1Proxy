package tool.xfy9326.anime1

import io.ktor.http.*
import io.ktor.utils.io.*
import java.text.Collator

sealed interface VideoArticle {
    val id: Int
    val title: String
    val url: Url
}

data class VideoPost(
    override val id: Int,
    override val title: String,
    override val url: Url,
    val episode: String?,
    val episodeNum: Int?,
    val dateTime: String,
    val categoryId: Int,
    val videoId: String,
    val thumbnailsServer: String,
    val apiData: String,
    val nextPostId: String?
) : VideoArticle, Comparable<VideoPost> {
    override fun compareTo(other: VideoPost): Int {
        val n1 = this.episodeNum ?: Int.MIN_VALUE
        val n2 = this.episodeNum ?: Int.MIN_VALUE
        return if (n1 != n2) {
            n1.compareTo(n2)
        } else if (this.episode == null && other.episode != null) {
            1
        } else if (this.episode != null && other.episode == null) {
            -1
        } else if (this.episode != null && other.episode != null) {
            Collator.getInstance().compare(this.episode, other.episode)
        } else {
            0
        }
    }
}

data class VideoCategoryPost(
    override val id: Int,
    override val title: String,
    override val url: Url,
    val posts: List<VideoPost>
) : VideoArticle

data class VideoCategory(
    val id: Int,
    val title: String,
    val status: String,
    val year: String,
    val season: String,
    val captionGroup: String?,
    val externalUrl: String?
)

data class VideoPlay(
    val url: Url,
    val type: String,
    val cookies: List<Cookie>,
    val expire: Long
)

data class RemoteVideo(
    val url: Url,
    val status: HttpStatusCode,
    val headers: Headers,
    val contentType: ContentType?,
    val contentLength: Long?,
    val channel: ByteReadChannel
)
