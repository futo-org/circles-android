package com.futo.circles.feature.circles.accept_invite.list

import android.view.ViewGroup
import com.futo.circles.core.list.BaseRvAdapter
import com.futo.circles.model.RoomListItem
import com.futo.circles.model.SelectableRoomListItem

class CirclesInviteAdapter(
    private val onCircleSelected: (SelectableRoomListItem) -> Unit
) : BaseRvAdapter<SelectableRoomListItem, CirclesInviteViewHolder>(DefaultIdEntityCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CirclesInviteViewHolder = CirclesInviteViewHolder(
        parent,
        onCircleClicked = { position -> onCircleSelected(getItem(position)) })


    override fun onBindViewHolder(holder: CirclesInviteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}