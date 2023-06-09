package org.futo.circles.feature.notifications.test.task

import android.content.Context
import org.futo.circles.R
import org.futo.circles.feature.notifications.PushersManager
import org.futo.circles.model.NotificationTestStatus

class NotificationsUnifiedPushGatewayTest(
    private val context: Context,
    private val pushersManager: PushersManager
) : BaseNotificationTest(R.string.settings_troubleshoot_test_current_gateway_title) {

    override fun perform() {
        description = context.getString(
            R.string.settings_troubleshoot_test_current_gateway,
            pushersManager.getPushGateway()
        )
        status = NotificationTestStatus.SUCCESS
    }
}