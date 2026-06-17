package org.lrq3000.polycalx;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class CalendarChangeRefreshJobService extends JobService {
    private static final String TAG = "CalendarChangeRefreshJob";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Calendar provider changed; refreshing widget event lists.");
        PolyCalXWidgetProvider.refreshEventLists(this);

        // Content-trigger jobs are one-shot after they run, so re-arm immediately to
        // keep listening for the next add, edit, delete, sync, or calendar-list change.
        WidgetRefreshScheduler.scheduleCalendarChangeJob(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
