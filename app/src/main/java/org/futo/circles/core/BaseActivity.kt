package org.futo.circles.core

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import org.futo.circles.extensions.disableScreenScale
import org.futo.circles.feature.rageshake.RageShake

abstract class BaseActivity(contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {

    private val rageShake by lazy { RageShake(this) }
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextWrapper(newBase.disableScreenScale()))
    }

    override fun onResume() {
        super.onResume()
        rageShake.start()
    }

    override fun onPause() {
        super.onPause()
        rageShake.stop()
    }
}