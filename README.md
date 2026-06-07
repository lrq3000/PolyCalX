<p align="center"><img src="./imported_images/polycalx_icon_readme.png" title="" alt="" width="116" /></p>
<h1 align="center">PolyCalX</h1>
<p align="center">
  A colorful, concise text-based calendar widget for Android.
  <br /><a href="https://github.com/lrq3000/PolyCalX/releases/latest"><img src="/.github/badge.png" alt="Get it on GitHub" height="80" /></a>
</p>

<p align="middle">
<img title="" src="./imported_images/PolyCalX_screenshot.png" alt="" width="40%">
</p>

## Description
This is a widget for Android to display the calendar/agenda in a concise
text-based form factor.

It has a few major differences that sets it apart from other similar widgets:

* There is no repetition: when an event spans a long timeframe, it will just be shown once
  but stay in the list as long as it is still undergoing. For example, if an event
  starts on May, 1st, and ends May, 10th, then if today is May, 5th, the event will still
  be shown at the top of our list, shown as starting on May, 1st, and will stay there
  until May, 10th. This simple intuitive feature is essential in displaying the agenda
  concisely over days or weeks.
* Current day is not shown explicitly, but when an event starts happening exactly today,
  the date is shown in bold.
* Each line is color coded depending on the calendar the event is taken from.
  This is intended to be especially useful for people who must coordinate multiple calendars.

It is relatively simple, at least by Android project standards, and attempts
to use the standard practices for each part. By default, it has no calendar
permissions, and so it will be in "screenshot mode" (which was also used to
prepare the app widget preview).

Tested on Android 10 and 13 (API 33).

## Install & Settings
For the moment use the APK in the [latest GitHub Release](https://github.com/lrq3000/PolyCalX/releases/latest).

After you place the widget on your homescreen, make sure to tap on it to open the settings, and then tap on "Calendar Permission"
to allow the widget to access your calendar. Otherwise, by default, it shows fake data as an example.

You can also select which calendars you want to show (each in a different color - use another app such as Etar to modify the colors).

The date/time format can also be changed.

## Build

Build the project from the command line using the Gradle wrapper:

```bash
./gradlew assembleDebug
```

To build a release APK:

```bash
./gradlew assembleRelease
```

To clean the build directory:

```bash
./gradlew clean
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## License

Licensed under the GNU General Public License v2 (GPLv2).

This is a fork of the awesome [PolyCal](https://github.com/jasongyorog/PolyCal).

## Similar projects

* [Todo Agenda](https://github.com/andstatus/todoagenda) widget for Android - Highly configurable calendar widget, if you prefer to cluster events per day, this is the widget you need. It can even almost mimic the PolyCalX with some tweaking, but is still less concise, but more configurable.
* [MinCal](https://github.com/mvmike/min-cal-widget) widget for Android - a concise monthly calendar, no title, only events counts are displayed.
