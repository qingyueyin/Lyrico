# Search Settings

Controls online search and batch matching behavior.

## Plugin Management

Go to: `Settings` → `Search Settings` → `Plugin Management`.

Manage installed search source plugins: import, enable/disable, reorder, configure, and uninstall. See [Using Plugins](../plugins.md) for full details.

## Search Limit

Limits the number of search results returned per plugin source. Higher numbers mean more candidates but may consume more bandwidth and increase image cache size.

## Prioritize Filename Matching

Affects batch matching behavior:

- **Disabled** (default): Uses the audio file's built-in title and artist tags as search keywords.
- **Enabled**: Ignores built-in tags and uses the filename as the search keyword. Useful for libraries with messy tags but well-structured filenames.

When enabled, it's recommended to set the title and artist field write rules to **Overwrite** mode so match results correctly overwrite the old tags.
