# Cascade for Android

A native Android (Kotlin, Jetpack Compose) version of [Cascade](https://github.com/cascade-outliner/cascade), a fast, tree-based outliner.

The outline lives on the device (SQLite) and works fully offline. There is no account, no sync and no server connection.

## Features

- Nested outline: collapse, zoom into any node, breadcrumbs back out
- Capture bar with slash commands (`/task`, `/due…`, `/zoom`) picked as chips
- Slash menu inside a node: convert, due date, indent, outdent, duplicate, copy link, delete
- Hold a bullet for the node menu; keep holding and drag to move or nest it
- Tasks and due dates, with a calendar picker
- Daily notes with a day switcher, week / month / year strip, and "due today, elsewhere"
- Search across all nodes, or create a node from the query
- Onboarding, light and dark theme
- `cascade://node/<id>` links

Not included: sync, sign-in, and the AI "Break into steps" split, which all need the server.

## Development

Requires JDK 17+ and the Android SDK (API 37).

```sh
./gradlew :app:assembleDebug        # build the APK
./gradlew :app:testDebugUnitTest    # unit and Robolectric UI tests
```

UI tests write screenshots to `app/build/screenshots/`.

## Layout

- `data/`: the outline store, ordering keys, daily notes, search and SQLite persistence. Order keys and daily node ids match the web app's.
- `commands/`: the slash command registry.
- `ui/`: Compose screens and components.

## Licenses

The Onest font is under the SIL Open Font License and the icons are from Phosphor Icons (MIT); see `licenses/`.
