package org.futo.circles.feature.circles.list

import android.view.ViewGroup
import org.futo.circles.core.list.BaseRvAdapter
import org.futo.circles.model.*

enum class CirclesListItemViewType { JoinedCircle, InvitedCircle, RequestCircle, Header }

class CirclesListAdapter(
    private val onRoomClicked: (CircleListItem) -> Unit,
    private val onInviteClicked: (CircleListItem, Boolean) -> Unit,
    private val onRequestClicked: (RequestCircleListItem, Boolean) -> Unit
) : BaseRvAdapter<CircleListItem, CirclesViewHolder>(PayloadIdEntityCallback { old, new ->
    if (new is JoinedCircleListItem && old is JoinedCircleListItem) {
        CircleListItemPayload(
            followersCount = new.followingCount.takeIf { it != old.followingCount },
            followedByCount = new.followedByCount.takeIf { it != old.followedByCount },
            unreadCount = new.unreadCount.takeIf { it != old.unreadCount },
            needUpdateFullItem = new.info.title != old.info.title || new.info.avatarUrl != old.info.avatarUrl
        )
    } else null
}) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is JoinedCircleListItem -> CirclesListItemViewType.JoinedCircle.ordinal
        is InvitedCircleListItem -> CirclesListItemViewType.InvitedCircle.ordinal
        is CirclesHeaderItem -> CirclesListItemViewType.Header.ordinal
        is RequestCircleListItem -> CirclesListItemViewType.RequestCircle.ordinal
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = when (CirclesListItemViewType.values()[viewType]) {
        CirclesListItemViewType.JoinedCircle -> JoinedCircleViewHolder(
            parent = parent,
            onCircleClicked = { position -> onRoomClicked(getItem(position)) }
        )
        CirclesListItemViewType.InvitedCircle -> InvitedCircleViewHolder(
            parent = parent,
            onInviteClicked = { position, isAccepted ->
                onInviteClicked(getItem(position), isAccepted)
            }
        )
        CirclesListItemViewType.Header -> CircleHeaderViewHolder(parent = parent)
        CirclesListItemViewType.RequestCircle -> RequestedCircleViewHolder(
            parent = parent,
            onRequestClicked = { position, isAccepted ->
                (getItem(position) as? RequestCircleListItem)?.let {
                    onRequestClicked(it, isAccepted)
                }
            }
        )
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