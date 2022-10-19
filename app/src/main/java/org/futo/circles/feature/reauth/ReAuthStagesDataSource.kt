package org.futo.circles.feature.reauth

import android.content.Context
import org.futo.circles.core.SingleEventLiveData
import org.futo.circles.core.auth.BaseLoginStagesDataSource
import org.futo.circles.extensions.Response
import org.futo.circles.model.CustomUIAuth
import org.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.auth.UIABaseAuth
import org.matrix.android.sdk.api.auth.registration.RegistrationFlowResponse
import org.matrix.android.sdk.api.auth.registration.RegistrationResult
import org.matrix.android.sdk.api.auth.registration.Stage
import org.matrix.android.sdk.api.auth.registration.toFlowResult
import org.matrix.android.sdk.api.util.JsonDict
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReAuthStagesDataSource(
    context: Context,
) : BaseLoginStagesDataSource(context) {

    val finishReAuthEventLiveData = SingleEventLiveData<Unit>()
    private var authPromise: Continuation<UIABaseAuth>? = null
    private var sessionId: String = ""
    private var stageResultContinuation: Continuation<Response<RegistrationResult>>? =
        null

    fun startReAuthStages(
        session: String,
        loginStages: List<Stage>,
        promise: Continuation<UIABaseAuth>
    ) {
        sessionId = session
        authPromise = promise
        stageResultContinuation = null
        val userId = MatrixSessionProvider.currentSession?.myUserId ?: ""
        val domain = userId.substringAfter(":")
        val name = userId.substringAfter("@").substringBefore(":")
        super.startLoginStages(loginStages, name, domain)
    }

    override suspend fun performLoginStage(
        authParams: JsonDict,
        password: String?
    ): Response<RegistrationResult> {
        val result = suspendCoroutine {
            authPromise?.resume(CustomUIAuth(sessionId, authParams))
            stageResultContinuation = it
        }
        (result as? Response.Success)?.let {
            stageCompleted(it.data)
        }
        return result
    }

    fun onStageResult(
        promise: Continuation<UIABaseAuth>,
        flowResponse: RegistrationFlowResponse,
        errCode: String?
    ) {
        authPromise = promise
        val result = errCode?.let {
            Response.Error(it)
        } ?: Response.Success(RegistrationResult.FlowResponse(flowResponse.toFlowResult()))
        stageResultContinuation?.resume(result)
    }

    private fun stageCompleted(result: RegistrationResult) {
        (result as? RegistrationResult.Success)?.let {
            finishReAuthEventLiveData.postValue(Unit)
        } ?: navigateToNextStage()
    }
}