package org.futo.circles.feature.circles.accept_invite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.R
import org.futo.circles.core.base.fragment.BaseFullscreenDialogFragment
import org.futo.circles.core.base.fragment.HasLoadingState
import org.futo.circles.core.extensions.navigateSafe
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.extensions.onBackPressed
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.feature.room.select.SelectRoomsFragment
import org.futo.circles.core.feature.room.select.interfaces.RoomsListener
import org.futo.circles.core.feature.room.select.interfaces.SelectRoomsListener
import org.futo.circles.core.model.SelectRoomTypeArg
import org.futo.circles.core.model.SelectableRoomListItem
import org.futo.circles.databinding.DialogFragmentAcceptCircleInviteBinding

@AndroidEntryPoint
class AcceptCircleInviteDialogFragment :
    BaseFullscreenDialogFragment<DialogFragmentAcceptCircleInviteBinding>(
        DialogFragmentAcceptCircleInviteBinding::inflate
    ), HasLoadingState, SelectRoomsListener, RoomsListener {

    override val fragment: Fragment = this
    private val viewModel by viewModels<AcceptCircleInviteViewModel>()

    private val selectRoomsFragment by lazy { SelectRoomsFragment.create(SelectRoomTypeArg.CirclesJoined) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) addSelectCirclesFragment()
        setupViews()
        setupObservers()
    }

    private fun addSelectCirclesFragment() {
        childFragmentManager.beginTransaction()
            .replace(R.id.lContainer, selectRoomsFragment)
            .commitAllowingStateLoss()
    }

    private fun setupViews() {
        with(binding) {
            btnInvite.setOnClickListener {
                viewModel.acceptInvite(selectRoomsFragment.getSelectedRooms())
                startLoading(btnInvite)
            }
            fbAddRoom.setOnClickListener { navigateToCreateCircle() }
            btnCreateCircle.setOnClickListener { navigateToCreateCircle() }
        }
    }

    private fun setupObservers() {
        viewModel.acceptResultLiveData.observeResponse(this,
            success = { onBackPressed() }
        )
    }

    private fun navigateToCreateCircle() {
        findNavController()
            .navigateSafe(AcceptCircleInviteDialogFragmentDirections.toCreateCircleDialogFragment())
    }

    override fun onRoomsSelected(rooms: List<SelectableRoomListItem>) {
        binding.btnInvite.isEnabled = rooms.isNotEmpty()
    }

    override fun onRoomsListChanged(rooms: List<SelectableRoomListItem>) {
        binding.createCircleGroup.setIsVisible(rooms.isEmpty())
        binding.selectCircleGroup.setIsVisible(rooms.isNotEmpty())
    }
}