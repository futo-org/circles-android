package org.futo.circles.auth.feature.token

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest.Companion.MIN_PERIODIC_INTERVAL_MILLIS
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.futo.circles.core.feature.ErrorLogger
import org.matrix.android.sdk.api.auth.data.sessionId
import org.matrix.android.sdk.api.session.Session
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToLong

class RefreshTokenManager @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun scheduleTokenRefreshIfNeeded(session: Session) {
        val credentials = session.sessionParams.credentials
        val expireTime = ((credentials.expiresInMs ?: 0) * 0.7).roundToLong()
        if (credentials.refreshToken.isNullOrEmpty()) return
        if (expireTime < MIN_PERIODIC_INTERVAL_MILLIS) return

        val sessionIdData = Data.Builder()
            .putString(RefreshTokenWorker.SESSION_ID_PARAM_KEY, credentials.sessionId())
            .build()

        val refreshRequest =
            PeriodicWorkRequestBuilder<RefreshTokenWorker>(expireTime, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .setInputData(sessionIdData)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            credentials.sessionId(),
            ExistingPeriodicWorkPolicy.KEEP,
            refreshRequest
        )
        ErrorLogger.appendLog(
            "scheduled sessionId ${credentials.sessionId()}, expireTime $expireTime"
        )
    }

    fun cancelTokenRefreshing(session: Session) {
        val credentials = session.sessionParams.credentials
        cancelTokenRefreshingById(credentials.sessionId())
    }

    fun cancelTokenRefreshingById(credentialsSessionId: String) {
        ErrorLogger.appendLog("$credentialsSessionId canceled")
        WorkManager.getInstance(context).cancelUniqueWork(credentialsSessionId)
    }

}