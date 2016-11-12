package org;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.*;
import org.mockito.internal.creation.bytebuddy.*;

import static org.hamcrest.MatcherAssert.assertThat;

public class TemporaryTest {

    @Test
    public void testInlineEnabled() {
        if (System.getenv("MOCK_MAKER").equals("mock-maker-inline")) {
            assertThat(
                "MOCK_MAKER environment variable set but not applied",
                Plugins.getMockMaker(),
                CoreMatchers.instanceOf(InlineByteBuddyMockMaker.class)
            );
        }
    }
}
