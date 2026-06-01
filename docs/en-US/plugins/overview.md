# Architecture And Lifecycle

Lyrico treats plugins as source providers. A plugin returns data, while the app decides how that data is displayed, filtered, and applied to audio tags.

## Import Lifecycle

1. User imports a plugin folder or ZIP.
2. Lyrico reads and validates `manifest.json`.
3. The package is stored in the plugin directory.
4. The plugin appears in the plugin list.
5. The user enables it and configures source settings if needed.

## Execution Lifecycle

1. A search, lyrics, cover, or batch flow asks for enabled sources.
2. Lyrico builds a request object with keyword, pagination, song context, and source config.
3. The plugin runtime loads the entry script.
4. Lyrico calls a global function such as `searchSongs`.
5. The plugin returns JSON.
6. Lyrico parses the result and applies host-side policy.

## Responsibilities

| Side | Responsibility |
|------|----------------|
| Plugin | Fetch, parse, normalize, and return source data |
| Host app | Store settings, schedule searches, compare matches, show fields, and apply tags |

Plugins should not decide whether a batch operation overwrites or supplements an existing tag. Return the available data and let the host apply the selected policy.

## Runtime Constraints

- JavaScript runs in the embedded QuickJS runtime.
- Plugin functions are global functions.
- Network access goes through host APIs.
- Plugins should keep work bounded and avoid long-running loops.
- Source configuration is supplied by the host at call time.
