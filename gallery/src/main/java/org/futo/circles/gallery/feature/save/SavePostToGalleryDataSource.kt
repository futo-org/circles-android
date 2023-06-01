package org.futo.circles.gallery.feature.save

import android.content.Context
import org.futo.circles.core.extensions.onBG
import org.futo.circles.core.model.MediaContent
import org.futo.circles.core.model.PostContent
import org.futo.circles.core.model.SelectableRoomListItem
import org.futo.circles.core.timeline.post.SendMessageDataSource
import org.futo.circles.core.utils.FileUtils

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