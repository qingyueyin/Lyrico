# Manifest Reference

`manifest.json` describes plugin identity, version, entry file, capabilities, and settings. Plugins should not declare which metadata fields they may return, which host APIs they need, or how fields should be written to audio tags.

Field application policy belongs to Lyrico. Plugins return data at runtime.

## Field Overview

| Field | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `id` | `string` | Yes | - | Unique plugin ID in reverse-domain format |
| `name` | `string` | Yes | - | Display name |
| `versionCode` | `int` | Yes | - | Version code, at least 1 |
| `versionName` | `string` | Yes | - | Version name |
| `apiVersion` | `int` | Yes | - | Plugin API version |
| `minHostApiVersion` | `int` | No | `1` | Minimum host API version |
| `author` | `string` | No | `""` | Author |
| `description` | `string` | No | `""` | Description |
| `entry` | `string` | No | `"source.js"` | Entry JavaScript file |
| `includeDirs` | `string[]` | No | `[]` | Local helper script directories |
| `icon` | `string \| null` | No | `null` | Relative icon path |
| `capabilities` | `string[]` | No | `[]` | Plugin capabilities |
| `configFields` | `ConfigField[]` | No | `[]` | User-configurable fields |

Fields used by older protocols to declare host APIs, returned fields, or write policies are no longer needed. New plugins should not include them.

## Example

```json
{
  "id": "com.example.source",
  "name": "Example Source",
  "versionCode": 1,
  "versionName": "1.0.0",
  "author": "Plugin Author",
  "description": "Example source plugin",
  "apiVersion": 1,
  "minHostApiVersion": 1,
  "entry": "source.js",
  "includeDirs": ["lib"],
  "capabilities": ["searchSongs", "getLyrics", "searchCovers"],
  "configFields": [
    {
      "key": "lyrics_source",
      "title": "Lyrics source",
      "summary": "Choose which lyrics source the plugin should prefer",
      "type": "dropdown",
      "required": true,
      "defaultValue": "official",
      "options": [
        { "value": "official", "label": "Official lyrics" },
        { "value": "user", "label": "User-uploaded lyrics" }
      ]
    }
  ]
}
```

## Field Details

`id` must use reverse-domain format, such as `com.example.music_source`.

`apiVersion` is used for plugin protocol compatibility. Plugins call host capabilities at runtime through `Platform`; missing capabilities are returned as runtime errors.

`capabilities` supports:

| Capability | Function |
|------------|----------|
| `searchSongs` | `searchSongs(request)` |
| `getLyrics` | `getLyrics(request)` |
| `searchCovers` | `searchCovers(request)` |

If `capabilities` is declared, source plugins must include `searchSongs`.

`includeDirs` can only reference relative directories inside the plugin package. Absolute paths, `..`, network URLs, and cross-plugin files are not allowed.

## configFields

`configFields` is the extensible manifest declaration that affects runtime behavior. User settings are passed to plugin functions through `request.config`.

Supported types:

| Type | Use |
|------|-----|
| `text` | Single-line text |
| `password` | Secret, cookie, or token |
| `number` | Number |
| `switch` | Boolean switch |
| `dropdown` | Option list |
| `textarea` | Multi-line text |
| `markdown` | Explanatory text, not saved to runtime config |

## Runtime Result Fields

Plugin function results use `fields` and `internal`:

| Field | Use |
|------|-----|
| `fields` | Host-standard metadata fields for display, matching, and candidate application |
| `internal` | Plugin-private context passed only back to the same plugin |

`fields` only accepts standard keys. Unknown keys are ignored and shown as debugging warnings. Platform IDs, hashes, tokens, lyrics IDs, and album IDs belong in `internal`.

Standard keys: `title`, `artist`, `album`, `album_artist`, `genre`, `date`, `track_number`, `disc_number`, `composer`, `lyricist`, `comment`, `lyrics`, `cover_url`, `language`, `copyright`, `rating`, `replaygain_track_gain`, `replaygain_track_peak`, `replaygain_album_gain`, `replaygain_album_peak`, `replaygain_reference_loudness`.
