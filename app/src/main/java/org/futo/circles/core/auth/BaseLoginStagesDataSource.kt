package org.futo.circles.core.auth

import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.futo.circles.R
import org.futo.circles.core.*
import org.futo.circles.extensions.Response
import org.matrix.android.sdk.api.auth.registration.RegistrationResult
import org.matrix.android.sdk.api.auth.registration.Stage
import org.matrix.android.sdk.api.util.JsonDict

abstract class BaseLoginStagesDataSource(
    private val context: Context
) {

    val subtitleLiveData = MutableLiveData<String>()
    val loginStageNavigationLiveData = SingleEventLiveData<LoginStageNavigationEvent>()

    val stagesToComplete = mutableListOf<Stage>()
    var currentStage: Stage? = null
        private set

    var userName: String = ""
        private set
    var domain: String = ""
        private set

    protected var userPassword: String = ""

    fun startLoginStages(
        loginStages: List<Stage>,
        name: String,
        serverDomain: String
    ) {
        userName = name
        domain = serverDomain
        userPassword = ""
        currentStage = null
        stagesToComplete.clear()
        stagesToComplete.addAll(loginStages)
        navigateToNextStage()
    }

    protected fun getIdentifier() = mapOf(
        USER_PARAM_KEY to "@$userName:$domain",
        TYPE_PARAM_KEY to LOGIN_PASSWORD_USER_ID_TYPE
    )

    abstract suspend fun performLoginStage(
        authParams: JsonDict,
        password: String? = null
    ): Response<RegistrationResult>

    private fun getCurrentStageIndex() =
        stagesToComplete.indexOf(currentStage).takeIf { it != -1 } ?: 0

    private fun setNextStage() {
        currentStage = currentStage?.let {
            stagesToComplete.getOrNull(getCurrentStageIndex() + 1)
        } ?: stagesToComplete.firstOrNull()
    }

    protected fun isStageRetry(result: RegistrationResult?): Boolean {
        val nextStageType =
            ((result as? RegistrationResult.FlowResponse)?.flowResult?.missingStages?.lastOrNull() as? Stage.Other)?.type
        return nextStageType == (currentStage as? Stage.Other)?.type && nextStageType != null
    }

    protected fun navigateToNextStage() {
        setNextStage()
        val event = when (val stage = currentStage) {
            is Stage.Terms -> LoginStageNavigationEvent.Terms
            is Stage.Other -> handleStageOther(stage.type)
            else -> throw IllegalArgumentException(
                context.getString(R.string.not_supported_stage_format, stage.toString())
            )
        }
        event?.let { loginStageNavigationLiveData.postValue(it) }
        updatePageSubtitle()
    }

    private fun handleStageOther(type: String): LoginStageNavigationEvent? = when (type) {
        LOGIN_PASSWORD_TYPE -> LoginStageNavigationEvent.Password
        DIRECT_LOGIN_PASSWORD_TYPE -> LoginStageNavigationEvent.DirectPassword
        LOGIN_BSSPEKE_OPRF_TYPE -> LoginStageNavigationEvent.BSspekeLogin
        LOGIN_BSSPEKE_VERIFY_TYPE -> null
        REGISTRATION_BSSPEKE_OPRF_TYPE -> LoginStageNavigationEvent.BSspekeSignup
        REGISTRATION_BSSPEKE_SAVE_TYPE -> null
        else -> throw IllegalArgumentException(
            context.getString(R.string.not_supported_stage_format, type)
        )
    }

    private fun updatePageSubtitle() {
        val size = stagesToComplete.size
        val number = getCurrentStageIndex() + 1
        val subtitle = context.getString(R.string.sign_up_stage_subtitle_format, number, size)
        subtitleLiveData.postValue(subtitle)
    }

    companion object {
        const val USER_PARAM_KEY = "user"
    }
}