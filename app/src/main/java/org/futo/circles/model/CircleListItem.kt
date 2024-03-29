package org.futo.circles.model

import org.futo.circles.R
import org.futo.circles.core.base.list.IdEntity
import org.futo.circles.core.model.RoomInfo
import org.matrix.android.sdk.api.session.room.model.Membership

sealed class CircleListItem : IdEntity<String>
data class CirclesHeaderItem(
    val titleRes: Int
) : CircleListItem() {
    override val id: String = titleRes.toString()

    companion object {
        val invitesCirclesHeader = CirclesHeaderItem(R.string.invites)
        val sharedCirclesHeader = CirclesHeaderItem(R.string.shared_circles)
        val privateCirclesHeader = CirclesHeaderItem(R.string.private_circles)
    }
}

sealed class CircleRoomListItem(
    override val id: String,
    open val info: RoomInfo,
    open val membership: Membership
) : CircleListItem()

data class JoinedCircleListItem(
    override val id: String,
    override val info: RoomInfo,
    val isShared: Boolean,
    val followingCount: Int,
    val followedByCount: Int,
    val unreadCount: Int,
    val knockRequestsCount: Int
) : CircleRoomListItem(id, info, Membership.JOIN)

data class InvitedCircleListItem(
    override val id: String,
    override val info: RoomInfo,
    val inviterName: String,
    val shouldBlurIcon: Boolean
) : CircleRoomListItem(id, info, Membership.INVITE)

