package org.lrq3000.polycalx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Context context;
    public final String TAG = "org.lrq3000.polycalx.MainActivity";
    int[] widget_ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WidgetRefreshScheduler.scheduleAll(this);

        // Check if launched from a widget tap (extras from LAUNCH_CALENDAR)
        Intent intent = getIntent();
        if (intent.hasExtra(PolyCalXWidgetProvider.EVENT_ID) || intent.hasExtra(PolyCalXWidgetProvider.EVENT_BEGIN)) {
            showTapOptionsDialog(intent);
            return;
        }

        // Otherwise show the normal configuration UI
        setContentView(R.layout.activity_main);
        LinearLayout layout = findViewById(R.id.main_layout);
        context = getApplicationContext();
        TextView tv = new TextView(context);
        tv.setEllipsize(null);
        tv.setHorizontallyScrolling(false);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        widget_ids = manager.getAppWidgetIds(ComponentName.unflattenFromString("org.lrq3000.polycalx/org.lrq3000.polycalx.PolyCalWidgetProvider"));

        if (widget_ids.length == 0) {
            tv.setText(getString(R.string.how_to_add_widget));
            tv.setTextSize((float)20.0);
            layout.addView(tv);

            Button accept_button = new Button(context);
            accept_button.setText("OK");
            accept_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                });
            layout.addView(accept_button);
        } else if (widget_ids.length == 1) {
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_ids[0]);
            settingsIntent.putExtra("from", "MainActivity");
            startActivityForResult(settingsIntent, 0);
        } else {
            tv.setText(getString(R.string.choose_widget));
            tv.setTextSize((float)16.0);
            layout.addView(tv);
            for(int i=0; i<widget_ids.length; ++i){
                Button button = new Button(context);
                button.setText(String.format("Configure widget %d", 1 + i));
                button.setTag( widget_ids[i] );
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int widget_id = (int) v.getTag();
                        Intent settingsIntent = new Intent(v.getContext(), SettingsActivity.class);
                        settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
                        settingsIntent.putExtra("from", "MainActivity");
                        startActivityForResult(settingsIntent, 0);
                    }
                });
                layout.addView(button);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        WidgetRefreshScheduler.scheduleAll(this);
        setIntent(intent);
        if (intent.hasExtra(PolyCalXWidgetProvider.EVENT_ID) || intent.hasExtra(PolyCalXWidgetProvider.EVENT_BEGIN)) {
            showTapOptionsDialog(intent);
        }
    }

    /**
     * Show a dialog letting the user choose what to do with the tapped event.
     */
    private void showTapOptionsDialog(final Intent intent) {
        final long eventId = intent.getLongExtra(PolyCalXWidgetProvider.EVENT_ID, 0);
        final long eventBegin = intent.getLongExtra(PolyCalXWidgetProvider.EVENT_BEGIN, 0);
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.d(TAG, "showTapOptionsDialog: eventId=" + eventId + " eventBegin=" + eventBegin + " widgetId=" + appWidgetId);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What to do with this event?");
        builder.setItems(new CharSequence[]{
                "Open event details",
                "Open this day in calendar",
                "Configure widget"
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Open event details
                        if (eventId > 0) {
                            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                            Intent eventIntent = new Intent(Intent.ACTION_VIEW);
                            eventIntent.setData(uri);
                            eventIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(eventIntent);
                        }
                        break;
                    case 1: // Open this day in calendar
                        if (eventBegin > 0) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(eventBegin);
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                            builder.appendPath("time");
                            ContentUris.appendId(builder, cal.getTimeInMillis());
                            Intent dayIntent = new Intent(Intent.ACTION_VIEW);
                            dayIntent.setData(builder.build());
                            dayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(dayIntent);
                        }
                        break;
                    case 2: // Configure widget
                        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                            settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                            settingsIntent.putExtra("from", "WidgetTapDialog");
                            startActivity(settingsIntent);
                        }
                        break;
                }
                finish();
            }
        });
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // requestCode is always 0.
        if (resultCode == RESULT_CANCELED && widget_ids != null && widget_ids.length == 1) { // Quit app if the user clicks "back" while in the only widget's config window
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
