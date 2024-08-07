package org.futo.circles.core.feature.notifications.test.task

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.futo.circles.core.R
import org.futo.circles.core.feature.notifications.PushersManager
import org.futo.circles.core.model.TaskStatus
import javax.inject.Inject

class NotificationsUnifiedPushEndpointTest @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pushersManager: PushersManager
) : BaseNotificationTest(R.string.settings_troubleshoot_test_current_endpoint_title) {

    override fun perform() {
        val endpoint = pushersManager.getPrivacyFriendlyUpEndpoint()
        if (endpoint != null) {
            description = context.getString(
                R.string.settings_troubleshoot_test_current_endpoint_success,
                endpoint
            )
            status = TaskStatus.SUCCESS
        } else {
            description =
                context.getString(R.string.settings_troubleshoot_test_current_endpoint_failed)
            status = TaskStatus.FAILED
        }
    }
}
