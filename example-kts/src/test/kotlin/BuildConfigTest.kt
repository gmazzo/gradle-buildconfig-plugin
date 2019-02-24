package com.github.gmazzo

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildConfigTest {

    @Test
    internal fun testBuildConfigProperties() {
        assertEquals("example-groovy", APP_NAME)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", APP_SECRET)
        assertTrue(System.currentTimeMillis() >= BUILD_TIME)
        assertTrue(FEATURE_ENABLED)
        assertArrayEquals(intArrayOf(1, 2, 3, 4), MAGIC_NUMBERS)
    }

}
