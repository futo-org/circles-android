package com.futo.circles.feature.groups

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import by.kirich1409.viewbindingdelegate.viewBinding
import com.futo.circles.R
import com.futo.circles.databinding.GroupsFragmentBinding
import com.futo.circles.extensions.bindToFab
import com.futo.circles.extensions.observeData
import com.futo.circles.feature.groups.list.GroupsListAdapter
import com.futo.circles.model.GroupListItem
import org.koin.androidx.viewmodel.ext.android.viewModel


class GroupsFragment : Fragment(R.layout.groups_fragment) {

    private val viewModel by viewModel<GroupsViewModel>()
    private val binding by viewBinding(GroupsFragmentBinding::bind)
    private val listAdapter by lazy { GroupsListAdapter(::onGroupListItemClicked) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        viewModel.groupsLiveData?.observeData(this, ::setGroupsList)
    }

    private fun setupViews() {
        binding.rvGroups.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = listAdapter
            bindToFab(binding.fbAddGroup)
        }

        binding.fbAddGroup.setOnClickListener { navigateToCreateGroup() }
    }

    private fun setGroupsList(list: List<GroupListItem>) {
        listAdapter.submitList(list)
    }

    private fun onGroupListItemClicked(room: GroupListItem) {
        findNavController().navigate(GroupsFragmentDirections.toGroupTimeLineNavGraph(room.id))
    }

    private fun navigateToCreateGroup() {
        findNavController().navigate(GroupsFragmentDirections.toCreateRoomDialogFragment())
    }
}