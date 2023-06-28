package org.futo.circles.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.model.CIRCLE_TAG
import org.futo.circles.core.model.GROUP_TYPE
import org.futo.circles.core.model.SharedCirclesSpace
import org.futo.circles.core.provider.MatrixSessionProvider
import org.futo.circles.core.room.CreateRoomDataSource
import org.futo.circles.core.utils.getSharedCirclesSpaceId
import org.futo.circles.feature.notifications.PushersManager
import org.futo.circles.feature.notifications.ShortcutsHandler
import org.futo.circles.gallery.feature.backup.RoomAccountDataSource
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.model.RoomType
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pushersManager: PushersManager,
    private val createRoomDataSource: CreateRoomDataSource,
    roomAccountDataSource: RoomAccountDataSource,
    shortcutsHandler: ShortcutsHandler
) : ViewModel() {

    val mediaBackupSettingsLiveData = roomAccountDataSource.getMediaBackupSettingsLive()
    val inviteIntoSharedSpaceLiveData = MatrixSessionProvider.currentSession?.roomService()
        ?.getRoomSummariesLive(roomSummaryQueryParams {
            excludeType = null
            memberships = listOf(Membership.INVITE)
        })?.map { it.filter { it.roomType == RoomType.SPACE }.map { it.roomId } }

    init {
        shortcutsHandler.observeRoomsAndBuildShortcuts(viewModelScope)
        createSharedCirclesSpaceIfNotExist()
    }

    private fun createSharedCirclesSpaceIfNotExist() {
        if (getSharedCirclesSpaceId() != null) return
        launchBg { createRoomDataSource.createRoom(SharedCirclesSpace()) }
    }

    fun registerPushNotifications() {
        pushersManager.registerPushNotifications()
    }

    fun getNotificationGroupOrCircleId(summary: RoomSummary): String? {
        if (summary.membership != Membership.JOIN) return null
        return if (summary.roomType == GROUP_TYPE) summary.roomId
        else getParentSpaceIdForRoom(summary)
    }


    private fun getParentSpaceIdForRoom(summary: RoomSummary): String? {
        val circles = MatrixSessionProvider.currentSession?.roomService()
            ?.getRoomSummaries(roomSummaryQueryParams { excludeType = null })
            ?.filter { item -> item.hasTag(CIRCLE_TAG) } ?: emptyList()

        val parentCircle =
            circles.firstOrNull { it.spaceChildren?.firstOrNull { it.childRoomId == summary.roomId } != null }

        return parentCircle?.roomId
    }

    fun autoAcceptInviteOnKnock(roomIds: List<String>) {
        MatrixSessionProvider.currentSession?.let { session ->
            roomIds.forEach { launchBg { session.roomService().joinRoom(it) } }
        }
    }
}