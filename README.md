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
- Home-screen widget with today's note and what's due today; tasks can be checked off from it
- `cascade://node/<id>` links

Not included: sync, sign-in, and the AI "Break into steps" split, which all need the server.

## Development

Requires JDK 17+ and the Android SDK (API 37).

```sh
./gradlew :app:assembleDebug        # build the APK
./gradlew testDebugUnitTest         # unit and Robolectric UI tests
```

UI tests write screenshots to `app/build/screenshots/` and `design/build/screenshots/`.

## Layout

`:app`

- `data/`: the outline store, ordering keys, daily notes, search and SQLite persistence. Order keys and daily node ids match the web app's.
- `commands/`: the slash command registry.
- `ui/`: screens. They wire the store to design system components and hold no styling of their own.
- `widget/`: the Today home-screen widget.

`:design`

The design system, the counterpart of `packages/theme` and `packages/ui` in the web repo. It knows nothing about the outline store: components take values and callbacks.

- `theme/`: tokens (`Cascade.colors`, `Space`, `Radius`, `FontSize`, `Motion`, `Elevation`) and `CascadeTheme`.
- Components: `CascadeButton`, `CascadeIconButton`, `Pill`, `Kbd`, `SectionLabel`, `Stepper`, `Calendar`, `AnchoredPopup`, `SlashMenu`, `CaptureBar`, `Sheet`, `PaletteField` / `PaletteItem`, `EmptyStateCard`.
- `outliner/`: `OutlinerRow`, `Bullet`, `Chevron`, `TaskMarker`, `OutlinerContent`, `ZoomTitle`, `Breadcrumbs`, `DaySwitcher`, `PeriodStrip`, `DropIndicator`, `DragGhost`.
- `gallery/`: every component in one list. Debug builds install it as a second launcher icon, "Cascade Design"; it also backs the Android Studio previews and the per-component screenshot tests.

To add a component: write it in `:design` using tokens only, add a section to `gallery/Gallery.kt`, and it gets a preview and light/dark screenshots.

## Licenses

The Onest font is under the SIL Open Font License and the icons are from Phosphor Icons (MIT); see `licenses/`.
