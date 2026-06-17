# Widget Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep the Android home-screen widget responsive to local day changes and local calendar data changes.

**Architecture:** Centralize widget refresh actions in the provider, add a small scheduler for next-midnight alarms and calendar-provider content-trigger jobs, and keep the existing `RemoteViewsFactory` as the calendar query/rendering owner.

**Tech Stack:** Android Java, AppWidgetProvider, AlarmManager, JobScheduler, CalendarContract, JUnit 4 unit tests.

---

## Task 1: Add Test Seams

**Files:**
- Modify: `app/build.gradle`
- Create: `app/src/test/java/org/lrq3000/polycalx/WidgetRefreshActionsTest.java`
- Create: `app/src/test/java/org/lrq3000/polycalx/WidgetRefreshSchedulerTest.java`

- [x] Add JUnit 4 for local JVM tests.
- [x] Add tests proving action matching does not depend on string identity.
- [x] Add tests proving next-midnight calculation uses the next local day boundary.

## Task 2: Implement Refresh Entry Points

**Files:**
- Create: `app/src/main/java/org/lrq3000/polycalx/WidgetRefreshActions.java`
- Create: `app/src/main/java/org/lrq3000/polycalx/WidgetRefreshScheduler.java`
- Modify: `app/src/main/java/org/lrq3000/polycalx/PolyCalXWidgetProvider.java`

- [x] Replace `==` broadcast action checks with null-safe `.equals` helpers.
- [x] Add a reusable `refreshEventLists(Context)` method that calls `notifyAppWidgetViewDataChanged`.
- [x] Schedule next-midnight refreshes after widget updates, appwidget enable, app launch, package replacement, boot/time/timezone/date changes, and refresh alarms.

## Task 3: Add Calendar Content Refresh

**Files:**
- Create: `app/src/main/java/org/lrq3000/polycalx/CalendarChangeRefreshJobService.java`
- Modify: `app/src/main/AndroidManifest.xml`

- [x] Register a `JobService` that refreshes widget lists when calendar provider content changes.
- [x] Schedule a content-trigger job for `CalendarContract.Events.CONTENT_URI`, `CalendarContract.Instances.CONTENT_URI`, and `CalendarContract.Calendars.CONTENT_URI`.

## Task 4: Fallback Interval And Verification

**Files:**
- Modify: `app/src/main/res/xml/appwidget_provider.xml`

- [x] Lower `android:updatePeriodMillis` to 1,800,000 milliseconds.
- [x] Run `./gradlew.bat testDebugUnitTest`.
- [x] Run `./gradlew.bat assembleDebug`.
