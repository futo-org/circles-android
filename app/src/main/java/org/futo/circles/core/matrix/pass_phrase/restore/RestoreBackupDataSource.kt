package org.futo.circles.core.matrix.pass_phrase.restore

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import org.futo.circles.R
import org.futo.circles.feature.settings.active_sessions.CrossSigningDataSource
import org.futo.circles.model.KeyData
import org.futo.circles.model.LoadingData
import org.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.listeners.StepProgressListener
import org.matrix.android.sdk.api.session.crypto.keysbackup.KeysBackupLastVersionResult
import org.matrix.android.sdk.api.session.crypto.keysbackup.KeysBackupService
import org.matrix.android.sdk.api.session.crypto.keysbackup.KeysVersionResult
import org.matrix.android.sdk.api.session.crypto.keysbackup.toKeysVersionResult
import org.matrix.android.sdk.api.util.awaitCallback

class RestoreBackupDataSource(
    private val context: Context,
    private val ssssDataSource: SSSSDataSource,
    private val crossSigningDataSource: CrossSigningDataSource
) {

    val loadingLiveData = MutableLiveData<LoadingData>()
    private val passPhraseLoadingData = LoadingData()

    private val progressObserver = object : StepProgressListener {
        override fun onStepProgress(step: StepProgressListener.Step) {
            when (step) {
                is StepProgressListener.Step.ComputingKey -> {
                    loadingLiveData.postValue(passPhraseLoadingData.apply {
                        this.progress = step.progress
                        this.total = step.total
                        messageId = R.string.computing_recovery_key
                        isLoading = true
                    })
                }
                is StepProgressListener.Step.DownloadingKey -> {
                    loadingLiveData.postValue(passPhraseLoadingData.apply {
                        messageId = R.string.downloading_keys
                        isLoading = true
                    })
                }
                is StepProgressListener.Step.ImportingKey -> {
                    loadingLiveData.postValue(passPhraseLoadingData.apply {
                        this.progress = step.progress
                        this.total = step.total
                        messageId = R.string.importing_keys
                        isLoading = true
                    })
                }
            }
        }
    }

    suspend fun getEncryptionAlgorithm(): String? {
        val keyVersion = awaitCallback {
            MatrixSessionProvider.currentSession?.cryptoService()?.keysBackupService()
                ?.getCurrentVersion(it)
        }.toKeysVersionResult()

        return keyVersion?.algorithm
    }

    suspend fun restoreKeysWithPassPhase(
        passphrase: String,
        userName: String,
        isBsSpeke: Boolean = false
    ) {
        val keyData =
            if (!ssssDataSource.isBackupKeyInQuadS())
                ssssDataSource.storeIntoSSSSWithPassphrase(passphrase, userName, isBsSpeke)
            else
                ssssDataSource.getRecoveryKeyFromPassphrase(
                    context, passphrase, progressObserver, isBsSpeke
                )
        restoreKeysWithRecoveryKey(keyData)
        loadingLiveData.postValue(passPhraseLoadingData.apply { isLoading = false })
    }

    private suspend fun restoreKeysWithRecoveryKey(keyData: KeyData) {
        val keysBackupService = getKeysBackupService()
        val keyVersion = getKeysVersion(keysBackupService)
        try {
            awaitCallback {
                keysBackupService.restoreKeysWithRecoveryKey(
                    keyVersion,
                    keyData.recoveryKey,
                    null,
                    MatrixSessionProvider.currentSession?.myUserId,
                    progressObserver,
                    it
                )
            }
            crossSigningDataSource.configureCrossSigning(keyData.keySpec)
            trustOnDecrypt(keysBackupService, keyVersion)
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.backup_could_not_be_decrypted_with_key))
        }
        loadingLiveData.postValue(passPhraseLoadingData.apply { isLoading = false })
    }

    suspend fun restoreKeysWithRecoveryKey(uri: Uri) {
        val key = readRecoveryKeyFile(uri)
        val keyData =
            if (!ssssDataSource.isBackupKeyInQuadS()) ssssDataSource.storeIntoSSSSWithKey(key)
            else ssssDataSource.getRecoveryKeyFromFileKey(context, key, progressObserver)
        restoreKeysWithRecoveryKey(keyData)
    }

    @SuppressLint("Recycle")
    private fun readRecoveryKeyFile(uri: Uri): String {
        val recoveryKey = context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }

        return recoveryKey?.takeIf { it.isNotEmpty() }
            ?: throw Exception(context.getString(R.string.backup_could_not_be_decrypted_with_key))
    }

    private suspend fun trustOnDecrypt(
        keysBackup: KeysBackupService,
        keysVersionResult: KeysVersionResult
    ) {
        awaitCallback {
            keysBackup.trustKeysBackupVersion(keysVersionResult, true, it)
        }
    }

    private fun getKeysBackupService() =
        MatrixSessionProvider.currentSession?.cryptoService()?.keysBackupService()
            ?: throw Exception(context.getString(R.string.session_is_not_created))

    private suspend fun getKeysVersion(keysBackupService: KeysBackupService) =
        awaitCallback<KeysBackupLastVersionResult> {
            keysBackupService.getCurrentVersion(it)
        }.toKeysVersionResult()
            ?: throw Exception(context.getString(R.string.failed_to_get_restore_keys_version))
}