# AI Filter Service - Implementation Guide

QOP can optionally delegate text-based feature filtering to an external AI service.
When the environment variable `QOP_PVS_AI_FILTER_POST_URL` is set, QOP will POST candidate features along with the user's text query to that URL and expect back the subset of features that match.

## Configuration

Set the environment variable to the service endpoint:

```
QOP_PVS_AI_FILTER_POST_URL=https://your-service.example.com/ai-filter
```

When this variable is **not set**, QOP falls back to a simple case-insensitive substring match on feature property values.

## HTTP Contract

### Request

- **Method:** `POST`
- **Content-Type:** JSON (no explicit `Content-Type` header is set by the caller — the body is written as a UTF-8 JSON string)
- **Body:**

```json
{
  "text_filter": "pharmacy open late",
  "featureCollection": {
    "type": "FeatureCollection",
    "features": [
      {
        "type": "Feature",
        "id": "abc-123",
        "geometry": { "type": "Point", "coordinates": [16.37, 48.21] },
        "properties": {
          "name": "Apotheke am Hauptplatz",
          "category": "pharmacy",
          "opening_hours": "Mo-Sa 08:00-22:00"
        }
      }
    ]
  }
}
```

| Field | Type | Description |
|---|---|---|
| `text_filter` | string | The user's free-text search query. |
| `featureCollection` | object | A GeoJSON `FeatureCollection` containing all candidate features. |
| `featureCollection.features[]` | array | Array of GeoJSON Feature objects (see below). |

#### Feature structure

Each feature in the input array has:

| Field | Type | Description |
|---|---|---|
| `type` | string | Always `"Feature"`. |
| `id` | string | Unique identifier of the feature. |
| `geometry` | object | GeoJSON geometry (Point, Polygon, etc.). |
| `properties` | object | Key-value map of feature attributes (name, category, address, etc.). The exact keys depend on the dataset. |

### Response

The service must return a JSON response with HTTP status `200`. The response body must contain a `features` array at the top level, where each element has an `id` field:

```json
{
  "features": [
    { "id": "abc-123" },
    { "id": "def-456" }
  ]
}
```

| Field | Type | Description |
|---|---|---|
| `features` | array | The filtered subset of features that match the text query. |
| `features[].id` | string | The `id` of a matching feature (must correspond to an `id` from the request). |

Only features whose `id` appears in the response will be included in the final result. The **order** of IDs in the response is preserved (insertion order).

Additional fields in the response objects are ignored — only `/features[]/id` is read.

### Error Handling

If the service returns a non-2xx status code, QOP reads the error response body and throws a `RuntimeException` with the message:

```
ai filter problem for <url> body = <error response body>
```

The entire search request will fail in this case. There is no retry logic.

## Implementation Notes

- The service receives **all** candidate features that passed prior spatial/routing filters, sorted by bike travel time. The number of features can be significant.
- QOP logs call timing to stdout: `ai-filter: (<N> features) t_call=<ms>ms t_parse=<ms>ms <url>` — keep response times reasonable for a good user experience.
- The service is free to use any technique (LLM, embeddings, keyword matching, etc.) to determine which features match the `text_filter`.
- The response may return a subset of the input features, all of them, or none. Returning an empty `features` array is valid and results in zero results shown to the user.
