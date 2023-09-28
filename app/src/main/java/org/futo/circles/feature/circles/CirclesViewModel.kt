package org.futo.circles.feature.circles

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import org.futo.circles.R
import org.futo.circles.core.SingleEventLiveData
import org.futo.circles.core.extensions.Response
import org.futo.circles.core.extensions.createResult
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.model.LoadingData
import org.futo.circles.core.provider.MatrixSessionProvider
import org.futo.circles.core.room.CreateRoomDataSource
import org.futo.circles.core.room.invite.InviteRequestsDataSource
import org.futo.circles.core.utils.getTimelineRoomFor
import org.futo.circles.model.RequestCircleListItem
import org.matrix.android.sdk.api.session.getRoomSummary
import javax.inject.Inject

@HiltViewModel
class CirclesViewModel @Inject constructor(
    dataSource: CirclesDataSource,
    private val inviteRequestsDataSource: InviteRequestsDataSource,
    private val createRoomDataSource: CreateRoomDataSource
) : ViewModel() {

    val roomsLiveData = dataSource.getCirclesFlow().asLiveData()
    val inviteResultLiveData = SingleEventLiveData<Response<Unit?>>()
    val navigateToCircleLiveData = SingleEventLiveData<Response<String>>()
    val createTimelineLoadingLiveData = MutableLiveData<LoadingData>()

    fun rejectInvite(roomId: String) {
        launchBg {
            val result = inviteRequestsDataSource.rejectInvite(roomId)
            inviteResultLiveData.postValue(result)
        }
    }

    fun inviteUser(room: RequestCircleListItem) {
        launchBg {
            val result = inviteRequestsDataSource.inviteUser(room.id, room.requesterId)
            inviteResultLiveData.postValue(result)
        }
    }

    fun kickUser(room: RequestCircleListItem) {
        launchBg { inviteRequestsDataSource.kickUser(room.id, room.requesterId) }
    }

    fun createTimeLineIfNotExist(circleId: String) {
        launchBg {
            val result = createResult {
                if (getTimelineRoomFor(circleId) == null) {
                    createTimelineLoadingLiveData.postValue(LoadingData(R.string.creating_timeline))
                    val name =
                        MatrixSessionProvider.getSessionOrThrow().getRoomSummary(circleId)?.name
                    createRoomDataSource.createCircleTimeline(circleId, name)
                    delay(1000L)
                }
                circleId
            }
            createTimelineLoadingLiveData.postValue(LoadingData(isLoading = false))
            navigateToCircleLiveData.postValue(result)
        }
    }

}