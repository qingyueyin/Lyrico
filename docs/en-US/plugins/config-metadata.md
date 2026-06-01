# Configuration And Result Fields

The plugin protocol keeps `configFields` as the manifest declaration for user settings. Runtime metadata is returned through `fields`, and plugin-private context is returned through `internal`.

## Configuration Fields

`configFields` control plugin runtime behavior. Lyrico passes saved settings to every plugin function through the request `config` object:

```json
{
  "keyword": "Song Artist",
  "page": 1,
  "pageSize": 20,
  "config": {
    "lyrics_source": "official"
  }
}
```

Supported types: `text`, `password`, `number`, `switch`, `dropdown`, `textarea`, and `markdown`. `markdown` is for explanatory text and is not saved into runtime config.

## fields

Put only host-standard metadata fields in `fields`:

```json
{
  "id": "12345",
  "title": "Song Title",
  "artist": "Artist",
  "fields": {
    "title": "Song Title",
    "artist": "Artist",
    "album": "Album Title",
    "date": "2024",
    "lyrics": "[00:00.00]..."
  }
}
```

Unknown keys are ignored and reported during debugging. Do not put platform-private values in `fields`.

## internal

Put platform-private context in `internal`:

```json
{
  "id": "12345",
  "fields": {
    "title": "Song Title"
  },
  "internal": {
    "song_id": "12345",
    "album_id": "67890",
    "lyrics_id": "abc"
  }
}
```

`internal` is not displayed, not written to audio tags, not shown in batch field selection, and not passed to other plugins. It is only passed back to the same plugin, for example `getLyrics(request.song.internal.lyrics_id)`.

## Apply Policy

Lyrico manages field application. Batch matching builds a three-state policy for host-standard fields:

| Mode | Behavior |
|------|----------|
| Disabled | Do not write this field |
| Supplement | Write only when the current tag is empty |
| Overwrite | Always replace the existing value |

Plugins only fetch and return data. Lyrico applies data according to the user's policy.
