package org.futo.circles.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import org.futo.circles.R
import org.futo.circles.databinding.LayoutPostBinding
import org.futo.circles.extensions.gone
import org.futo.circles.extensions.setIsVisible
import org.futo.circles.model.*


interface PostOptionsListener {
    fun onShowRepliesClicked(eventId: String)
    fun onShare(content: PostContent)
    fun onIgnore(senderId: String)
    fun onSaveToDevice(content: PostContent)
    fun onSaveToGallery(roomId: String, eventId: String)
    fun onReply(roomId: String, eventId: String, userName: String)
    fun onShowPreview(roomId: String, eventId: String)
    fun onShowEmoji(roomId: String, eventId: String)
    fun onReport(roomId: String, eventId: String)
    fun onRemove(roomId: String, eventId: String)
    fun onEmojiChipClicked(roomId: String, eventId: String, emoji: String, isUnSend: Boolean)
}

class PostLayout(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    private val binding =
        LayoutPostBinding.inflate(LayoutInflater.from(context), this)

    private var optionsListener: PostOptionsListener? = null
    private var post: Post? = null

    init {
        binding.btnShowReplies.setOnClickListener {
            post?.let { optionsListener?.onShowRepliesClicked(it.id) }
        }
        binding.lvContent.setOnClickListener {
            post?.let {
                if (it.content.isMedia()) optionsListener?.onShowPreview(it.postInfo.roomId, it.id)
            }
        }
    }

    fun setListener(postOptionsListener: PostOptionsListener) {
        optionsListener = postOptionsListener
        binding.postFooter.setListener(postOptionsListener)
        binding.postHeader.setListener(postOptionsListener)
    }


    fun setData(data: Post, userPowerLevel: Int) {
        post = data
        setGeneralMessageData(data, userPowerLevel)
        bindRepliesButton(data)
    }

    fun setPayload(payload: PostItemPayload) {
        bindRepliesButton(payload.hasReplies, payload.repliesCount, payload.isRepliesVisible)
    }

    private fun setGeneralMessageData(data: Post, userPowerLevel: Int) {
        val isReply = data is ReplyPost
        binding.vReplyMargin.setIsVisible(isReply)
        binding.postHeader.setData(data, userPowerLevel)
        binding.postFooter.setData(data, isReply)
    }

    private fun bindRepliesButton(post: Post) {
        val rootPost = (post as? RootPost) ?: kotlin.run { binding.btnShowReplies.gone(); return }

        bindRepliesButton(
            rootPost.hasReplies(), rootPost.getRepliesCount(), rootPost.isRepliesVisible
        )
    }

    private fun bindRepliesButton(
        hasReplies: Boolean,
        repliesCount: Int,
        isRepliesVisible: Boolean
    ) {
        with(binding.btnShowReplies) {
            setIsVisible(hasReplies)
            setClosedText(
                context.resources.getQuantityString(
                    R.plurals.show_replies_plurals,
                    repliesCount, repliesCount
                )
            )
            setIsOpened(isRepliesVisible)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (child.id == R.id.postCard || child.id == R.id.btnShowReplies || child.id == R.id.vReplyMargin) {
            super.addView(child, index, params)
        } else {
            findViewById<FrameLayout>(R.id.lvContent).addView(child, index, params)
        }
    }
}