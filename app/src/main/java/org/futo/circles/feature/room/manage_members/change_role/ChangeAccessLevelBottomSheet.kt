package org.futo.circles.feature.room.manage_members.change_role

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.futo.circles.databinding.BottomSheetChangeAccessLevelBinding
import org.futo.circles.extensions.observeData
import org.futo.circles.feature.room.manage_members.change_role.list.ChangeAccessLevelAdapter
import org.futo.circles.model.AccessLevelListItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChangeAccessLevelBottomSheet : BottomSheetDialogFragment() {

    private var binding: BottomSheetChangeAccessLevelBinding? = null
    private val args: ChangeAccessLevelBottomSheetArgs by navArgs()
    private val viewModel by viewModel<ChangeAccessLevelViewModel> {
        parametersOf(args.levelValue, args.myUserLevelValue)
    }
    private val listAdapter by lazy { ChangeAccessLevelAdapter(::onLevelListItemClicked) }
    private var changeAccessLevelListener: ChangeAccessLevelListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        changeAccessLevelListener =
            parentFragmentManager.fragments.firstOrNull { it is ChangeAccessLevelListener } as? ChangeAccessLevelListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetChangeAccessLevelBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding?.let {
            it.rvRoles.apply {
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = listAdapter
            }
            it.btnCancel.setOnClickListener { dismiss() }
            it.btnSetLevel.setOnClickListener {
                setNewAccessLevel()
                dismiss()
            }
        }
    }

    private fun setupObservers() {
        viewModel.accessLevelsLiveData.observeData(this) { listAdapter.submitList(it) }
        viewModel.isLevelChangedLiveData.observeData(this) { binding?.btnSetLevel?.isEnabled = it }
    }

    private fun onLevelListItemClicked(item: AccessLevelListItem) {
        viewModel.toggleAccessLevel(item.role.value)
    }

    private fun setNewAccessLevel() {
        viewModel.getCurrentSelectedValue()?.let { newLevelValue ->
            changeAccessLevelListener?.onChangeAccessLevel(args.userId, newLevelValue)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}