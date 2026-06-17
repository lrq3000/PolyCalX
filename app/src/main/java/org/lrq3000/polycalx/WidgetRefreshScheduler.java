package org.lrq3000.polycalx;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.TimeZone;

public final class WidgetRefreshScheduler {
    private static final String TAG = "WidgetRefreshScheduler";
    private static final int MIDNIGHT_REFRESH_REQUEST_CODE = 3000;
    private static final int CALENDAR_CHANGE_JOB_ID = 3001;

    private WidgetRefreshScheduler() { }

    public static void scheduleAll(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] allWidgetIDs = appWidgetManager.getAppWidgetIds(new ComponentName(context, PolyCalXWidgetProvider.class));
        if (allWidgetIDs == null || allWidgetIDs.length == 0) {
            Log.d(TAG, "No active widgets found. Skipping refresh scheduling.");
            return;
        }
        scheduleNextMidnightRefresh(context);
        scheduleCalendarChangeJob(context);
    }

    public static void cancelAll(Context context) {
        cancelNextMidnightRefresh(context);
        cancelCalendarChangeJob(context);
    }

    public static long computeNextMidnightMillis(long nowMillis, TimeZone timeZone) {
        Calendar nextMidnight = Calendar.getInstance(timeZone);
        nextMidnight.setTimeInMillis(nowMillis);
        nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);
        return nextMidnight.getTimeInMillis();
    }

    public static void scheduleNextMidnightRefresh(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.w(TAG, "Cannot schedule midnight refresh because AlarmManager is unavailable.");
            return;
        }

        long triggerAtMillis = computeNextMidnightMillis(System.currentTimeMillis(), TimeZone.getDefault());
        PendingIntent refreshIntent = createMidnightRefreshIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);

        if (canUseExactAlarms(alarmManager)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, refreshIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, refreshIntent);
            }
        } else {
            // Android 12+ may deny exact alarms. In that case, an inexact alarm plus the
            // 30-minute widget fallback is the safest permission-free path to correct bolding.
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, refreshIntent);
        }

        Log.d(TAG, "Scheduled next midnight refresh at " + triggerAtMillis);
    }

    public static void cancelNextMidnightRefresh(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent existingRefreshIntent = createMidnightRefreshIntent(context, PendingIntent.FLAG_NO_CREATE);
        if (existingRefreshIntent != null) {
            alarmManager.cancel(existingRefreshIntent);
        }
    }

    public static void scheduleCalendarChangeJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) {
            Log.w(TAG, "Cannot schedule calendar refresh job because JobScheduler is unavailable.");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            jobScheduler.cancel(CALENDAR_CHANGE_JOB_ID);
            Log.d(TAG, "Calendar refresh job canceled because READ_CALENDAR is not granted.");
            return;
        }

        JobInfo jobInfo = new JobInfo.Builder(
                CALENDAR_CHANGE_JOB_ID,
                new ComponentName(context, CalendarChangeRefreshJobService.class))
                // CalendarProvider can notify different paths depending on what changed, so
                // observe the provider root and the event-heavy paths that matter to rendering.
                .addTriggerContentUri(new JobInfo.TriggerContentUri(CalendarContract.CONTENT_URI, JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                .addTriggerContentUri(new JobInfo.TriggerContentUri(CalendarContract.Events.CONTENT_URI, JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                .addTriggerContentUri(new JobInfo.TriggerContentUri(CalendarContract.Instances.CONTENT_URI, JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                .addTriggerContentUri(new JobInfo.TriggerContentUri(CalendarContract.Calendars.CONTENT_URI, JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                .setTriggerContentUpdateDelay(0)
                .setTriggerContentMaxDelay(0)
                .build();

        int result = jobScheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Scheduled calendar content refresh job.");
        } else {
            Log.w(TAG, "Failed to schedule calendar content refresh job. Result=" + result);
        }
    }

    public static void cancelCalendarChangeJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancel(CALENDAR_CHANGE_JOB_ID);
        }
    }

    private static boolean canUseExactAlarms(AlarmManager alarmManager) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms();
    }

    private static PendingIntent createMidnightRefreshIntent(Context context, int modeFlag) {
        int flags = modeFlag | PendingIntent.FLAG_IMMUTABLE;
        Intent intent = new Intent(context, PolyCalXWidgetProvider.class);
        intent.setAction(PolyCalXWidgetProvider.REFRESH_AT_MIDNIGHT);
        return PendingIntent.getBroadcast(context, MIDNIGHT_REFRESH_REQUEST_CODE, intent, flags);
    }
}
