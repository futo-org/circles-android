package org.futo.circles.core.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import org.futo.circles.core.model.MediaFileData
import java.io.InputStream

@GlideModule
class CirclesAppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(
            MediaFileData::class.java,
            InputStream::class.java,
            CirclesGlideModelLoaderFactory(context)
        )
    }
}