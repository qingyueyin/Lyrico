package com.lonx.lyrico.data.editfield

import androidx.annotation.StringRes

data class EditFieldDefinition(
    val code: String,
    val groupCode: String,
    @StringRes val titleRes: Int,
    val defaultVisible: Boolean = true,
    val order: Int,
    val scope: EditFieldScope = EditFieldScope.Both,
    val configurable: Boolean = true,
    val readOnly: Boolean = false,
)
