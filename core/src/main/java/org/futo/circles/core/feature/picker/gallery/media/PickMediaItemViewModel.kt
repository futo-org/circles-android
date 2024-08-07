package org.futo.circles.core.feature.picker.gallery.media

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import org.futo.circles.core.feature.circles.filter.CircleFilterAccountDataManager
import org.futo.circles.core.feature.picker.gallery.PickGalleryMediaDialogFragment.Companion.IS_VIDEO_AVAILABLE
import org.futo.circles.core.feature.timeline.BaseTimelineViewModel
import org.futo.circles.core.feature.timeline.data_source.BaseTimelineDataSource
import org.futo.circles.core.feature.timeline.data_source.TimelineType
import org.futo.circles.core.model.GalleryContentListItem
import org.futo.circles.core.model.GalleryTimelineLoadingListItem
import org.futo.circles.core.model.MediaContent
import org.futo.circles.core.model.Post
import org.futo.circles.core.model.PostContentType
import org.futo.circles.core.model.TimelineLoadingItem
import javax.inject.Inject

@HiltViewModel
class PickMediaItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    timelineDataSourceFactory: BaseTimelineDataSource.Factory,
    circleFilterAccountDataManager: CircleFilterAccountDataManager
) : BaseTimelineViewModel(
    savedStateHandle,
    context,
    timelineDataSourceFactory.create(TimelineType.GALLERY),
    circleFilterAccountDataManager
) {

    private val isVideoAvailable: Boolean = savedStateHandle[IS_VIDEO_AVAILABLE] ?: true

    private val selectedItemsFlow = MutableStateFlow<List<GalleryContentListItem>>(emptyList())

    val galleryItemsLiveData = combine(
        getTimelineEventFlow(),
        selectedItemsFlow
    ) { items, selectedIds ->
        items.mapNotNull { post ->
            when (post) {
                is Post -> {
                    (post.content as? MediaContent)?.let {
                        if (it.type == PostContentType.VIDEO_CONTENT && !isVideoAvailable) null
                        else GalleryContentListItem(
                            post.id,
                            post.postInfo,
                            it,
                            selectedIds.firstOrNull { it.id == post.id } != null
                        )
                    }
                }

                is TimelineLoadingItem -> GalleryTimelineLoadingListItem()
            }
        }
    }.distinctUntilChanged().asLiveData()


    fun toggleItemSelected(item: GalleryContentListItem) {
        val list = selectedItemsFlow.value.toMutableList()
        if (item.isSelected) list.removeIf { it.id == item.id }
        else list.add(item)
        selectedItemsFlow.value = list
    }
}