# Anime1 Proxy

Local video server for anime1

Watch anime in local video player

## Requirements

- Java 17

## Usage

Double click full jar to open it as system tray

Or open terminal

```shell
java -jar Anime1Proxy-all.jar
```

## Url methods

- `http://127.0.0.1:8520`  
  Home page (Show help)
- `http://127.0.0.1:8520/p?url=<Url>`  
  Parse url to json detail
- `http://127.0.0.1:8520/l`  
  List all recent video categories (No external videos by default)
- `http://127.0.0.1:8520/c/<CategoryId>`  
  Open category by category id (Response m3u8 playlist)
- `http://127.0.0.1:8520/v/<PostId>`  
  Open video by post id (Response video stream)

## Example

List all video categories without external videos

```text
http://127.0.0.1:8520/l
```

List all videos under this category

```text
http://127.0.0.1:8520/p?url=https://anime1.me/category/2013年春季/進擊的巨人
```

```json5
{
  "id": "90",
  "title": "進擊的巨人",
  "url": "http://127.0.0.1:8520/c/90",
  "videos": [
    {
      "id": "1213",
      "title": "進擊的巨人 [01]",
      "url": "http://127.0.0.1:8520/v/1213"
    },
    // More videos ...
  ]
}
```

Open this category as m3u8 playlist

```text
http://127.0.0.1:8520/c/90
```

Play specific video in browser or any video player

```text
http://127.0.0.1:8520/v/1213
```

## Python version

[Anime1LocalServer](https://github.com/XFY9326/Anime1LocalServer)

Difference: Anime1Proxy add Http/2 support and faster
