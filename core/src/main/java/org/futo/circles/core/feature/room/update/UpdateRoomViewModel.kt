package org.futo.circles.core.feature.room.update

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.futo.circles.core.base.SingleEventLiveData
import org.futo.circles.core.extensions.Response
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.model.AccessLevel
import org.futo.circles.core.model.CircleRoomTypeArg
import javax.inject.Inject

@HiltViewModel
class UpdateRoomViewModel @Inject constructor(
    private val dataSource: UpdateRoomDataSource
) : ViewModel() {

    val selectedImageLiveData = MutableLiveData<Uri>()
    val updateRoomResponseLiveData = SingleEventLiveData<Response<Unit?>>()
    val roomSummaryLiveData = MutableLiveData(dataSource.getRoomSummary())
    val roomPowerLevelLiveData = dataSource.getRoomPowerLevelFlow().asLiveData()
    val isRoomDataChangedLiveData = MutableLiveData(false)

    fun setImageUri(uri: Uri) {
        selectedImageLiveData.value = uri
    }

    fun update(
        name: String,
        topic: String,
        userAccessLevel: AccessLevel,
        roomTypeArg: CircleRoomTypeArg
    ) {
        launchBg {
            val result = dataSource.updateRoom(
                name,
                topic,
                selectedImageLiveData.value,
                userAccessLevel.takeIf { isDefaultUserRoleChanged(userAccessLevel) },
                roomTypeArg
            )
            updateRoomResponseLiveData.postValue(result)
        }
    }

    fun handleRoomDataUpdate(
        name: String,
        topic: String,
        userAccessLevel: AccessLevel
    ) {
        val isDataUpdated = dataSource.isNameChanged(name) ||
                dataSource.isTopicChanged(topic) ||
                isDefaultUserRoleChanged(userAccessLevel) ||
                selectedImageLiveData.value != null

        isRoomDataChangedLiveData.postValue(isDataUpdated)
    }

    private fun isDefaultUserRoleChanged(userAccessLevel: AccessLevel): Boolean {
        val initialRole = roomPowerLevelLiveData.value?.usersDefault ?: AccessLevel.User.levelValue
        return initialRole != userAccessLevel.levelValue
    }
}