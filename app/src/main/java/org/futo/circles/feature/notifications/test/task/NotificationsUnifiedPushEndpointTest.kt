package org.futo.circles.feature.notifications.test.task

import android.content.Context
import org.futo.circles.R
import org.futo.circles.feature.notifications.PushersManager
import org.futo.circles.model.NotificationTestStatus

class NotificationsUnifiedPushEndpointTest(
    private val context: Context,
    private val pushersManager: PushersManager
) : BaseNotificationTest(R.string.settings_troubleshoot_test_current_endpoint_title) {

    override fun perform() {
        val endpoint = pushersManager.getPrivacyFriendlyUpEndpoint()
        if (endpoint != null) {
            description = context.getString(
                R.string.settings_troubleshoot_test_current_endpoint_success,
                endpoint
            )
            status = NotificationTestStatus.SUCCESS
        } else {
            description =
                context.getString(R.string.settings_troubleshoot_test_current_endpoint_failed)
            status = NotificationTestStatus.FAILED
        }
    }
}
