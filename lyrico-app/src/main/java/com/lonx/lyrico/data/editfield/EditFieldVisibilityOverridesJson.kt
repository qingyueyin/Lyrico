package com.lonx.lyrico.data.editfield

import kotlinx.serialization.Serializable

@Serializable
data class EditFieldVisibilityOverridesJson(
    val version: Int = 1,
    val values: Map<String, Boolean> = emptyMap(),
)
