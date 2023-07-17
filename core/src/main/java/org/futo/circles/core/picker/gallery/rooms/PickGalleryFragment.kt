package org.futo.circles.core.picker.gallery.rooms

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.R
import org.futo.circles.core.databinding.FragmentPickGalleryBinding
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.picker.gallery.PickGalleryListener
import org.futo.circles.core.picker.gallery.rooms.list.PhotosListAdapter

@AndroidEntryPoint
class PickGalleryFragment : Fragment(R.layout.fragment_pick_gallery) {

    private val viewModel by viewModels<PickGalleryViewModel>()
    private val binding by viewBinding(FragmentPickGalleryBinding::bind)

    private var pickGalleryListener: PickGalleryListener? = null
    private val listAdapter by lazy {
        PhotosListAdapter(onRoomClicked = { gallery ->
            pickGalleryListener?.onGalleryChosen(gallery.id)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pickGalleryListener = parentFragment as? PickGalleryListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.rvRooms.adapter = listAdapter
    }

    private fun setupObservers() {
        viewModel.galleriesLiveData?.observeData(this) { listAdapter.submitList(it) }
    }
}