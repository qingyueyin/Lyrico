# Plugin Package Structure

A Lyrico plugin is a folder or ZIP package with a manifest file and one JavaScript entry file.

```text
com.example.source/
├── manifest.json
├── source.js
└── lib/
    └── helper.js
```

## Required Files

| File | Purpose |
|------|---------|
| `manifest.json` | Declares plugin identity, entry file, capabilities, and settings |
| `source.js` | Exposes global plugin functions such as `searchSongs` |

The entry file is resolved from the `main` field in the manifest. If `main` is omitted, Lyrico uses `source.js`.

## JavaScript Format

Plugin scripts run as classic JavaScript in QuickJS. Define functions on the global scope:

```javascript
function searchSongs(request) {
  return JSON.stringify([])
}
```

ES module syntax is not supported in plugin entry files:

```javascript
export function searchSongs(request) {
  return JSON.stringify([])
}
```

## Packaging Rules

- Put `manifest.json` at the package root.
- Keep entry scripts and helper files inside the plugin package.
- Package the root files directly in the ZIP; do not put them under an extra wrapper directory unless that wrapper is the plugin directory being imported.
- Keep network credentials in `configFields`, not hard-coded in JavaScript.

## Source Control

For plugin development, keep a folder version in source control and create a ZIP only when importing or distributing the plugin.
