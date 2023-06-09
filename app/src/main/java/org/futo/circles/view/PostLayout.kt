package org.futo.circles.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.blure.complexview.Shadow
import org.futo.circles.R
import org.futo.circles.databinding.LayoutPostBinding
import org.futo.circles.extensions.convertDpToPixel
import org.futo.circles.extensions.setIsVisible
import org.futo.circles.feature.timeline.post.markdown.MarkdownParser
import org.futo.circles.model.MediaContent
import org.futo.circles.model.PollContent
import org.futo.circles.model.Post
import org.futo.circles.model.PostContent
import org.futo.circles.model.PostItemPayload
import org.futo.circles.model.TextContent
import org.matrix.android.sdk.api.session.room.send.SendState


interface PostOptionsListener {
    fun onUserClicked(userId: String)
    fun onShare(content: PostContent)
    fun onIgnore(senderId: String)
    fun onSaveToDevice(content: PostContent)
    fun onEditPostClicked(roomId: String, eventId: String)
    fun onSaveToGallery(roomId: String, eventId: String)
    fun onReply(roomId: String, eventId: String)
    fun onShowPreview(roomId: String, eventId: String)
    fun onShowEmoji(roomId: String, eventId: String)
    fun onReport(roomId: String, eventId: String)
    fun onRemove(roomId: String, eventId: String)
    fun onEmojiChipClicked(roomId: String, eventId: String, emoji: String, isUnSend: Boolean)
    fun onPollOptionSelected(roomId: String, eventId: String, optionId: String)
    fun endPoll(roomId: String, eventId: String)
    fun onEditPollClicked(roomId: String, eventId: String)
    fun onInfoClicked(roomId: String, eventId: String)
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


    fun setData(data: Post, userPowerLevel: Int, isThread: Boolean) {
        post = data
        setGeneralMessageData(data, userPowerLevel, isThread)
    }

    fun setPayload(payload: PostItemPayload) {
        setShadow(payload.readInfo.shouldIndicateAsNew)
        setSendStatus(payload.sendState, payload.readInfo.readByCount)
        binding.postFooter.setRepliesCount(payload.repliesCount)
    }

    private fun setGeneralMessageData(data: Post, userPowerLevel: Int, isThread: Boolean) {
        binding.postHeader.setData(data, userPowerLevel)
        binding.postFooter.setData(data, userPowerLevel, isThread)
        setMentionBorder(data.content)
        setIsEdited(data.postInfo.isEdited)
        setShadow(data.readInfo.shouldIndicateAsNew)
        setSendStatus(data.sendState, data.readInfo.readByCount)
    }

    private fun setMentionBorder(content: PostContent) {
        val hasMention = when (content) {
            is MediaContent -> content.mediaContentInfo.caption?.let {
                MarkdownParser.hasCurrentUserMention(it)
            } ?: false

            is TextContent -> MarkdownParser.hasCurrentUserMention(content.message)
            is PollContent -> false
        }
        if (hasMention)
            binding.lCard.setBackgroundResource(R.drawable.bg_mention_highlight)
        else binding.lCard.background = null
    }

    private fun setIsEdited(isEdited: Boolean) {
        binding.tvEditedLabel.setIsVisible(isEdited)
    }

    private fun setShadow(isNew: Boolean) {
        val color = if (isNew) "#0E7AFE" else "#8E8E93"
        binding.lShadow.shadow =
            Shadow(
                1, 255, color, GradientDrawable.RECTANGLE,
                FloatArray(8) { context.convertDpToPixel(4f) },
                Shadow.Position.CENTER
            )
    }

    private fun setSendStatus(sendState: SendState, readByCount: Int) {
        when {
            sendState.isSending() -> {
                binding.ivSendStatus.setImageResource(R.drawable.ic_sending)
                binding.tvReadByCount.text = ""
            }

            sendState.hasFailed() -> {
                binding.ivSendStatus.setImageResource(R.drawable.ic_send_failed)
                binding.tvReadByCount.text = ""
            }

            sendState.isSent() -> {
                if (readByCount > 0) {
                    binding.ivSendStatus.setImageResource(R.drawable.ic_seen)
                    binding.tvReadByCount.text = readByCount.toString()
                } else {
                    binding.ivSendStatus.setImageResource(R.drawable.ic_sent)
                    binding.tvReadByCount.text = ""
                }
            }
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (child.id == R.id.lShadow) {
            super.addView(child, index, params)
        } else {
            findViewById<FrameLayout>(R.id.lvContent).addView(child, index, params)
        }
    }
}