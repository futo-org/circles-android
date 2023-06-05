package org.futo.circles.core.room.update

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.extensions.onBackPressed
import org.futo.circles.core.extensions.showSuccess
import org.futo.circles.core.fragment.BaseFullscreenDialogFragment
import org.futo.circles.core.fragment.HasLoadingState
import org.futo.circles.core.picker.DeviceMediaPickerHelper
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.matrix.android.sdk.api.session.room.model.RoomSummary

abstract class UpdateRoomDialogFragment(inflate: (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding) :
    BaseFullscreenDialogFragment(inflate), HasLoadingState {

    abstract val roomId: String
    private val viewModel by viewModel<UpdateRoomViewModel> { parametersOf(roomId) }
    abstract val mediaPickerHelper: DeviceMediaPickerHelper
    abstract val successMessageResId: Int
    abstract fun onCoverImageSelected(uri: Uri)
    abstract fun setInitialGroupData(room: RoomSummary)
    abstract fun setUpdateButtonEnabled(isEnabled: Boolean)

    protected fun changeCoverImage() {
        mediaPickerHelper.showMediaPickerDialog(onImageSelected = { _, uri ->
            viewModel.setImageUri(uri)
        })
    }

    protected fun onInputRoomDataChanged(
        name: String,
        topic: String? = null,
        isPublic: Boolean = false
    ) {
        viewModel.handleRoomDataUpdate(name, topic ?: "", isPublic)
    }

    protected fun updateRoom(
        name: String,
        topic: String? = null,
        isPublic: Boolean = false
    ) {
        viewModel.update(name, topic ?: "", isPublic)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.selectedImageLiveData.observeData(this) {
            onCoverImageSelected(it)
        }
        viewModel.updateGroupResponseLiveData.observeResponse(this,
            success = {
                showSuccess(getString(successMessageResId), true)
                onBackPressed()
            }
        )
        viewModel.groupSummaryLiveData.observeData(this) {
            it?.let { setInitialGroupData(it) }
        }
        viewModel.isRoomDataChangedLiveData.observeData(this) {
            setUpdateButtonEnabled(it)
        }
    }
}