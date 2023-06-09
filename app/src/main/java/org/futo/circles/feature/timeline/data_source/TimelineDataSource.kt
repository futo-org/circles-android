package org.futo.circles.feature.timeline.data_source

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import org.futo.circles.mapping.nameOrId
import org.futo.circles.model.CircleRoomTypeArg
import org.futo.circles.model.Post
import org.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.Timeline
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.TimelineSettings

class TimelineDataSource(
    roomId: String,
    private val type: CircleRoomTypeArg,
    private val threadEventId: String?,
    private val timelineBuilder: TimelineBuilder
) : Timeline.Listener {

    private val session = MatrixSessionProvider.currentSession
    private val room = session?.getRoom(roomId)
    val roomTitleLiveData = room?.getRoomSummaryLive()?.map { it.getOrNull()?.nameOrId() }
    val timelineEventsLiveData = MutableLiveData<List<Post>>()
    private val isThread = threadEventId != null
    private val listDirection =
        if (isThread) Timeline.Direction.FORWARDS else Timeline.Direction.BACKWARDS

    private var timelines: MutableList<Timeline> = mutableListOf()

    fun startTimeline() {
        getTimelineRooms().forEach { room ->
            val timeline =
                room.timelineService().createTimeline(
                    null, TimelineSettings(
                        initialSize = MESSAGES_PER_PAGE,
                        rootThreadEventId = threadEventId
                    )
                )
                    .apply {
                        addListener(this@TimelineDataSource)
                        start(threadEventId)
                    }
            timelines.add(timeline)
        }
    }

    fun clearTimeline() {
        timelines.forEach { timeline ->
            timeline.removeAllListeners()
            timeline.dispose()
        }
        timelines.clear()
    }

    fun loadMore() {
        timelines.forEach { timeline ->
            if (timeline.hasMoreToLoad(listDirection))
                timeline.paginate(listDirection, MESSAGES_PER_PAGE)
        }
    }

    override fun onTimelineUpdated(snapshot: List<TimelineEvent>) {
        if (snapshot.isNotEmpty())
            timelineEventsLiveData.value = timelineBuilder.build(snapshot, isThread)
    }

    override fun onTimelineFailure(throwable: Throwable) {
        timelines.forEach { it.restartWithEventId(null) }
    }

    private fun getTimelineRooms(): List<Room> = when (type) {
        CircleRoomTypeArg.Circle -> room?.roomSummary()?.spaceChildren?.mapNotNull {
            session?.getRoom(it.childRoomId)
        } ?: emptyList()

        else -> listOfNotNull(room)
    }

    companion object {
        private const val MESSAGES_PER_PAGE = 30
    }
}