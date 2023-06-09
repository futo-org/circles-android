package org.futo.circles.extensions

import android.text.Editable
import org.futo.circles.feature.timeline.post.markdown.span.TextStyle
import org.futo.circles.feature.timeline.post.markdown.span.toSpanClass

fun Editable.getGivenSpansAt(
    vararg span: TextStyle,
    start: Int = 0,
    end: Int = length
): MutableList<Any> {
    val spanList = mutableListOf<Any>()
    for (selectedSpan in span) {
        getSpans(start, end, selectedSpan.toSpanClass()).forEach { spanList.add(it) }
    }
    return spanList
}
