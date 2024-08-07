package org.futo.circles.core.feature.room.well_known

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.R
import org.futo.circles.core.base.fragment.BaseFullscreenDialogFragment
import org.futo.circles.core.databinding.DialogFragmentRoomWellKnownBinding
import org.futo.circles.core.extensions.getText
import org.futo.circles.core.extensions.gone
import org.futo.circles.core.extensions.loadRoomProfileIcon
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.extensions.onBackPressed
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.extensions.showError
import org.futo.circles.core.extensions.showSuccess
import org.futo.circles.core.model.RoomPublicInfo
import org.futo.circles.core.model.ShareUrlTypeArg
import org.matrix.android.sdk.api.session.room.model.Membership

@AndroidEntryPoint
class RoomWellKnownDialogFragment :
    BaseFullscreenDialogFragment<DialogFragmentRoomWellKnownBinding>(
        DialogFragmentRoomWellKnownBinding::inflate
    ) {

    private val viewModel by viewModels<RoomWellKnownViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.btnRequest.setOnClickListener {
            viewModel.sendKnockRequest(
                binding.tilRequestMessage.getText().takeIf { it.isNotEmpty() })
            binding.btnRequest.setIsLoading(true)
        }
    }

    private fun setupObservers() {
        viewModel.roomPublicInfoLiveData.observeResponse(this,
            success = { roomInfo -> bindRoomData(roomInfo) },
            onRequestInvoked = { binding.vLoading.gone() },
            error = { bindError(it) }
        )
        viewModel.knockRequestLiveData.observeResponse(this,
            success = {
                showSuccess(getString(R.string.request_sent))
                onBackPressed()
            },
            error = {
                showError(getString(R.string.failed_to_request_invitation))
            },
            onRequestInvoked = { binding.btnRequest.setIsLoading(false) })

        viewModel.parseErrorEventLiveData.observeData(this) {
            binding.vLoading.gone()
            bindError(getString(R.string.unable_to_parse_url))
        }
    }

    private fun bindError(message: String) {
        binding.ivCover.apply {
            setImageResource(R.drawable.ic_error)
            setColorFilter(ContextCompat.getColor(requireContext(), R.color.red))
        }
        binding.tvRoomName.text = message
    }


    private fun bindRoomData(roomInfo: RoomPublicInfo) {
        with(binding) {
            roomInfo.avatarUrl?.let {
                ivCover.loadRoomProfileIcon(
                    roomInfo.avatarUrl,
                    roomInfo.name ?: ""
                )
            }
            tvRoomName.apply {
                setIsVisible(roomInfo.name != null)
                text = roomInfo.name
            }
            btnRequest.setIsVisible(shouldShowKnockButton(roomInfo.membership))
            tilRequestMessage.setIsVisible(shouldShowKnockButton(roomInfo.membership))
            binding.tvRoomId.text = roomInfo.id
            tvMembersCount.apply {
                setIsVisible(roomInfo.memberCount > 0)
                text = getString(R.string.joined_members_count, roomInfo.memberCount)
            }
            btnRequest.setText(getString(R.string.request_to_join))
            tvTopic.apply {
                setIsVisible(roomInfo.topic?.isNotEmpty() == true)
                text = getString(R.string.topic_format, roomInfo.topic ?: "")
            }
            tvType.apply {
                setIsVisible(roomInfo.type != ShareUrlTypeArg.ROOM)
                text = getString(R.string.room_type_format, roomInfo.type.typeKey)
            }
            tvMembersip.text = getString(
                when (roomInfo.membership) {
                    Membership.NONE,
                    Membership.KNOCK,
                    Membership.LEAVE -> R.string.request_to_become_member_room

                    Membership.INVITE -> R.string.you_have_pending_invitation_room

                    Membership.JOIN -> R.string.you_are_already_member_room

                    Membership.BAN -> R.string.you_are_banned_room
                }
            )
        }
    }

    private fun shouldShowKnockButton(membership: Membership): Boolean =
        membership == Membership.NONE || membership == Membership.KNOCK || membership == Membership.LEAVE


}