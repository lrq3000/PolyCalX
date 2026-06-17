package org.lrq3000.polycalx;

import android.content.Intent;

public final class WidgetRefreshActions {
    private WidgetRefreshActions() { }

    public static boolean isReloadEventsAction(String action) {
        return PolyCalXWidgetProvider.RELOAD_EVENTS.equals(action);
    }

    public static boolean isChangeSourceAction(String action) {
        return PolyCalXWidgetProvider.CHANGE_SOURCE.equals(action);
    }

    public static boolean isSchedulerWakeAction(String action) {
        return PolyCalXWidgetProvider.REFRESH_AT_MIDNIGHT.equals(action) ||
                Intent.ACTION_DATE_CHANGED.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_LOCALE_CHANGED.equals(action) ||
                Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action);
    }
}
