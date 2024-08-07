package org.futo.circles.core.notifications

import android.content.Context
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import org.futo.circles.core.R
import org.futo.circles.core.feature.notifications.FcmHelper
import org.futo.circles.core.feature.notifications.PushersManager
import org.futo.circles.core.provider.PreferencesProvider
import javax.inject.Inject

class GoogleFcmHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesProvider: PreferencesProvider
) : FcmHelper {

    override fun isFirebaseAvailable(): Boolean = true

    override fun getFcmToken(): String? = preferencesProvider.getFcmToken()


    override fun storeFcmToken(token: String?) {
        preferencesProvider.storeFcmToken(token)
    }

    override fun ensureFcmTokenIsRetrieved(
        pushersManager: PushersManager,
        registerPusher: Boolean
    ) {
        if (checkPlayServices(context)) {
            try {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    storeFcmToken(token)
                    if (registerPusher) pushersManager.enqueueRegisterPusherWithFcmKey(token)
                }
            } catch (ignore: Throwable) {
            }
        } else {
            Toast.makeText(context, R.string.no_valid_google_play_services_apk, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkPlayServices(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    override fun onEnterForeground() {}

    override fun onEnterBackground() {}
}
