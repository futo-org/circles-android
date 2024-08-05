package org.futo.circles.core.feature.room.invite

import dagger.hilt.android.scopes.ViewModelScoped
import org.futo.circles.core.extensions.createResult
import org.futo.circles.core.feature.room.RoomRelationsBuilder
import org.futo.circles.core.model.Gallery
import org.futo.circles.core.model.Group
import org.futo.circles.core.model.RoomRequestTypeArg
import org.futo.circles.core.model.Timeline
import org.futo.circles.core.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getRoom
import javax.inject.Inject

@ViewModelScoped
class ManageInviteRequestsDataSource @Inject constructor(
    private val roomRelationsBuilder: RoomRelationsBuilder
) {

    suspend fun inviteUser(roomId: String, userId: String) = createResult {
        MatrixSessionProvider.currentSession?.getRoom(roomId)?.membershipService()
            ?.invite(userId, null)
    }

    suspend fun acceptInvite(roomId: String, requestTypeArg: RoomRequestTypeArg) = createResult {
        MatrixSessionProvider.currentSession?.roomService()?.joinRoom(roomId)
        val circleRoomForRelations = when (requestTypeArg) {
            RoomRequestTypeArg.Circle -> Timeline()
            RoomRequestTypeArg.Group -> Group()
            RoomRequestTypeArg.Photo -> Gallery()
            RoomRequestTypeArg.DM -> null // no additional relations for dms
        }

        circleRoomForRelations?.let {
            roomRelationsBuilder.setInvitedRoomRelations(roomId, it)
        }
    }

    suspend fun rejectInvite(roomId: String) = createResult {
        MatrixSessionProvider.currentSession?.roomService()?.leaveRoom(roomId)
    }

    suspend fun kickUser(roomId: String, userId: String) = createResult {
        MatrixSessionProvider.currentSession?.getRoom(roomId)?.membershipService()?.remove(userId)
    }

}