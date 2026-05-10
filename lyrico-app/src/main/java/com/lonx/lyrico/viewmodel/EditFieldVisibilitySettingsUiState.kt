package com.lonx.lyrico.viewmodel

import com.lonx.lyrico.data.editfield.EditFieldDefinition
import com.lonx.lyrico.data.editfield.EditFieldGroupDefinition
import com.lonx.lyrico.data.editfield.EditFieldVisibilityConfig

data class EditFieldVisibilitySettingsUiState(
    val config: EditFieldVisibilityConfig = EditFieldVisibilityConfig(),
    val groups: List<EditFieldVisibilityGroupUiState> = emptyList(),
)

data class EditFieldVisibilityGroupUiState(
    val group: EditFieldGroupDefinition,
    val checked: Boolean,
    val fields: List<EditFieldVisibilityFieldUiState>,
)

data class EditFieldVisibilityFieldUiState(
    val field: EditFieldDefinition,
    val checked: Boolean,
    val enabled: Boolean,
)
