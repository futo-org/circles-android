package org.futo.circles.gallery.feature.preview

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.core.extensions.gone
import org.futo.circles.core.extensions.loadEncryptedIntoWithAspect
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.onBackPressed
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.extensions.showSuccess
import org.futo.circles.core.extensions.visible
import org.futo.circles.core.extensions.withConfirmation
import org.futo.circles.core.fragment.BaseFullscreenDialogFragment
import org.futo.circles.core.share.ShareProvider
import org.futo.circles.gallery.R
import org.futo.circles.gallery.databinding.DialogFragmentMediaPreviewBinding
import org.futo.circles.gallery.model.RemoveImage

@AndroidEntryPoint
class MediaPreviewDialogFragment :
    BaseFullscreenDialogFragment(DialogFragmentMediaPreviewBinding::inflate) {

    private val viewModel by viewModels<MediaPreviewViewModel>()
    private val binding by lazy { getBinding() as DialogFragmentMediaPreviewBinding }

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
        dialog?.window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            it.statusBarColor = Color.BLACK
        }
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViews() {
        with(binding) {
            lParent.setOnClickListener { toggle() }
            ivImage.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) toggle()
                false
            }
            videoView.setOnClickListener { toggle() }
            videoView.player = videoPlayer
            videoView.controllerShowTimeoutMs = AUTO_HIDE_DELAY_MILLIS.toInt()
        }
        delayedHide()
    }

    @SuppressLint("RestrictedApi")
    private fun setupToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { onBackPressed() }
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
                        withConfirmation(RemoveImage()) {
                            viewModel.removeImage()
                            onBackPressed()
                        }
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
            it.mediaFileData.loadEncryptedIntoWithAspect(
                binding.ivImage,
                it.aspectRatio,
                it.mediaContentInfo.thumbHash
            )
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
            context?.let { showSuccess(it.getString(R.string.saved)) }
        }
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