package org.futo.circles.extensions

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Size
import android.widget.ImageView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import org.futo.circles.R
import org.futo.circles.feature.blurhash.ThumbHash
import org.futo.circles.glide.GlideApp
import org.futo.circles.model.MediaFileData
import org.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.Session

fun ImageView.loadImage(url: String?) {
    Glide.with(this)
        .load(url)
        .fitCenter()
        .into(this)
}

fun ImageView.loadEncryptedImage(
    content: MediaFileData,
    preferredSize: Size? = null,
    loadOriginalSize: Boolean = false,
    thumbHash: String? = null
) {
    val loadWidth = if (loadOriginalSize) Target.SIZE_ORIGINAL else preferredSize?.width ?: width
    val loadHeight = if (loadOriginalSize) Target.SIZE_ORIGINAL else preferredSize?.height ?: height
    val placeholder = thumbHash?.let {
        BitmapDrawable(
            resources,
            ThumbHash.getBitmapFromHash(
                it,
                loadWidth.takeIf { it > 0 },
                loadHeight.takeIf { it > 0 })
        )
    }
    content.elementToDecrypt?.let {
        GlideApp
            .with(context)
            .load(content)
            .placeholder(placeholder)
            .override(loadWidth, loadHeight)
            .fitCenter()
            .into(this)
    } ?: loadMatrixImage(content.fileUrl, loadOriginalSize, preferredSize = preferredSize)
}

fun ImageView.loadProfileIcon(
    url: String?,
    userId: String,
    loadOriginalSize: Boolean = false,
    preferredSize: Size? = null,
    session: Session? = null
) {

    val backgroundColor = ColorGenerator.DEFAULT.getColor(userId)
    var text = userId.firstOrNull()?.toString()?.uppercase() ?: ""
    if (text == "@") text = userId.elementAtOrNull(1)?.toString()?.uppercase() ?: "?"
    val placeholder = TextDrawable.Builder()
        .setShape(TextDrawable.SHAPE_ROUND_RECT)
        .setColor(backgroundColor)
        .setTextColor(Color.WHITE)
        .setText(text)
        .build()

    loadMatrixImage(url, loadOriginalSize, placeholder, preferredSize, session)
}


fun ImageView.setIsEncryptedIcon(isEncrypted: Boolean) {
    setImageResource(if (isEncrypted) R.drawable.ic_lock else R.drawable.ic_lock_open)
}

private fun ImageView.loadMatrixImage(
    url: String?,
    loadOriginalSize: Boolean = false,
    placeholder: Drawable? = null,
    preferredSize: Size? = null,
    session: Session? = null
) {
    val currentSession = session ?: MatrixSessionProvider.currentSession
    val size = if (loadOriginalSize) null else preferredSize ?: Size(width, height)
    val resolvedUrl = currentSession?.resolveUrl(url, size)
    Glide.with(this)
        .load(resolvedUrl)
        .fitCenter()
        .error(placeholder)
        .into(this)
}