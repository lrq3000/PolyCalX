# F-Droid submission notes

PolyCalX is prepared for F-Droid as a fully source-built Android application.
This document keeps the packaging-relevant facts close to the source tree so
maintainers can update the external `fdroiddata` recipe without reverse
engineering the Gradle project.

## App identity

- Application ID: `org.lrq3000.polycalx`
- License: `GPL-2.0-only`
- Source code: `https://github.com/lrq3000/PolyCalX`
- Issue tracker: `https://github.com/lrq3000/PolyCalX/issues`
- Current release in this tree: version code `25`, version name `2.5`

## Build recipe hints

The project uses the checked-in Gradle wrapper and the Android Gradle Plugin.
F-Droid can build the release artifact from source with:

```bash
./gradlew assembleRelease
```

No proprietary services, signing credentials, checked-in keystores, or bundled
third-party binary libraries are required. Release bundles/APKs generated under
`app/release/` are intentionally ignored so the repository remains source-only
for review and reproducible rebuilds.

## Runtime permissions

PolyCalX requests `android.permission.READ_CALENDAR` so the widget can display
local calendar events selected by the user. Without this permission, it falls
back to screenshot/demo mode.

## Suggested fdroiddata metadata

```yaml
Categories:
  - Time
  - Writing
License: GPL-2.0-only
AuthorName: PolyCalX contributors
SourceCode: https://github.com/lrq3000/PolyCalX
IssueTracker: https://github.com/lrq3000/PolyCalX/issues

AutoName: PolyCalX
Description: |-
  PolyCalX is a minimalistic, text-based Android home screen widget that shows
  upcoming calendar events concisely, with per-calendar color coding and without
  repeating long-running events on every day they span.

RepoType: git
Repo: https://github.com/lrq3000/PolyCalX.git

Builds:
  - versionName: '2.5'
    versionCode: 25
    commit: v2.5
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version v%v
UpdateCheckMode: Tags
CurrentVersion: '2.5'
CurrentVersionCode: 25
```
