package org.lrq3000.polycalx;

public final class WidgetRefreshActions {
    private WidgetRefreshActions() { }

    public static boolean isReloadEventsAction(String action) {
        return PolyCalXWidgetProvider.RELOAD_EVENTS.equals(action);
    }

    public static boolean isChangeSourceAction(String action) {
        return PolyCalXWidgetProvider.CHANGE_SOURCE.equals(action);
    }
}
