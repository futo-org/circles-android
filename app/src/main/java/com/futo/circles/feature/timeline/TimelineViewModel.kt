package com.futo.circles.feature.timeline

import android.net.Uri
import androidx.lifecycle.asLiveData
import com.futo.circles.core.SingleEventLiveData
import com.futo.circles.extensions.Response
import com.futo.circles.extensions.launchBg
import com.futo.circles.feature.room.LeaveRoomDataSource
import com.futo.circles.feature.timeline.post.share.ShareableContent
import com.futo.circles.feature.timeline.data_source.TimelineDataSource
import com.futo.circles.feature.timeline.post.PostOptionsDataSource
import com.futo.circles.model.ImageContent
import com.futo.circles.model.PostContent
import org.matrix.android.sdk.api.util.Cancelable

class TimelineViewModel(
    private val timelineDataSource: TimelineDataSource,
    private val leaveRoomDataSource: LeaveRoomDataSource,
    private val postOptionsDataSource: PostOptionsDataSource
) : BaseTimelineViewModel(timelineDataSource) {

    val timelineEventsLiveData = timelineDataSource.timelineEventsLiveData
    val accessLevelLiveData = timelineDataSource.accessLevelFlow.asLiveData()
    val scrollToTopLiveData = SingleEventLiveData<Unit>()
    val shareLiveData = SingleEventLiveData<ShareableContent>()
    val downloadImageLiveData = SingleEventLiveData<Unit>()
    val ignoreUserLiveData = SingleEventLiveData<Response<Unit?>>()
    val unSendReactionLiveData = SingleEventLiveData<Response<Cancelable?>>()
    val leaveGroupLiveData = SingleEventLiveData<Response<Unit?>>()
    val deleteCircleLiveData = SingleEventLiveData<Response<Unit?>>()


    fun toggleRepliesVisibilityFor(eventId: String) {
        timelineDataSource.toggleRepliesVisibility(eventId)
    }

    fun sharePostContent(content: PostContent) {
        launchBg {
            shareLiveData.postValue(postOptionsDataSource.getShareableContent(content))
        }
    }

    fun removeMessage(roomId: String, eventId: String) {
        postOptionsDataSource.removeMessage(roomId, eventId)
    }

    fun ignoreSender(senderId: String) {
        launchBg {
            ignoreUserLiveData.postValue(postOptionsDataSource.ignoreSender(senderId))
        }
    }

    fun saveToDevice(imageContent: ImageContent) {
        launchBg {
            postOptionsDataSource.saveImageToDevice(imageContent)
            downloadImageLiveData.postValue(Unit)
        }
    }

    fun sendTextPost(roomId: String, message: String, threadEventId: String?) {
        timelineDataSource.sendTextMessage(roomId, message, threadEventId)
        if (threadEventId == null) scrollToTopLiveData.postValue(Unit)
    }

    fun sendImagePost(roomId: String, uri: Uri, threadEventId: String?) {
        timelineDataSource.sendImage(roomId, uri, threadEventId)
        if (threadEventId == null) scrollToTopLiveData.postValue(Unit)
    }

    fun sendReaction(roomId: String, eventId: String, emoji: String) {
        postOptionsDataSource.sendReaction(roomId, eventId, emoji)
    }

    fun unSendReaction(roomId: String, eventId: String, emoji: String) {
        launchBg {
            val result = postOptionsDataSource.unSendReaction(roomId, eventId, emoji)
            unSendReactionLiveData.postValue(result)
        }
    }

    fun leaveGroup() {
        launchBg { leaveGroupLiveData.postValue(leaveRoomDataSource.leaveGroup()) }
    }

    fun deleteCircle() {
        launchBg { deleteCircleLiveData.postValue(leaveRoomDataSource.deleteCircle()) }
    }

    fun isSingleOwner(): Boolean = leaveRoomDataSource.isUserSingleRoomOwner()

}