package org.futo.circles.core.feature.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.futo.circles.core.base.SingleEventLiveData
import org.futo.circles.core.extensions.Response
import org.futo.circles.core.extensions.createResult
import org.futo.circles.core.extensions.getOrThrow
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.extensions.launchUi
import org.futo.circles.core.feature.room.RoomRelationsBuilder
import org.futo.circles.core.feature.room.invite.ManageInviteRequestsDataSource
import org.futo.circles.core.feature.workspace.SharedCircleDataSource
import org.futo.circles.core.model.TimelineListItem
import org.futo.circles.core.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getRoom
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userDataSource: UserDataSource,
    private val userOptionsDataSource: UserOptionsDataSource,
    private val roomRelationsBuilder: RoomRelationsBuilder,
    private val manageInviteRequestsDataSource: ManageInviteRequestsDataSource,
    sharedCircleDataSource: SharedCircleDataSource
) : ViewModel() {

    private val userId: String = savedStateHandle.getOrThrow("userId")
    private val profileRoomId = sharedCircleDataSource.getSharedCirclesSpaceId() ?: ""

    val userLiveData = userDataSource.userLiveData
    val timelineLiveDataLiveData = MutableLiveData<List<TimelineListItem>>()
    val requestFollowLiveData = SingleEventLiveData<Response<Unit?>>()
    val inviteToConnectLiveData = SingleEventLiveData<Response<Unit?>>()
    val ignoreUserLiveData = SingleEventLiveData<Response<Unit?>>()
    val unIgnoreUserLiveData = SingleEventLiveData<Response<Unit?>>()
    val unFollowUserLiveData = SingleEventLiveData<Response<Unit?>>()
    val isUserIgnoredLiveData = userOptionsDataSource.ignoredUsersLiveData?.map {
        it.firstOrNull { it.userId == userId } != null
    }

    init {
        getUsersTimelines()
    }

    private fun getUsersTimelines() {
        launchUi {
            userDataSource.getTimelinesFlow().collectLatest {
                timelineLiveDataLiveData.postValue(it)
            }
        }
    }

    fun requestFollowTimeline(timelineId: String) {
        launchBg {
            val result = createResult {
                MatrixSessionProvider.currentSession?.roomService()?.knock(timelineId)
            }
            requestFollowLiveData.postValue(result)
        }
    }

    fun unFollowTimeline(timelineId: String) {
        launchBg {
            createResult {
                roomRelationsBuilder.removeFromAllParents(timelineId)
                MatrixSessionProvider.currentSession?.roomService()?.leaveRoom(timelineId)
            }
        }
    }

    fun ignoreUser() {
        launchBg {
            ignoreUserLiveData.postValue(userOptionsDataSource.ignoreSender(userId))
        }
    }

    fun unIgnoreUser() {
        launchBg {
            unIgnoreUserLiveData.postValue(userOptionsDataSource.unIgnoreSender(userId))
        }
    }

    fun unFollowUser() {
        launchBg {
            unFollowUserLiveData.postValue(userOptionsDataSource.unFollowUser(userId))
        }
    }

    fun amIFollowingUser(): Boolean = userOptionsDataSource.amIFollowingUser(userId)

    fun isUserMyFollower(): Boolean {
        val mySharedCircleMembers =
            MatrixSessionProvider.currentSession?.getRoom(profileRoomId)
                ?.roomSummary()?.otherMemberIds ?: emptyList()
        return mySharedCircleMembers.contains(userId)
    }

    fun inviteToMySharedCircle() {
        launchBg {
            val result = manageInviteRequestsDataSource.inviteUser(profileRoomId, userId)
            inviteToConnectLiveData.postValue(result)
        }
    }

}