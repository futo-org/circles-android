package org.futo.circles.feature.circles.list

import android.view.ViewGroup
import org.futo.circles.core.base.list.BaseRvAdapter
import org.futo.circles.model.CircleInvitesNotificationListItem
import org.futo.circles.model.CircleListItem
import org.futo.circles.model.CircleListItemPayload
import org.futo.circles.model.CirclesHeaderItem
import org.futo.circles.model.JoinedCircleListItem

enum class CirclesListItemViewType { JoinedCircle, InviteNotification, Header }

class CirclesListAdapter(
    private val onRoomClicked: (CircleListItem) -> Unit,
    private val onOpenInvitesClicked: () -> Unit
) : BaseRvAdapter<CircleListItem, CirclesViewHolder>(PayloadIdEntityCallback { old, new ->
    if (new is JoinedCircleListItem && old is JoinedCircleListItem) {
        CircleListItemPayload(
            followersCount = new.followingCount.takeIf { it != old.followingCount },
            followedByCount = new.followedByCount.takeIf { it != old.followedByCount },
            unreadCount = new.unreadCount.takeIf { it != old.unreadCount },
            knocksCount = new.knockRequestsCount.takeIf { it != old.knockRequestsCount },
            needUpdateFullItem = new.info.title != old.info.title || new.info.avatarUrl != old.info.avatarUrl
        )
    } else null
}) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is JoinedCircleListItem -> CirclesListItemViewType.JoinedCircle.ordinal
        is CircleInvitesNotificationListItem -> CirclesListItemViewType.InviteNotification.ordinal
        is CirclesHeaderItem -> CirclesListItemViewType.Header.ordinal
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (CirclesListItemViewType.entries[viewType]) {
        CirclesListItemViewType.JoinedCircle -> JoinedCircleViewHolder(
            parent = parent,
            onCircleClicked = { position -> onRoomClicked(getItem(position)) }
        )

        CirclesListItemViewType.InviteNotification -> CircleInviteNotificationViewHolder(
            parent = parent,
            onClicked = { onOpenInvitesClicked() }
        )

        CirclesListItemViewType.Header -> CircleHeaderViewHolder(parent = parent)
    }

    override fun onBindViewHolder(holder: CirclesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: CirclesViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach { payload ->
                if (payload is CircleListItemPayload && holder is JoinedCircleViewHolder) {
                    if (payload.needUpdateFullItem) holder.bind(getItem(position))
                    else holder.bindPayload(payload)
                }
            }
        }
    }
}