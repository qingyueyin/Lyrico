# Build A Plugin

This page shows a minimal source plugin that can search songs, fetch lyrics, and search covers.

## Manifest

Create `manifest.json`:

```json
{
  "id": "com.example.music",
  "name": "Example Music",
  "versionCode": 1,
  "versionName": "1.0.0",
  "apiVersion": 1,
  "main": "source.js",
  "capabilities": ["searchSongs", "getLyrics", "searchCovers"],
  "configFields": [
    {
      "key": "language",
      "label": "Lyrics language",
      "type": "select",
      "defaultValue": "original",
      "options": [
        { "label": "Original", "value": "original" },
        { "label": "Translated", "value": "translated" }
      ]
    }
  ]
}
```

## Entry Script

Create `source.js`:

```javascript
function searchSongs(request) {
  var keyword = request.keyword || ""

  return JSON.stringify([
    {
      id: "example-1",
      title: keyword || "Example Song",
      artist: "Example Artist",
      album: "Example Album",
      comment: "Matched by Example Music",
      fields: ["title", "artist", "album", "comment"]
    }
  ])
}

function getLyrics(request) {
  return JSON.stringify({
    lyrics: "[00:00.00]Example lyrics",
    translatedLyrics: request.config && request.config.language === "translated"
      ? "[00:00.00]Example translation"
      : ""
  })
}

function searchCovers(request) {
  return JSON.stringify([
    {
      id: "cover-1",
      url: "https://example.com/cover.jpg",
      width: 1000,
      height: 1000
    }
  ])
}
```

## Result Fields

Use `fields` to describe which metadata fields this result intentionally provides. This lets Lyrico show and apply available fields in a predictable way.

```javascript
{
  title: "Example Song",
  artist: "Example Artist",
  comment: "Source note",
  fields: ["title", "artist", "comment"]
}
```

Do not expose internal matching-only fields as user-applicable metadata. Put those names in `internal` when needed.

## Package

Zip the plugin folder or import the folder directly during development:

```text
com.example.music/
├── manifest.json
└── source.js
```
