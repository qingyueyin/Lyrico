package com.lonx.lyrico.data.editfield

import androidx.annotation.StringRes

data class EditFieldGroupDefinition(
    val code: String,
    @StringRes val titleRes: Int,
    val defaultVisible: Boolean = true,
    val order: Int,
)
