package org.futo.circles.core.feature.picker.gallery.media

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.R
import org.futo.circles.core.databinding.FragmentPickGalleryBinding
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.base.list.BaseRvDecoration
import org.futo.circles.core.model.GalleryContentListItem
import org.futo.circles.core.feature.picker.gallery.PickGalleryMediaDialogFragment.Companion.IS_MULTI_SELECT
import org.futo.circles.core.feature.picker.gallery.PickGalleryMediaDialogFragment.Companion.IS_VIDEO_AVAILABLE
import org.futo.circles.core.feature.picker.gallery.PickGalleryMediaViewModel
import org.futo.circles.core.feature.picker.gallery.media.list.GalleryMediaGridAdapter
import org.futo.circles.core.feature.picker.gallery.media.list.GalleryMediaItemViewHolder


@AndroidEntryPoint
class PickMediaItemFragment : Fragment(R.layout.fragment_pick_gallery) {

    private val viewModel by viewModels<PickMediaItemViewModel>()
    private val parentViewModel by viewModels<PickGalleryMediaViewModel>({ requireParentFragment() })
    private val binding by viewBinding(FragmentPickGalleryBinding::bind)

    private val isMultiSelect by lazy {
        arguments?.getBoolean(IS_MULTI_SELECT) ?: false
    }

    private val listAdapter by lazy {
        GalleryMediaGridAdapter(
            isMultiSelect = isMultiSelect,
            onMediaItemClicked = { item -> onMediaItemSelected(item) },
            onLoadMore = { viewModel.loadMore() })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }


    private fun setupViews() {
        binding.rvRooms.apply {
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = listAdapter
            getRecyclerView().itemAnimator = null
            addItemDecoration(BaseRvDecoration.OffsetDecoration<GalleryMediaItemViewHolder>(2))
        }
    }

    private fun setupObservers() {
        viewModel.galleryItemsLiveData.observeData(this) {
            listAdapter.submitList(it)
        }
    }


    private fun onMediaItemSelected(item: GalleryContentListItem) {
        if (isMultiSelect) viewModel.toggleItemSelected(item)
        parentViewModel.onMediaItemClicked(requireContext(), item)
    }

    companion object {
        private const val ROOM_ID = "roomId"
        fun create(roomId: String, isVideoAvailable: Boolean, isMultiSelect: Boolean) =
            PickMediaItemFragment().apply {
                arguments = bundleOf(
                    ROOM_ID to roomId,
                    IS_VIDEO_AVAILABLE to isVideoAvailable,
                    IS_MULTI_SELECT to isMultiSelect
                )
            }
    }
}
