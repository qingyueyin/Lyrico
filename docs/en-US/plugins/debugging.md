# Debug Plugins Locally

Use local debugging to check manifest validation, function output, configuration handling, and host API behavior before importing the plugin into Lyrico.

## Run A Function

From the project root:

```bash
node tools/plugin-devkit/src/cli.js test ./my-plugin searchSongs --keyword "晴天"
```

If the CLI is installed as `lyrico-plugin`, the same call can be written as:

```bash
lyrico-plugin test ./my-plugin searchSongs --keyword "晴天" --page-size 5
```

## Test With Configuration

Save source settings in a JSON file:

```json
{
  "language": "ja",
  "coverSize": "large",
  "token": "example"
}
```

Pass it to the plugin call:

```bash
lyrico-plugin test ./my-plugin searchSongs --keyword "晴天" --config ./config.json
```

The values are exposed to the plugin through the request config object.

## Check Common Problems

| Symptom | What to check |
|---------|---------------|
| Plugin cannot be imported | `manifest.json` exists at the package root and contains valid identity fields |
| Function is missing | The function is declared globally and the manifest capability matches it |
| Empty result | Log the request, final URL, and parsed response shape |
| Settings not applied | Confirm `configFields` keys match the keys read by the script |
| HTTP request fails | Check method, headers, encoding, and any required cookies or tokens |

## Logging

Use host logging during development:

```javascript
Platform.log.info("MyPlugin", "search request: " + JSON.stringify(request))
Platform.log.warn("MyPlugin", "unexpected response shape")
Platform.log.error("MyPlugin", "request failed: " + err.message)
```

Keep logs useful and avoid printing private tokens.
