package org.futo.circles.auth.feature.uia.stages.password

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import org.futo.circles.auth.R
import org.futo.circles.auth.bsspeke.BSSpekeClient
import org.futo.circles.auth.bsspeke.BSSpekeClientProvider
import org.futo.circles.auth.feature.uia.UIADataSource.Companion.ENROLL_BSSPEKE_OPRF_TYPE
import org.futo.circles.auth.feature.uia.UIADataSource.Companion.ENROLL_BSSPEKE_SAVE_TYPE
import org.futo.circles.auth.feature.uia.UIADataSource.Companion.LOGIN_BSSPEKE_OPRF_TYPE
import org.futo.circles.auth.feature.uia.UIADataSource.Companion.LOGIN_BSSPEKE_VERIFY_TYPE
import org.futo.circles.auth.feature.uia.UIADataSource.Companion.TYPE_PARAM_KEY
import org.futo.circles.auth.feature.uia.UIADataSourceProvider
import org.futo.circles.core.extensions.Response
import org.matrix.android.sdk.api.auth.registration.RegistrationResult
import org.matrix.android.sdk.api.auth.registration.Stage
import javax.inject.Inject

class BsSpekeStageDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val uiaDataSource = UIADataSourceProvider.getDataSourceOrThrow()

    private var isVerifyStages = true

    suspend fun processPasswordStage(password: String): Response<Unit> {
        val currentStageKey = uiaDataSource.getCurrentStageKey()
        isVerifyStages = currentStageKey == LOGIN_BSSPEKE_OPRF_TYPE || currentStageKey== LOGIN_BSSPEKE_VERIFY_TYPE
        BSSpekeClientProvider.initClient(uiaDataSource.userName, uiaDataSource.domain, password)
        val bsSpekeClient = BSSpekeClientProvider.getClientOrThrow()
        return when (val oprfResult = performOprfStage(bsSpekeClient)) {
            is Response.Success -> processOprfSuccessResponse(
                oprfResult.data, bsSpekeClient, password,
            )

            is Response.Error -> oprfResult
        }
    }

    private fun getOprfTypeKey() = if (isVerifyStages) LOGIN_BSSPEKE_OPRF_TYPE
    else ENROLL_BSSPEKE_OPRF_TYPE

    private suspend fun performOprfStage(
        bsSpekeClient: BSSpekeClient
    ): Response<RegistrationResult> = uiaDataSource.performUIAStage(
        mapOf(
            TYPE_PARAM_KEY to getOprfTypeKey(),
            CURVE_PARAM_KEY to getCurve(),
            BLIND_PARAM_KEY to bsSpekeClient.generateBase64Blind()
        )
    )

    private suspend fun processOprfSuccessResponse(
        oprfResult: RegistrationResult,
        bsSpekeClient: BSSpekeClient,
        password: String
    ): Response<Unit> {
        val phfParams = getPhf()
        val blocks: Int
        val iterations: Int
        try {
            blocks = getBlocks(context, phfParams)
            iterations = getIterations(context, phfParams)
        } catch (e: Exception) {
            return Response.Error(e.message ?: "")
        }
        return when (val result =
            if (isVerifyStages) performVerifyStage(
                oprfResult, bsSpekeClient, password, blocks, iterations
            )
            else performSaveStage(
                oprfResult, bsSpekeClient, password, blocks, iterations, phfParams
            )) {
            is Response.Error -> result
            is Response.Success -> Response.Success(Unit)
        }
    }

    private suspend fun performSaveStage(
        oprfResult: RegistrationResult,
        bsSpekeClient: BSSpekeClient,
        password: String,
        blocks: Int,
        iterations: Int,
        phfParams: Map<String, Any?>
    ): Response<RegistrationResult> {
        val PandV: Pair<String, String>
        try {
            val blindSalt = getBlindSalt(context, oprfResult, ENROLL_BSSPEKE_SAVE_TYPE)
            PandV = bsSpekeClient.generateBase64PandV(blindSalt, blocks, iterations)
        } catch (e: Exception) {
            return Response.Error(e.message ?: "")
        }
        return uiaDataSource.performUIAStage(
            mapOf(
                TYPE_PARAM_KEY to ENROLL_BSSPEKE_SAVE_TYPE,
                P_PARAM_KEY to PandV.first,
                V_PARAM_KEY to PandV.second,
                PHF_PARAM_KEY to phfParams
            ), password = password
        )
    }

    private suspend fun performVerifyStage(
        oprfResult: RegistrationResult,
        bsSpekeClient: BSSpekeClient,
        password: String,
        blocks: Int,
        iterations: Int
    ): Response<RegistrationResult> {
        val A: String
        val verifier: String
        try {
            val blindSalt = getBlindSalt(context, oprfResult, LOGIN_BSSPEKE_VERIFY_TYPE)
            val B = getB(oprfResult)
            A = bsSpekeClient.generateABase64(blindSalt, blocks, iterations)
            bsSpekeClient.deriveSharedKey(B)
            verifier = bsSpekeClient.generateVerifierBase64()
        } catch (e: Exception) {
            return Response.Error(e.message ?: "")
        }
        return uiaDataSource.performUIAStage(
            mapOf(
                TYPE_PARAM_KEY to LOGIN_BSSPEKE_VERIFY_TYPE,
                A_PARAM_KEY to A,
                VERIFIER_PARAM_KEY to verifier
            ), password
        )
    }

    private fun getCurve(): String =
        ((getOprfStage() as? Stage.Other)?.params?.getOrDefault(
            CURVE_PARAM_KEY, ""
        ))?.toString() ?: ""

    private fun getPhf(): Map<String, Any?> =
        ((getOprfStage() as? Stage.Other)?.params?.getOrDefault(
            PHF_PARAM_KEY, emptyMap<String, Any>()
        ) as? Map<*, *>)?.map { it.key.toString() to it.value }?.toMap()
            ?: emptyMap()

    private fun getBlocks(context: Context, phfParams: Map<String, Any?>): Int =
        (phfParams.getOrDefault(PHF_BLOCKS_PARAM_KEY, 0) as? Double)
            ?.toInt()
            ?.takeIf { it >= MIN_PHF_BLOCKS }
            ?: throw IllegalArgumentException(
                context.getString(R.string.phf_blocks_error_format, MIN_PHF_BLOCKS)
            )

    private fun getIterations(context: Context, phfParams: Map<String, Any?>): Int =
        (phfParams.getOrDefault(PHF_ITERATIONS_PARAM_KEY, 0) as? Double)
            ?.toInt()
            ?.takeIf { it >= MIN_PHF_ITERATIONS }
            ?: throw IllegalArgumentException(
                context.getString(R.string.phf_iterations_error_format, MIN_PHF_ITERATIONS)
            )

    private fun getBlindSalt(
        context: Context,
        oprfResult: RegistrationResult,
        stageType: String
    ): ByteArray {
        val stage =
            ((oprfResult as? RegistrationResult.FlowResponse)?.flowResult?.missingStages?.firstOrNull {
                (it as? Stage.Other)?.type == stageType
            } as? Stage.Other)
                ?: throw IllegalArgumentException(context.getString(R.string.blind_salt_is_missing))

        val blindSaltString = stage.params?.getOrDefault(BLIND_SALT_PARAM_KEY, "")?.toString()
        return Base64.decode(blindSaltString, Base64.NO_WRAP)
    }

    private fun getB(oprfResult: RegistrationResult): ByteArray {
        val verifyStage =
            ((oprfResult as? RegistrationResult.FlowResponse)?.flowResult?.missingStages?.firstOrNull {
                (it as? Stage.Other)?.type == LOGIN_BSSPEKE_VERIFY_TYPE
            } as? Stage.Other)
                ?: throw IllegalArgumentException(context.getString(R.string.b_param_is_missing))

        val blindSaltString = verifyStage.params?.getOrDefault(B_PARAM_KEY, "")?.toString()
        return Base64.decode(blindSaltString, Base64.NO_WRAP)
    }

    private fun getOprfStage(): Stage? =
        uiaDataSource.stagesToComplete.firstOrNull { (it as? Stage.Other)?.type == getOprfTypeKey() }

    companion object {
        private const val CURVE_PARAM_KEY = "curve"
        private const val PHF_PARAM_KEY = "phf_params"
        private const val BLIND_SALT_PARAM_KEY = "blind_salt"
        private const val PHF_ITERATIONS_PARAM_KEY = "iterations"
        private const val PHF_BLOCKS_PARAM_KEY = "blocks"
        private const val MIN_PHF_ITERATIONS = 3
        private const val MIN_PHF_BLOCKS = 100_000
        private const val BLIND_PARAM_KEY = "blind"

        //Signup
        private const val P_PARAM_KEY = "P"
        private const val V_PARAM_KEY = "V"

        //Login
        private const val A_PARAM_KEY = "A"
        private const val B_PARAM_KEY = "B"
        private const val VERIFIER_PARAM_KEY = "verifier"
    }
}