package org.futo.circles.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import org.matrix.android.sdk.api.extensions.orFalse
import org.matrix.android.sdk.api.util.getApplicationInfoCompat

fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

fun Context.disableScreenScale(): Context {
    val overrideConfiguration = resources.configuration.apply {
        fontScale = 1f
        densityDpi = resources.displayMetrics.xdpi.toInt()
    }
    return createConfigurationContext(overrideConfiguration)
}

fun Context.convertDpToPixel(dp: Float): Float {
    return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

@Px
fun Context.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
        .toInt()

fun Context.getBitmap(@DrawableRes drawableRes: Int): Bitmap? {
    val drawable =
        ResourcesCompat.getDrawable(resources, drawableRes, null) ?: return null
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.draw(canvas)
    return bitmap
}

fun Context.getApplicationLabel(packageName: String): String {
    return try {
        val applicationInfo = packageManager.getApplicationInfoCompat(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}

fun Context.isConnectedToWifi(): Boolean {
    val connectivityManager = getSystemService<ConnectivityManager>() ?: return false
    return connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        .orFalse()
}