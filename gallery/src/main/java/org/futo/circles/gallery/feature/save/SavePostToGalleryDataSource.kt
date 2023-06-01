package org.futo.circles.gallery.feature.save

import android.content.Context
import org.futo.circles.core.extensions.onBG
import org.futo.circles.core.timeline.SendMessageDataSource
import org.futo.circles.core.utils.FileUtils
import org.futo.circles.gallery.model.SelectableRoomListItem
import org.futo.circles.model.MediaContent
import org.futo.circles.model.PostContent

class SavePostToGalleryDataSource(
    private val context: Context,
    private val sendMessageDataSource: SendMessageDataSource
) {

    suspend fun saveMediaToGalleries(
        content: PostContent,
        selectedGalleries: List<SelectableRoomListItem>
    ) {
        val mediaContent = content as? MediaContent ?: return
        onBG {
            val uri =
                FileUtils.downloadEncryptedFileToContentUri(context, mediaContent.mediaFileData)
            uri?.let {
                selectedGalleries.forEach {
                    sendMessageDataSource.sendMedia(
                        it.id, uri, null, null, mediaContent.getMediaType()
                    )
                }
            }
        }
    }
}