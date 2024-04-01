package org.futo.circles.feature.whats_new

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import org.futo.circles.BuildConfig
import org.futo.circles.R
import org.futo.circles.core.feature.markdown.MarkdownParser
import org.futo.circles.core.provider.PreferencesProvider
import org.futo.circles.databinding.DialogWhatsNewBinding


class WhatsNewDialog(context: Context) : AppCompatDialog(context) {

    private val binding = DialogWhatsNewBinding.inflate(LayoutInflater.from(context))
    private val preferencesProvider by lazy { PreferencesProvider(context) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCancelable(false)
        window?.apply {
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20))
            setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
        }
        setupViews()
    }

    private fun setupViews() {
        with(binding) {
            tvTitle.text = context.getString(R.string.whats_new_in_format, BuildConfig.VERSION_NAME)
            btnDone.setOnClickListener {
                preferencesProvider.storeWhatsNewShowedFor(BuildConfig.VERSION_CODE)
                dismiss()
            }
            tvDescription.apply {
                movementMethod = ScrollingMovementMethod()
                val text = context.getString(R.string.changelog).replace("-", "\n*")

                tvDescription.setText(MarkdownParser.parse(text), TextView.BufferType.SPANNABLE)
            }
        }
    }

    companion object {
        fun showIfNeed(context: Context) {
            val wasShownFor = PreferencesProvider(context).getWhatsNewShowedForVersion()
            val currentVersionCode = BuildConfig.VERSION_CODE / 100
            if (currentVersionCode > wasShownFor) WhatsNewDialog(context).show()
        }

    }

}