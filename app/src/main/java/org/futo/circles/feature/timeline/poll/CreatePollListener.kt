package org.futo.circles.feature.timeline.poll

import org.futo.circles.model.CreatePollContent

interface CreatePollListener {
    fun onCreatePoll(roomId: String, pollContent: CreatePollContent)
    fun onEditPoll(roomId: String, eventId: String, pollContent: CreatePollContent)
}