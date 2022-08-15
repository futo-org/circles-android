package org.futo.circles.core.picker.device.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.futo.circles.core.list.ViewBindingHolder
import org.futo.circles.core.list.context
import org.futo.circles.core.picker.device.PickDeviceMediaDataSource
import org.futo.circles.databinding.ListItemGalleryImageBinding
import org.futo.circles.databinding.ListItemGalleryVideoBinding
import org.futo.circles.extensions.onClick
import org.futo.circles.model.DeviceImageListItem
import org.futo.circles.model.DeviceMediaListItem
import org.futo.circles.model.DeviceVideoListItem

abstract class DeviceMediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(data: DeviceMediaListItem)
}

class DeviceImageViewHolder(
    parent: ViewGroup,
    onMediaClicked: (Int) -> Unit
) : DeviceMediaViewHolder(inflate(parent, ListItemGalleryImageBinding::inflate)) {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ListItemGalleryImageBinding

    init {
        onClick(binding.ivGalleryImage) { position -> onMediaClicked(position) }
    }

    override fun bind(data: DeviceMediaListItem) {
        if (data !is DeviceImageListItem) return
        Glide.with(context)
            .load(data.contentUri)
            .override(
                PickDeviceMediaDataSource.THUMBNAIL_SIZE,
                PickDeviceMediaDataSource.THUMBNAIL_SIZE
            )
            .into(binding.ivGalleryImage)
    }
}

class DeviceVideoViewHolder(
    parent: ViewGroup,
    onMediaClicked: (Int) -> Unit
) : DeviceMediaViewHolder(inflate(parent, ListItemGalleryVideoBinding::inflate)) {

    private companion object : ViewBindingHolder

    private val binding = baseBinding as ListItemGalleryVideoBinding

    init {
        onClick(itemView) { position -> onMediaClicked(position) }
    }

    override fun bind(data: DeviceMediaListItem) {
        if (data !is DeviceVideoListItem) return
        with(binding) {
            ivVideoCover.setImageBitmap(data.thumbnail)
            tvDuration.text = data.durationString
        }
    }
}