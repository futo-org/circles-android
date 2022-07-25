package org.futo.circles.feature.photos.preview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import org.futo.circles.R
import org.futo.circles.core.fragment.BaseFullscreenDialogFragment
import org.futo.circles.databinding.MediaPreviewDialogFragmentBinding
import org.futo.circles.extensions.*
import org.futo.circles.feature.timeline.post.share.ShareProvider
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class MediaPreviewDialogFragment :
    BaseFullscreenDialogFragment(MediaPreviewDialogFragmentBinding::inflate) {

    private val args: MediaPreviewDialogFragmentArgs by navArgs()
    private val viewModel by viewModel<MediaPreviewViewModel> {
        parametersOf(args.roomId, args.eventId)
    }

    private val binding by lazy { getBinding() as MediaPreviewDialogFragmentBinding }

    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hide() }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadData(requireContext())
        setupViews()
        setupToolbar()
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
        with(binding) {
            lParent.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            lParent.setOnClickListener { toggle() }
            ivImage.setOnClickListener { toggle() }
            videoView.setOnClickListener { toggle() }
            videoView.player = videoPlayer
            videoView.controllerShowTimeoutMs = AUTO_HIDE_DELAY_MILLIS.toInt()
        }

        delayedHide()
    }

    private fun setupToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { activity?.onBackPressed() }
            (menu as? MenuBuilder)?.setOptionalIconsVisible(true)
            setOnMenuItemClickListener { item ->
                return@setOnMenuItemClickListener when (item.itemId) {
                    R.id.save -> {
                        viewModel.save()
                        true
                    }
                    R.id.share -> {
                        viewModel.share()
                        true
                    }
                    R.id.delete -> {
                        showRemoveConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.imageLiveData.observeData(this) {
            binding.videoView.gone()
            it.mediaContentData.loadEncryptedIntoWithAspect(binding.ivImage, it.aspectRatio)
        }
        viewModel.videoLiveData.observeData(this) {
            binding.ivImage.gone()
            videoPlayer.setMediaItem(MediaItem.fromUri(it.second))
            videoPlayer.prepare()
            videoPlayer.play()
        }
        viewModel.shareLiveData.observeData(this) { content ->
            context?.let { ShareProvider.share(it, content) }
        }
        viewModel.downloadLiveData.observeData(this) {
            context?.let { showSuccess(it.getString(R.string.saved), false) }
        }
    }

    private fun showRemoveConfirmation() {
        showDialog(
            titleResIdRes = R.string.remove_image,
            messageResId = R.string.remove_image_message,
            positiveButtonRes = R.string.remove,
            negativeButtonVisible = true,
            positiveAction = {
                viewModel.removeImage()
                activity?.onBackPressed()
            }
        )
    }

    private fun toggle() {
        if (binding.toolbar.isVisible) hide() else show()
    }

    private fun hide() {
        binding.toolbar.gone()
    }

    private fun show() {
        binding.toolbar.visible()
        binding.videoView.showController()
        delayedHide()
    }

    private fun delayedHide() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, AUTO_HIDE_DELAY_MILLIS)
    }

    companion object {
        private const val AUTO_HIDE_DELAY_MILLIS = 3000L
    }
}