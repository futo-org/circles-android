package org.futo.circles.feature.log_in

import android.content.Context
import android.net.Uri
import org.futo.circles.BuildConfig
import org.futo.circles.R
import org.futo.circles.core.matrix.pass_phrase.restore.RestorePassPhraseDataSource
import org.futo.circles.core.matrix.room.CoreSpacesTreeBuilder
import org.futo.circles.extensions.Response
import org.futo.circles.extensions.createResult
import org.futo.circles.provider.MatrixInstanceProvider
import org.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.matrix.android.sdk.api.session.Session

class LoginDataSource(
    private val context: Context,
    private val restorePassPhraseDataSource: RestorePassPhraseDataSource,
    private val coreSpacesTreeBuilder: CoreSpacesTreeBuilder
) {

    private val homeServerConnectionConfig by lazy {
        HomeServerConnectionConfig
            .Builder()
            .withHomeServerUri(Uri.parse(BuildConfig.MATRIX_HOME_SERVER_URL))
            .build()
    }

    private val authService by lazy {
        MatrixInstanceProvider.matrix.authenticationService()
    }

    val passPhraseLoadingLiveData = restorePassPhraseDataSource.loadingLiveData

    suspend fun logIn(name: String, password: String): Response<Session> = createResult {
        val session = authService.directAuthentication(
            homeServerConnectionConfig = homeServerConnectionConfig,
            matrixId = name,
            password = password,
            initialDeviceName = context.getString(
                R.string.initial_device_name,
                context.getString(R.string.app_name)
            )
        )
        MatrixSessionProvider.awaitForSessionSync(session)
    }

    suspend fun restoreKeys(password: String) = createResult {
        restorePassPhraseDataSource.restoreKeysWithPassPhase(password)
    }

    suspend fun createSpacesTree() = createResult {
        coreSpacesTreeBuilder.createCoreSpacesTree()
    }

    suspend fun startSignUp() = createResult {
        authService.getLoginFlow(homeServerConnectionConfig)
    }

    fun isCirclesTreeCreated() = coreSpacesTreeBuilder.isCirclesHierarchyCreated()
}