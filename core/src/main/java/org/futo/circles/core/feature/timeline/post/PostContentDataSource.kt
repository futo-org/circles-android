package org.futo.circles.core.feature.timeline.post

import dagger.hilt.android.scopes.ViewModelScoped
import org.futo.circles.core.mapping.toPost
import org.futo.circles.core.model.Post
import org.futo.circles.core.model.PostContent
import org.futo.circles.core.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.room.getTimelineEvent
import javax.inject.Inject


@ViewModelScoped
class PostContentDataSource @Inject constructor() {

    private val session = MatrixSessionProvider.currentSession


    fun getPost(roomId: String, eventId: String): Post? {
        val roomForMessage = session?.getRoom(roomId)
        val timelineEvent = roomForMessage?.getTimelineEvent(eventId) ?: return null
        return timelineEvent.toPost()
    }

    fun getPostContent(roomId: String, eventId: String): PostContent? =
        getPost(roomId, eventId)?.content

}