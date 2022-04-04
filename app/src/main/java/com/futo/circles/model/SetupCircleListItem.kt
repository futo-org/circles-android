package com.futo.circles.model

import android.net.Uri
import com.futo.circles.core.list.IdEntity

data class SetupCircleListItem(
    override val id: Int,
    val name: String,
    val userName: String,
    val coverUri: Uri? = null
) : IdEntity<Int>