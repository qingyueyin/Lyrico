# Plugin Functions

Source plugins expose global JavaScript functions. Lyrico calls only the functions declared by the plugin capabilities.

## `searchSongs(request)`

Searches song metadata.

```javascript
function searchSongs(request) {
  return JSON.stringify([
    {
      id: "123",
      title: "Example Song",
      artist: "Example Artist",
      album: "Example Album",
      fields: ["title", "artist", "album"]
    }
  ])
}
```

Common request fields:

| Field | Meaning |
|-------|---------|
| `keyword` | Search keyword |
| `page` | Page number |
| `pageSize` | Requested result count |
| `config` | Saved plugin configuration |

## `getLyrics(request)`

Fetches lyrics for a selected search result or song context.

```javascript
function getLyrics(request) {
  return JSON.stringify({
    lyrics: "[00:00.00]Example lyrics",
    translatedLyrics: "",
    romanizedLyrics: ""
  })
}
```

## `searchCovers(request)`

Searches cover images.

```javascript
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

## Return Values

Return JSON strings. Keep result objects stable and include only fields the plugin truly provides.

Use `fields` to mark user-applicable metadata fields and `internal` for data used only by matching or follow-up requests.
