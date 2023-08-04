package org.futo.circles.core.model

import org.futo.circles.core.list.IdEntity
import org.futo.circles.core.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.room.send.SendState

data class Post(
    val postInfo: PostInfo,
    val content: PostContent,
    val sendState: SendState,
    val readByCount: Int,
    val repliesCount: Int
) : IdEntity<String> {
    override val id: String get() = postInfo.id
    fun isMyPost(): Boolean =
        postInfo.sender.userId == MatrixSessionProvider.currentSession?.myUserId

    fun canShare(): Boolean = content.type != PostContentType.POLL_CONTENT
}