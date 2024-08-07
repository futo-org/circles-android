package org.futo.circles.core.model

import org.matrix.android.sdk.api.session.room.model.message.PollType

data class CreatePollContent(
    val pollType: PollType,
    val question: String,
    val options: List<String>
)