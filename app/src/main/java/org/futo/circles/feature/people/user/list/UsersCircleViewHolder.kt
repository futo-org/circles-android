package org.futo.circles.feature.people.user.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.futo.circles.core.list.ViewBindingHolder
import org.futo.circles.databinding.ListItemAcceptCircleInviteBinding
import org.futo.circles.extensions.loadProfileIcon
import org.futo.circles.model.JoinedCircleListItem

class UsersCircleViewHolder(
    parent: ViewGroup
) : RecyclerView.ViewHolder(inflate(parent, ListItemAcceptCircleInviteBinding::inflate)) {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ListItemAcceptCircleInviteBinding


    fun bind(data: JoinedCircleListItem) {
        with(binding) {
            tvCircleName.text = data.info.title
            binding.ivCircleImage.loadProfileIcon(data.info.avatarUrl, data.info.title)
        }
    }
}