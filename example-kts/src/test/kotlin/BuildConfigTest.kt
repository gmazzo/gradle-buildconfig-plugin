package com.github.gmazzo

import org.junit.Assert.*
import org.junit.Test

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("example-kts", BuildConfig.APP_NAME)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET)
        assertTrue(System.currentTimeMillis() >= BuildConfig.BUILD_TIME)
        assertTrue(BuildConfig.FEATURE_ENABLED)
        assertArrayEquals(intArrayOf(1, 2, 3, 4), BuildConfig.MAGIC_NUMBERS)
        assertEquals(SomeData("a", 1), BuildConfig.MY_DATA)

        // test sourceSet buildConfig
        assertEquals("aTestValue", TestBuildConfig.TEST_CONSTANT)

        // resource files
        assertEquals("file1.json", BuildConfig.RESOURCE_FILE1_JSON.path)
        assertEquals("file2.json", BuildConfig.RESOURCE_FILE2_JSON.path)
        assertEquals("config/local.properties", BuildConfig.RESOURCE_CONFIG_LOCAL_PROPERTIES.path)
        assertEquals("config/prod.properties", BuildConfig.RESOURCE_CONFIG_PROD_PROPERTIES.path)
    }

}
