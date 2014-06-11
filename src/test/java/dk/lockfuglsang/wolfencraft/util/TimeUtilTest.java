package dk.lockfuglsang.wolfencraft.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TimeUtilTest {
    public static final int SEC = 20;
    public static final int MIN = SEC*60;
    public static final int HOUR = MIN*60;
    public static final int DAY = HOUR*24;
    public static final int T2D23H59M58S = (5 * DAY) / 2 + 11 * HOUR + 59 * MIN + 58 * SEC;

    @Test
    public void testGetTicksAsTime() throws Exception {
        assertThat(TimeUtil.getTicksAsTime(0), is(nullValue()));
        assertThat(TimeUtil.getTicksAsTime(19), is(nullValue()));
        assertThat(TimeUtil.getTicksAsTime(20), is("1s"));
        assertThat(TimeUtil.getTicksAsTime(1200), is("1m"));
        assertThat(TimeUtil.getTicksAsTime(1220), is("1m1s"));
        assertThat(TimeUtil.getTicksAsTime(1400), is("1m10s"));
        assertThat(TimeUtil.getTicksAsTime((7*HOUR)/2), is("3h30m")); // 7/2 hours
        assertThat(TimeUtil.getTicksAsTime(T2D23H59M58S), is("2d23h59m58s"));
    }

    @Test
    public void testGetTimeAsTicks() throws Exception {
        assertThat(TimeUtil.getTimeAsTicks("2d23h59m58s") , is(T2D23H59M58S));
        assertThat(TimeUtil.getTimeAsTicks("2d 23h\t 59m   58s") , is(T2D23H59M58S));
    }
}