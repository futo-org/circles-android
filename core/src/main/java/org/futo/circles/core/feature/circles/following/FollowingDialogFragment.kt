package org.futo.circles.core.feature.circles.following

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.base.fragment.BaseFullscreenDialogFragment
import org.futo.circles.core.databinding.DialogFragmentFollowingBinding
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.feature.circles.following.list.FollowingAdapter
import org.futo.circles.core.model.FollowingListItem

@AndroidEntryPoint
class FollowingDialogFragment :
    BaseFullscreenDialogFragment<DialogFragmentFollowingBinding>(DialogFragmentFollowingBinding::inflate) {

    private val viewModel by viewModels<FollowingViewModel>()
    private val listAdapter by lazy {
        FollowingAdapter(onRemoveClicked = { showRemoveOptionsDialog(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.rvRooms.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = listAdapter
        }
    }

    private fun setupObservers() {
        viewModel.roomsLiveData.observeData(this) { listAdapter.submitList(it) }
        viewModel.removeResponseLiveData.observeResponse(this)
    }

    private fun showRemoveOptionsDialog(item: FollowingListItem) {
        RemoveFollowingDialog(requireContext(), item.id, item.name,
            item.followInCirclesCount,
            object : RemoveFollowingListener {
                override fun onRemove(roomId: String) {
                    viewModel.removeRoomFromCircle(roomId)
                }

                override fun onUnfollow(roomId: String) {
                    viewModel.unfollowRoom(roomId)
                }
            }).show()
    }
}