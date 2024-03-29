package org.futo.circles.feature.circles

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.R
import org.futo.circles.auth.explanation.CirclesExplanationDialog
import org.futo.circles.core.base.NetworkObserver
import org.futo.circles.core.databinding.FragmentRoomsBinding
import org.futo.circles.core.extensions.navigateSafe
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.extensions.setEnabledViews
import org.futo.circles.core.extensions.showNoInternetConnection
import org.futo.circles.core.model.CircleRoomTypeArg
import org.futo.circles.core.provider.PreferencesProvider
import org.futo.circles.core.view.EmptyTabPlaceholderView
import org.futo.circles.core.view.LoadingDialog
import org.futo.circles.feature.circles.list.CirclesListAdapter
import org.futo.circles.model.CircleListItem

@AndroidEntryPoint
class CirclesFragment : Fragment(org.futo.circles.core.R.layout.fragment_rooms), MenuProvider {

    private val viewModel by viewModels<CirclesViewModel>()
    private val binding by viewBinding(FragmentRoomsBinding::bind)
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }
    private val preferencesProvider by lazy { PreferencesProvider(requireContext()) }
    private var listAdapter: CirclesListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
        activity?.addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (preferencesProvider.shouldShowExplanation(CircleRoomTypeArg.Circle))
            CirclesExplanationDialog(requireContext(), CircleRoomTypeArg.Circle).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        (menu as? MenuBuilder)?.setOptionalIconsVisible(true)
        menu.clear()
        inflater.inflate(R.menu.circles_tab_menu, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help -> CirclesExplanationDialog(requireContext(), CircleRoomTypeArg.Circle).show()
        }
        return true
    }

    private fun setupViews() {
        binding.rvRooms.apply {
            setEmptyView(EmptyTabPlaceholderView(requireContext()).apply {
                setText(getString(R.string.circles_empty_message))
            })
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = CirclesListAdapter(
                onRoomClicked = { roomListItem -> onRoomListItemClicked(roomListItem) },
                onInviteClicked = { roomListItem, isAccepted ->
                    onInviteClicked(roomListItem, isAccepted)
                },
                onUnblurProfileIconClicked = { roomListItem ->
                    viewModel.unblurProfileIcon(roomListItem)
                }
            ).also { listAdapter = it }
            bindToFab(binding.fbAddRoom)
        }
        binding.fbAddRoom.setOnClickListener { navigateToCreateRoom() }
    }

    private fun setupObservers() {
        NetworkObserver.observe(this) { setEnabledViews(it, listOf(binding.rvRooms)) }
        viewModel.roomsLiveData.observeData(this) {
            listAdapter?.submitList(it)
            binding.rvRooms.notifyItemsChanged()
        }
        viewModel.inviteResultLiveData.observeResponse(this)
        viewModel.createTimelineLoadingLiveData.observeData(this) {
            loadingDialog.handleLoading(it)
        }
        viewModel.navigateToCircleLiveData.observeResponse(this,
            success = {
                findNavController().navigateSafe(CirclesFragmentDirections.toTimeline(it))
            })
    }

    private fun onInviteClicked(room: CircleListItem, isAccepted: Boolean) {
        if (showNoInternetConnection()) return
        if (isAccepted) onAcceptInviteClicked(room)
        else viewModel.rejectInvite(room.id)
    }

    private fun onRoomListItemClicked(room: CircleListItem) {
        viewModel.createTimeLineIfNotExist(room.id)
    }

    private fun navigateToCreateRoom() {
        findNavController().navigateSafe(CirclesFragmentDirections.toCreateCircleDialogFragment())
    }

    private fun onAcceptInviteClicked(room: CircleListItem) {
        findNavController().navigateSafe(
            CirclesFragmentDirections.toAcceptCircleInviteDialogFragment(room.id)
        )
    }
}