package org.futo.circles.model

import android.graphics.Bitmap
import android.util.Size
import org.futo.circles.core.picker.MediaType
import org.matrix.android.sdk.api.session.crypto.attachments.ElementToDecrypt
import org.matrix.android.sdk.api.session.room.model.message.MessageType

enum class PostContentType(val typeKey: String) {
    TEXT_CONTENT(MessageType.MSGTYPE_TEXT),
    IMAGE_CONTENT(MessageType.MSGTYPE_IMAGE),
    VIDEO_CONTENT(MessageType.MSGTYPE_VIDEO),
    POLL_CONTENT(MessageType.MSGTYPE_POLL_START)
}

sealed class PostContent(open val type: PostContentType) {
    fun isMedia(): Boolean =
        type == PostContentType.IMAGE_CONTENT || type == PostContentType.VIDEO_CONTENT

    fun isPoll(): Boolean = type == PostContentType.POLL_CONTENT
}

data class TextContent(
    val message: String
) : PostContent(PostContentType.TEXT_CONTENT)

data class MediaContent(
    override val type: PostContentType,
    val mediaFileData: MediaFileData,
    val mediaContentInfo: MediaContentInfo,
) : PostContent(type) {
    val aspectRatio = mediaContentInfo.width.toFloat() / mediaContentInfo.height.toFloat()
    fun calculateSize(width: Int) = Size(width, (width / aspectRatio).toInt())
    fun getMediaType(): MediaType =
        if (type == PostContentType.VIDEO_CONTENT) MediaType.Video else MediaType.Image
}

data class MediaContentInfo(
    val caption: String?,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val duration: String,
    val thumbHash: String?
)

data class MediaFileData(
    val fileName: String,
    val mimeType: String,
    val fileUrl: String,
    val elementToDecrypt: ElementToDecrypt?
)

data class PollContent(
    val question: String,
    val state: PollState,
    val totalVotes: Int,
    val options: List<PollOption>,
    val isClosedType: Boolean
) : PostContent(PostContentType.POLL_CONTENT)

enum class PollState { Sending, Ready, Voted, Ended }

fun PollState.canEdit() = this == PollState.Sending || this == PollState.Ready

fun PollState.canVote() =
    this != PollState.Sending && this != PollState.Ended && this != PollState.Voted

data class PollOption(
    val optionId: String,
    val optionAnswer: String,
    val voteCount: Int,
    val voteProgress: Int,
    val isMyVote: Boolean,
    val isWinner: Boolean
)