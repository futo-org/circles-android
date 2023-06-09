package org.futo.circles.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import org.futo.circles.R
import org.futo.circles.databinding.ViewNotificationCounterBinding
import org.futo.circles.extensions.getAttributes
import org.futo.circles.extensions.setIsVisible

class NotificationCounterView(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    private val binding =
        ViewNotificationCounterBinding.inflate(LayoutInflater.from(context), this)


    private var notificationCircleColor: Int = -1

    init {
        getAttributes(attrs, R.styleable.NotificationCounterView) {
            notificationCircleColor = getColor(
                R.styleable.NotificationCounterView_notificationCircleBackground,
                ContextCompat.getColor(context, android.R.color.holo_red_dark)
            )
        }
    }

    fun setCount(count: Int) {
        setIsVisible(count > 0)
        if (count > 0) {
            binding.ivCounter.setImageDrawable(
                TextDrawable.Builder()
                    .setShape(TextDrawable.SHAPE_ROUND_RECT)
                    .setColor(notificationCircleColor)
                    .setTextColor(Color.WHITE)
                    .setBold()
                    .setText(count.toString())
                    .build()
            )
        }
    }
}