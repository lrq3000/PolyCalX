package org.lrq3000.polycalx;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WidgetRefreshActionsTest {
    @Test
    public void reloadEventsActionMatchesNonInternedString() {
        String actionFromBroadcast = new String(PolyCalXWidgetProvider.RELOAD_EVENTS);

        assertTrue(WidgetRefreshActions.isReloadEventsAction(actionFromBroadcast));
    }

    @Test
    public void changeSourceActionMatchesNonInternedString() {
        String actionFromBroadcast = new String(PolyCalXWidgetProvider.CHANGE_SOURCE);

        assertTrue(WidgetRefreshActions.isChangeSourceAction(actionFromBroadcast));
    }

    @Test
    public void actionMatchersRejectNull() {
        assertFalse(WidgetRefreshActions.isReloadEventsAction(null));
        assertFalse(WidgetRefreshActions.isChangeSourceAction(null));
    }
}
