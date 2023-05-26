package org.futo.circles.di.data_source

import org.futo.circles.auth.feature.log_in.switch_user.SwitchUserDataSource
import org.futo.circles.auth.feature.pass_phrase.create.CreatePassPhraseDataSource
import org.futo.circles.auth.feature.pass_phrase.restore.RestoreBackupDataSource
import org.futo.circles.auth.feature.pass_phrase.restore.SSSSDataSource
import org.futo.circles.auth.feature.sign_up.username.UsernameDataSource
import org.futo.circles.core.provider.PreferencesProvider
import org.futo.circles.feature.photos.backup.service.MediaBackupServiceManager
import org.futo.circles.feature.rageshake.BugReportDataCollector
import org.futo.circles.feature.rageshake.BugReportDataSource
import org.futo.circles.feature.settings.SettingsDataSource
import org.futo.circles.feature.settings.active_sessions.ActiveSessionsDataSource
import org.futo.circles.feature.settings.change_password.ChangePasswordDataSource
import org.koin.dsl.module

val settingsDSModule = module {
    factory { SettingsDataSource(get(), get(), get()) }
    factory { ChangePasswordDataSource(get(), get()) }
    factory { ActiveSessionsDataSource(get(), get()) }
    single { BugReportDataCollector(get()) }
    factory { BugReportDataSource(get(), get()) }
    single { MediaBackupServiceManager() }

    //-----
    factory { PreferencesProvider(get()) }
    factory { SwitchUserDataSource() }
    factory { CreatePassPhraseDataSource(get(), get(), get()) }
    factory { RestoreBackupDataSource(get(), get(), get()) }
    factory { SSSSDataSource(get()) }
    factory { UsernameDataSource(get()) }

}