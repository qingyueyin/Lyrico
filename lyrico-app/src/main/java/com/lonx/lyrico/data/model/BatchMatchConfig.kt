package com.lonx.lyrico.data.model

import android.os.Parcelable
import androidx.annotation.StringRes
import com.lonx.lyrico.R
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BatchMatchConfig(
    val fields: Map<BatchMatchField, BatchMatchMode>,
    val concurrency: Int = 3,
    val preferFileName: Boolean = false
) : Parcelable {
}

@Serializable
enum class BatchMatchMode(
    @field:StringRes val labelRes: Int
) {
    SUPPLEMENT(R.string.batch_match_mode_supplement), // 仅为空时补充
    OVERWRITE(R.string.batch_match_mode_overwrite)   // 覆盖
}

@Serializable
enum class BatchMatchField(
    @field:StringRes val labelRes: Int,
    @field:StringRes val summaryRes: Int = 0
) {
    TITLE(R.string.label_title),
    ARTIST(R.string.label_artists),
    ALBUM(R.string.label_album),
    GENRE(R.string.label_genre),
    DATE(R.string.label_year),
    TRACK_NUMBER(R.string.label_track_number),
    LYRICS(R.string.label_lyrics),
    COMMENT(R.string.label_comment),
    COVER(R.string.label_cover)
}
object BatchMatchConfigDefaults {
    val DEFAULT_CONFIG = BatchMatchConfig(
        fields = mapOf(
            BatchMatchField.TITLE to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.ARTIST to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.ALBUM to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.GENRE to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.DATE to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.TRACK_NUMBER to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.LYRICS to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.COMMENT to BatchMatchMode.SUPPLEMENT,
            BatchMatchField.COVER to BatchMatchMode.SUPPLEMENT
        ),
        concurrency = 3,
        preferFileName = false
    )
}
