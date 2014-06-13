package dk.lockfuglsang.wolfencraft.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class StringUtilTest {

    @Test
    public void testStripFormatting() throws Exception {
        assertThat(StringUtil.stripFormatting("abe"), is("abe"));
        assertThat(StringUtil.stripFormatting("\u00a700\u00a711\u00a722\u00a733\u00a744\u00a755\u00a766\u00a777\u00a788\u00a799\u00a7aa\u00a7bb\u00a7cc\u00a7dd\u00a7ee\u00a7ff\u00a7kk\u00a7ll\u00a7mm\u00a7nn\u00a7oo\u00a7rr"), is("0123456789abcdefklmnor"));
    }

    @Test
    public void testAlignLeft() throws Exception {
        // Arrange
        String[] lines = new String[] {"a", "abe", "a\u00a74color", "&3codes [1234] HellM"};
        String[] expected = new String[] {"a                                                          ", "abe                                                   ", "a\u00a74color                                          ", "&3codes [1234] HellM"};

        // Act
        String[] actual = StringUtil.alignLeft(lines);

        // Assert
        assertThat(actual, is(expected));
        assertThat(lines, is(not(expected)));
    }

    @Test
    public void testAlignLeftIterations() throws Exception {
        // Arrange
        String[] lines = new String[] {"MMMMMMMMMMMMMMMMMMMM", "iiiiiiiiiiiiiiiiiiii", "i", "m"};
        String[] expected = new String[] {"MMMMMMMMMMMMMMMMMMMM", "iiiiiiiiiiiiiiiiiiii\u00a78_____________.", "i\u00a78______________________...", "m\u00a78______________________."};

        // Act
        String[] actual = StringUtil.alignLeft2(lines);

        // Assert
        assertThat(actual, is(expected));
        assertThat(lines, is(not(expected)));
    }
}