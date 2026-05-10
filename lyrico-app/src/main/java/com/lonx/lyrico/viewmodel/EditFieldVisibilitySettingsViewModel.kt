package com.lonx.lyrico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.lyrico.data.editfield.EditFieldRegistry
import com.lonx.lyrico.data.editfield.EditFieldVisibilityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditFieldVisibilitySettingsViewModel(
    private val repository: EditFieldVisibilityRepository,
) : ViewModel() {

    val uiState: StateFlow<EditFieldVisibilitySettingsUiState> =
        repository.configFlow
            .map { config ->
                val groups = EditFieldRegistry.groups
                    .sortedBy { it.order }
                    .map { group ->
                        val groupChecked = config.isGroupSwitchChecked(group)

                        val fields = EditFieldRegistry.fieldsOf(group.code)
                            .filter { it.configurable }
                            .map { field ->
                                EditFieldVisibilityFieldUiState(
                                    field = field,
                                    checked = config.isFieldSwitchChecked(field),
                                    enabled = groupChecked,
                                )
                            }

                        EditFieldVisibilityGroupUiState(
                            group = group,
                            checked = groupChecked,
                            fields = fields,
                        )
                    }

                EditFieldVisibilitySettingsUiState(
                    config = config,
                    groups = groups,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = EditFieldVisibilitySettingsUiState(),
            )

    fun setGroupVisible(groupCode: String, visible: Boolean) {
        viewModelScope.launch {
            repository.setGroupVisible(groupCode, visible)
        }
    }

    fun setFieldVisible(fieldCode: String, visible: Boolean) {
        viewModelScope.launch {
            repository.setFieldVisible(fieldCode, visible)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            repository.resetAll()
        }
    }
}
