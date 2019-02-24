package com.github.gmazzo

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("example-kts", APP_NAME)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", APP_SECRET)
        assertTrue(System.currentTimeMillis() >= BUILD_TIME)
        assertTrue(FEATURE_ENABLED)
        assertArrayEquals(intArrayOf(1, 2, 3, 4), MAGIC_NUMBERS)

        // resource files
        assertEquals(File("file1.json"), RESOURCE_FILE1_JSON)
        assertEquals(File("file2.json"), RESOURCE_FILE2_JSON)
    }

}
