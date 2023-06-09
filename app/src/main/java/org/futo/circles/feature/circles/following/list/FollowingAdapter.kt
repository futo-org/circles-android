package org.futo.circles.feature.circles.following.list

import android.view.ViewGroup
import org.futo.circles.core.list.BaseRvAdapter
import org.futo.circles.model.FollowingListItem

class FollowingAdapter(
    private val onRemoveClicked: (FollowingListItem) -> Unit
) : BaseRvAdapter<FollowingListItem, FollowingViewHolder>(DefaultIdEntityCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FollowingViewHolder = FollowingViewHolder(
        parent = parent,
        onRemoveClicked = { position -> onRemoveClicked(getItem(position)) }
    )

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}