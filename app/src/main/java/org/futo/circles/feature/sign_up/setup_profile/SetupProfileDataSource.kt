package org.futo.circles.feature.sign_up.setup_profile

import android.content.Context
import android.net.Uri
import org.futo.circles.R
import org.futo.circles.extensions.createResult
import org.futo.circles.extensions.getFilename
import org.futo.circles.provider.MatrixSessionProvider
import java.util.*

class SetupProfileDataSource(
    private val context: Context
) {

    private val session by lazy {
        MatrixSessionProvider.currentSession ?: throw IllegalArgumentException(
            context.getString(R.string.session_is_not_created)
        )
    }

    val profileLiveData = session.userService().getUserLive(session.myUserId)
    val threePidLiveData = session.profileService().getThreePidsLive(true)

    suspend fun saveProfileData(profileImageUri: Uri?, displayName: String?) = createResult {
        profileImageUri?.let { uri ->
            session.profileService().updateAvatar(session.myUserId, uri, getFileName(uri))
        }

        displayName?.let { name ->
            session.profileService().setDisplayName(session.myUserId, name)
        }
    }

    fun isNameChanged(newName: String) =
        session.userService().getUser(session.myUserId)?.displayName != newName

    private fun getFileName(uri: Uri) = uri.getFilename(context) ?: UUID.randomUUID().toString()

}