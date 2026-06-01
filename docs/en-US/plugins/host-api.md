# Host API Reference

Plugins access host functionality through `globalThis.Platform`.

```javascript
var text = Platform.http.getText("https://example.com/api")
Platform.log.info("Example", text)
```

## HTTP

Use the HTTP API for network requests.

```javascript
var text = Platform.http.getText("https://example.com/search?q=test")

var json = Platform.http.getJson("https://example.com/search", {
  query: {
    q: "test"
  },
  headers: {
    "User-Agent": "Lyrico Plugin"
  }
})

var response = Platform.http.request({
  method: "POST",
  url: "https://example.com/api",
  headers: {
    "Content-Type": "application/json"
  },
  body: JSON.stringify({ q: "test" })
})
```

Check the current host implementation for the exact request object fields supported by your app version.

## Logging

```javascript
Platform.log.debug("Example", "debug message")
Platform.log.info("Example", "request started")
Platform.log.warn("Example", "fallback used")
Platform.log.error("Example", "request failed")
```

Avoid logging passwords, cookies, tokens, or full authenticated URLs.

## Encoding And Crypto

Host APIs may expose helpers for encoding, hashing, encryption, and compression. Prefer host helpers when a source requires a specific algorithm that is hard to implement safely in plain JavaScript.

```javascript
var encoded = Platform.encoding.base64Encode("hello")
var decoded = Platform.encoding.base64Decode(encoded)
```

## Error Handling

Wrap network calls and parsing in `try` / `catch`, then return an empty result or a structured failure that the host can display.

```javascript
function searchSongs(request) {
  try {
    var data = Platform.http.getJson("https://example.com/search", {
      query: { q: request.keyword || "" }
    })

    return JSON.stringify(data.items || [])
  } catch (err) {
    Platform.log.warn("Example", "search failed: " + err.message)
    return JSON.stringify([])
  }
}
```
