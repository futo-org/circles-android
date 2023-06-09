package org.futo.circles.feature.people.user

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.futo.circles.R
import org.futo.circles.core.utils.getSharedCircleFor
import org.futo.circles.extensions.getRoomOwners
import org.futo.circles.mapping.toTimelineRoomListItem
import org.futo.circles.model.TIMELINE_TYPE
import org.futo.circles.model.TimelineHeaderItem
import org.futo.circles.model.TimelineListItem
import org.futo.circles.model.TimelineRoomListItem
import org.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getUserOrDefault
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams

class UserDataSource(
    context: Context,
    private val userId: String
) {

    private val session by lazy {
        MatrixSessionProvider.currentSession ?: throw IllegalArgumentException(
            context.getString(R.string.session_is_not_created)
        )
    }

    val userLiveData = session.userService().getUserLive(userId).map {
        it.getOrNull() ?: session.getUserOrDefault(userId)
    }

    suspend fun getTimelinesFlow() = combine(
        getAllFollowingTimelinesFlow(),
        getAllSharedTimelinesFlow()
    ) { followingTimelines, sharedTimelines ->
        buildList(followingTimelines, sharedTimelines)
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    private fun buildList(
        followingTimelines: List<TimelineRoomListItem>,
        sharedTimelines: List<TimelineRoomListItem>
    ): List<TimelineListItem> = mutableListOf<TimelineListItem>().apply {
        val allItems = mutableListOf<TimelineRoomListItem>().apply {
            addAll(followingTimelines)
            addAll(sharedTimelines)
        }.distinctBy { it.id }

        val following = allItems.filter { it.isJoined }
        if (following.isNotEmpty()) {
            add(TimelineHeaderItem.followingHeader)
            addAll(following)
        }
        val others = allItems - following.toSet()
        if (others.isNotEmpty()) {
            add(TimelineHeaderItem.othersHeader)
            addAll(others)
        }
    }

    private fun getAllFollowingTimelinesFlow() = session.roomService()
        .getRoomSummariesLive(roomSummaryQueryParams())
        .map { list -> filterUsersTimelines(list) }.asFlow()

    private suspend fun getAllSharedTimelinesFlow() = session.roomService().getRoomSummaryLive(
        getSharedCircleFor(userId)?.roomId ?: ""
    ).asFlow().map { sharedSummary ->
        sharedSummary.getOrNull()?.let { mapSharedTimelines(it) } ?: emptyList()
    }

    private suspend fun mapSharedTimelines(sharedSummary: RoomSummary): List<TimelineRoomListItem> =
        session.spaceService().querySpaceChildren(sharedSummary.roomId).children.map {
            it.toTimelineRoomListItem()
        }

    private fun filterUsersTimelines(list: List<RoomSummary>): List<TimelineRoomListItem> {
        return list.mapNotNull { summary ->
            if (isUsersCircleTimeline(summary)) summary.toTimelineRoomListItem()
            else null
        }
    }

    private fun isUsersCircleTimeline(summary: RoomSummary) =
        summary.roomType == TIMELINE_TYPE && summary.membership == Membership.JOIN &&
                getRoomOwners(summary.roomId).map { it.userId }.contains(userId)

}