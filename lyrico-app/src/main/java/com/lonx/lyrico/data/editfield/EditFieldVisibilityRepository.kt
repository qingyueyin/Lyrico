package com.lonx.lyrico.data.editfield

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lonx.lyrico.data.repository.settingsDataStore
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EditFieldVisibilityRepository(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {

    val configFlow: Flow<EditFieldVisibilityConfig> =
        context.settingsDataStore.data.map { preferences ->
            val raw = preferences[EDIT_FIELD_VISIBILITY_OVERRIDES]

            val overrides = raw
                ?.let { decodeOverrides(it) }
                .orEmpty()
                .filterKnownCodes()

            EditFieldVisibilityConfig(overrides = overrides)
        }

    suspend fun setGroupVisible(groupCode: String, visible: Boolean) {
        require(EditFieldRegistry.groupMap.containsKey(groupCode)) {
            "Unknown edit field group code: $groupCode"
        }
        setVisible(groupCode, visible)
    }

    suspend fun setFieldVisible(fieldCode: String, visible: Boolean) {
        require(EditFieldRegistry.fieldMap.containsKey(fieldCode)) {
            "Unknown edit field code: $fieldCode"
        }
        setVisible(fieldCode, visible)
    }

    suspend fun setVisible(code: String, visible: Boolean) {
        requireKnownCode(code)

        context.settingsDataStore.edit { preferences ->
            val current = preferences[EDIT_FIELD_VISIBILITY_OVERRIDES]
                ?.let { decodeOverrides(it) }
                .orEmpty()
                .toMutableMap()

            current[code] = visible

            preferences[EDIT_FIELD_VISIBILITY_OVERRIDES] =
                encodeOverrides(current.filterKnownCodes())
        }
    }

    suspend fun reset(code: String) {
        requireKnownCode(code)

        context.settingsDataStore.edit { preferences ->
            val current = preferences[EDIT_FIELD_VISIBILITY_OVERRIDES]
                ?.let { decodeOverrides(it) }
                .orEmpty()
                .toMutableMap()

            current.remove(code)

            if (current.isEmpty()) {
                preferences.remove(EDIT_FIELD_VISIBILITY_OVERRIDES)
            } else {
                preferences[EDIT_FIELD_VISIBILITY_OVERRIDES] =
                    encodeOverrides(current.filterKnownCodes())
            }
        }
    }

    suspend fun resetAll() {
        context.settingsDataStore.edit { preferences ->
            preferences.remove(EDIT_FIELD_VISIBILITY_OVERRIDES)
        }
    }

    private fun decodeOverrides(raw: String): Map<String, Boolean> {
        return runCatching {
            val decoded = json.decodeFromString<EditFieldVisibilityOverridesJson>(raw)
            migrateOverrides(
                version = decoded.version,
                values = decoded.values,
            )
        }.getOrDefault(emptyMap())
    }

    private fun encodeOverrides(values: Map<String, Boolean>): String {
        return json.encodeToString(
            EditFieldVisibilityOverridesJson(
                version = CURRENT_VERSION,
                values = values,
            )
        )
    }

    private fun migrateOverrides(
        version: Int,
        values: Map<String, Boolean>,
    ): Map<String, Boolean> {
        val mutable = values.toMutableMap()
        return mutable
    }

    private fun Map<String, Boolean>.filterKnownCodes(): Map<String, Boolean> {
        return filterKeys { it in EditFieldRegistry.knownCodes }
    }

    private fun requireKnownCode(code: String) {
        require(code in EditFieldRegistry.knownCodes) {
            "Unknown edit field visibility code: $code"
        }
    }

    companion object {
        val EDIT_FIELD_VISIBILITY_OVERRIDES =
            stringPreferencesKey("edit_field_visibility_overrides")

        private const val CURRENT_VERSION = 1
    }
}
