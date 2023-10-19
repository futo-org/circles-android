package org.futo.circles.core.feature.timeline.data_source

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import org.futo.circles.core.feature.timeline.builder.MultiTimelineBuilder
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.Timeline
import javax.inject.Inject

@ViewModelScoped
class MultiTimelinesDataSource @Inject constructor(
    savedStateHandle: SavedStateHandle,
    timelineBuilder: MultiTimelineBuilder
) : BaseTimelineDataSource(savedStateHandle, timelineBuilder) {

    private var timelines: MutableList<Timeline> = mutableListOf()

    override fun startTimeline() {
        getTimelineRooms().forEach { room ->
            val timeline = createAndStartNewTimeline(room)
            timelines.add(timeline)
        }
    }

    override fun clearTimeline() {
        timelines.forEach { timeline -> closeTimeline(timeline) }
        timelines.clear()
    }

    override fun loadMore(): Boolean {
        var hasMore = false
        timelines.forEach { timeline ->
            if (loadNextPage(timeline)) hasMore = true
        }
        return hasMore
    }

    override fun onTimelineFailure(throwable: Throwable) {
        timelines.forEach { restartTimelineOnFailure(it) }
    }

    private fun getTimelineRooms(): List<Room> = room.roomSummary()?.spaceChildren?.mapNotNull {
        session.getRoom(it.childRoomId)
    } ?: emptyList()
}