package org.lrq3000.polycalx;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WidgetRefreshSchedulerTest {
    @Test
    public void nextMidnightIsStartOfFollowingLocalDay() {
        TimeZone paris = TimeZone.getTimeZone("Europe/Paris");
        Calendar now = Calendar.getInstance(paris);
        now.set(2026, Calendar.JUNE, 17, 10, 15, 30);
        now.set(Calendar.MILLISECOND, 123);

        Calendar expected = Calendar.getInstance(paris);
        expected.set(2026, Calendar.JUNE, 18, 0, 0, 0);
        expected.set(Calendar.MILLISECOND, 0);

        assertEquals(expected.getTimeInMillis(), WidgetRefreshScheduler.computeNextMidnightMillis(now.getTimeInMillis(), paris));
    }

    @Test
    public void nextMidnightIsAlwaysInTheFuture() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar now = Calendar.getInstance(utc);
        now.set(2026, Calendar.JUNE, 17, 0, 0, 0);
        now.set(Calendar.MILLISECOND, 0);

        assertTrue(WidgetRefreshScheduler.computeNextMidnightMillis(now.getTimeInMillis(), utc) > now.getTimeInMillis());
    }
}
