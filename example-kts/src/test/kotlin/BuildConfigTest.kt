package com.github.gmazzo

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("example-kts", APP_NAME)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", APP_SECRET)
        assertTrue(System.currentTimeMillis() >= BUILD_TIME)
        assertTrue(FEATURE_ENABLED)
        assertArrayEquals(intArrayOf(1, 2, 3, 4), MAGIC_NUMBERS)
        assertEquals(SomeData("a", 1), MY_DATA)

        // test sourceSet buildConfig
        assertEquals("aTestValue", TEST_CONSTANT)

        // resource files
        assertEquals("file1.json", RESOURCE_FILE1_JSON.path)
        assertEquals("file2.json", RESOURCE_FILE2_JSON.path)
        assertEquals("config/local.properties", RESOURCE_CONFIG_LOCAL_PROPERTIES.path)
        assertEquals("config/prod.properties", RESOURCE_CONFIG_PROD_PROPERTIES.path)
    }

}
