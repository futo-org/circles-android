package org.futo.circles.core.feature.media

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.base.fragment.BaseBindingFragment
import org.futo.circles.core.databinding.FragmentFullScreenMediaBinding
import org.futo.circles.core.extensions.gone
import org.futo.circles.core.extensions.loadEncryptedIntoWithAspect
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.setIsVisible

@AndroidEntryPoint
class FullScreenMediaFragment :
    BaseBindingFragment<FragmentFullScreenMediaBinding>(FragmentFullScreenMediaBinding::inflate) {

    private val viewModel by viewModels<FullScreenMediaViewModel>()

    private val videoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onIsLoadingChanged(isLoading: Boolean) {
                    super.onIsLoadingChanged(isLoading)
                    binding.vLoading.setIsVisible(isLoading)
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadData(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.play()
    }

    override fun onDestroy() {
        videoPlayer.stop()
        videoPlayer.release()
        super.onDestroy()
    }

    private fun setupViews() {
        binding.videoView.player = videoPlayer
        val transitionName = arguments?.getString(EVENT_ID) ?: ""
        binding.ivImage.transitionName = transitionName
        binding.videoView.transitionName = transitionName
    }

    private fun setupObservers() {
        viewModel.imageLiveData.observeData(this) {
            binding.videoView.apply {
                transitionName = null
                gone()
            }
            it.mediaFileData.loadEncryptedIntoWithAspect(binding.ivImage, it.thumbHash)
            binding.ivImage.post { parentFragment?.startPostponedEnterTransition() }
        }
        viewModel.videoLiveData.observeData(this) {
            binding.ivImage.apply {
                transitionName = null
                gone()
            }
            videoPlayer.setMediaItem(MediaItem.fromUri(it.second))
            videoPlayer.prepare()
            videoPlayer.play()
            binding.videoView.post { parentFragment?.startPostponedEnterTransition() }
        }
    }

    companion object {
        private const val ROOM_ID = "roomId"
        private const val EVENT_ID = "eventId"

        fun create(roomId: String, eventId: String) =
            FullScreenMediaFragment().apply {
                arguments = bundleOf(ROOM_ID to roomId, EVENT_ID to eventId)
            }
    }
}