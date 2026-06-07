package org.lrq3000.polycalx;

import android.appwidget.AppWidgetManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CalendarRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String EVENT_ID = "org.lrq3000.polycalx.EVENT_ID";
    public static final String EVENT_BEGIN = "org.lrq3000.polycalx.EVENT_BEGIN";
    private static final String TAG = "CalendarRemoteViewsFactory";
    private Context mContext;
    private Cursor mCursor;
    private int widget_id;
    private int text_size;
    private String date_format;
    private String date_format_allday;

    public CalendarRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        //widget_id = Integer.parseInt(intent.getData().getSchemeSpecificPart());
        widget_id = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        Log.d(TAG, "wID " + widget_id + " created.");
    }

    @Override
    public void onCreate() { }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        Log.d(TAG, "wID " + widget_id + " got identityToken " + identityToken);

        // SharedPreferences SharePref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String pref_file_name = String.format("org.lrq3000.polycalx.prefs_for_widget_%d", widget_id);
        SharedPreferences SharePref = mContext.getSharedPreferences(pref_file_name , 0);

        text_size = SharePref.getInt("text_size", 12);
        date_format = SharePref.getString("date_format", (String) PolyCalXDateFormats.getFormatsParseable()[0]);
        date_format_allday = SharePref.getString("date_format_allday", (String) PolyCalXDateFormats.getFormatsParseableAllday()[0]);
        Log.d(TAG, "wID " + widget_id + " got date_format='" + date_format + "' and date_format_allday='" + date_format_allday + "'");

        Set<String> EnabledCalendarIDs = SharePref.getStringSet("calendar_selection", new HashSet<String>() );
        Log.d(TAG, "wID " + widget_id + " got calendar_selection='" + EnabledCalendarIDs.toString() + "'" );

        GetCalendarEvents(EnabledCalendarIDs);
        Log.d(TAG, "wID " + widget_id + " found " + getCount() + " calendar events.");

        Binder.restoreCallingIdentity(identityToken);
    }


    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    /**
     * A class that describes a view hierarchy that can be displayed in another process. The hierarchy is inflated from a layout resource file, and this class provides some basic operations for modifying the content of the inflated hierarchy.
     */
    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG, "RemoteViews getViewAt(" + position + ")");
        if (position == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        SimpleDateFormat formatter;
        if ( 1 == mCursor.getInt(EVENT_INDEX_ALLDAY) ) {
            formatter = new SimpleDateFormat(date_format_allday, Locale.US);
        } else {
            formatter = new SimpleDateFormat(date_format, Locale.US);
        }
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        symbols.setAmPmStrings(new String[] { "am", "pm" });
        formatter.setDateFormatSymbols(symbols);
        Date StartDate = new Date( mCursor.getLong(EVENT_INDEX_BEGIN) );
        // NOTE: We intentionally do NOT set the formatter's timezone to the event's timezone.
        // The formatter defaults to the device's local timezone, which correctly converts the
        // UTC epoch millisecond timestamp (from CalendarContract.Instances.BEGIN) to the
        // user's current timezone. If we applied the event timezone here, events from other
        // timezones would display their original time instead of the user's local time.

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.appwidget_item);

        // What happens when tapping the widget?
        // Retrieve the setting
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("org.lrq3000.polycalx.prefs_for_widget_" + widget_id, Context.MODE_PRIVATE);
        boolean openSettingsOnTap = sharedPreferences.getBoolean("open_settings_on_tap", false);

        // In any case we refresh the widget's view when the user taps
        Intent intent = new Intent(mContext, PolyCalXWidgetProvider.class);
        intent.setAction("org.lrq3000.polycalx.RELOAD_EVENTS");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, widget_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.item_layout, pendingIntent);

        // Then we do another action depending on the setting
        if (openSettingsOnTap) {
            // Create an intent to open SettingsActivity
            Intent settingsIntent = new Intent(mContext, SettingsActivity.class);
            settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
            rv.setOnClickFillInIntent(R.id.item_layout, settingsIntent);
        } else {
            // Create an intent to launch the Agenda app
            Bundle extras = new Bundle();
            //extras.putLong(EVENT_ID, mCursor.getLong(EVENT_INDEX_EVENTID));
            extras.putLong(EVENT_BEGIN, mCursor.getLong(EVENT_INDEX_BEGIN));
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.item_layout, fillInIntent);
        }

        int other_color = Color.LTGRAY;

        String formattedDate = formatter.format(StartDate);
        long eventBeginTime = mCursor.getLong(EVENT_INDEX_BEGIN);
        if (isEventToday(eventBeginTime)) {
            // If the event is today, display the date/time in bold
            SpannableString spanString = new SpannableString(formattedDate);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            rv.setTextViewText(R.id.event_time, spanString);
        } else {
            // Else, if the event is any other day than today, just display it without bold
            rv.setTextViewText(R.id.event_time, formattedDate );
        }
        rv.setTextColor(R.id.event_time, other_color);
        rv.setTextViewTextSize(R.id.event_time, TypedValue.COMPLEX_UNIT_SP, text_size);

        String title_raw = mCursor.getString(EVENT_INDEX_TITLE);
        if (title_raw != null) {
            rv.setTextViewText(R.id.event_title, title_raw.replaceAll("[\\t\\n\\r]+", " "));
        } else {
            rv.setTextViewText(R.id.event_title, "");
        }
        rv.setTextColor(R.id.event_title, mCursor.getInt(EVENT_INDEX_DISPLAY_COLOR));
        rv.setTextViewTextSize(R.id.event_title, TypedValue.COMPLEX_UNIT_SP, text_size);

        String location_raw = mCursor.getString(EVENT_INDEX_LOCATION);
        if (location_raw != null) {
            rv.setTextViewText(R.id.event_location, location_raw.replaceAll("[\\t\\n\\r]+", " "));
        } else {
            rv.setTextViewText(R.id.event_location, "" );
        }
        rv.setTextColor(R.id.event_location, other_color);
        rv.setTextViewTextSize(R.id.event_location, TypedValue.COMPLEX_UNIT_SP, text_size);

        return rv;
    }

    private boolean isEventToday(long eventBeginTime) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
    
        Calendar eventDate = Calendar.getInstance();
        eventDate.setTimeInMillis(eventBeginTime);
    
        return today.get(Calendar.YEAR) == eventDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == eventDate.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return mCursor.moveToPosition(position) ? mCursor.getLong(0) : position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    public static final String[] EVENT_COLUMN_LIST = new String[] {
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_TIMEZONE,
            CalendarContract.Events.DISPLAY_COLOR,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Instances.END
    };
    private static final int EVENT_INDEX_EVENTID = 0;
    private static final int EVENT_INDEX_BEGIN = 1;
    private static final int EVENT_INDEX_ALLDAY = 2;
    private static final int EVENT_INDEX_EVENT_TIMEZONE = 3;
    private static final int EVENT_INDEX_DISPLAY_COLOR = 4;
    private static final int EVENT_INDEX_TITLE = 5;
    private static final int EVENT_INDEX_LOCATION = 6;
    private static final int EVENT_INDEX_END = 7;

    private void GetCalendarEvents(Set<String> EnabledCalendarIDs) {
        long now_ms = System.currentTimeMillis();

        Calendar cal_end = Calendar.getInstance();
        cal_end.add(Calendar.YEAR, 1);
        long end_ms = cal_end.getTimeInMillis();

        Uri.Builder instancesUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(instancesUriBuilder, now_ms );
        ContentUris.appendId(instancesUriBuilder, end_ms );
        Uri instancesUri = instancesUriBuilder.build();

        String[] selectionArgs = new String[0];
        String selectionString = "";
        if( EnabledCalendarIDs.isEmpty() ) {
            selectionString = "( " + CalendarContract.Instances.CALENDAR_ID + " != " + CalendarContract.Instances.CALENDAR_ID + " )";
        } else {
            // selectionArgs = EnabledCalendarIDs.toArray(new String[EnabledCalendarIDs.size()]);
            selectionArgs = EnabledCalendarIDs.toArray(new String[0]);

            String[] query_list = new String[selectionArgs.length];
            for(int i=0; i<selectionArgs.length; ++i){
                query_list[i] = "( " + CalendarContract.Instances.CALENDAR_ID + " = ? )";
            }
            selectionString = TextUtils.join(" OR ", query_list);
        }

        // Log.d(TAG, "Query: " + selectionString);
        // Log.d(TAG, "Args: " + TextUtils.join(",", selectionArgs));
        mCursor = mContext.getContentResolver().query(instancesUri, EVENT_COLUMN_LIST, selectionString,selectionArgs, CalendarContract.Instances.BEGIN + " ASC");
    }
}