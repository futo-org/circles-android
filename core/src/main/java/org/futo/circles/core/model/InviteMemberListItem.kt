package org.futo.circles.core.model

import org.futo.circles.core.R
import org.futo.circles.core.base.list.IdEntity

sealed class InviteMemberListItem : IdEntity<String>

data class HeaderItem(
    val titleRes: Int
) : InviteMemberListItem() {
    override val id: String = titleRes.toString()

    companion object {
        val knownUsersHeader = HeaderItem(R.string.known_users)
        val suggestionHeader = HeaderItem(R.string.suggestions)
    }
}

data class UserListItem(
    val user: CirclesUserSummary,
    val isSelected: Boolean = false
) : InviteMemberListItem() {
    override val id: String = user.id
}

data class NoResultsItem(
    val titleRes: Int = R.string.no_results
) : InviteMemberListItem() {
    override val id: String = titleRes.toString()
}