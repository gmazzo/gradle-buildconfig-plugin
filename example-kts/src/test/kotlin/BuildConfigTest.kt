package com.github.gmazzo

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    }

    @Test
    fun testBuildConfigTestProperties() {
        assertEquals("aTestValue", TestBuildConfig.TEST_CONSTANT)
    }

    @Test
    fun testResourcesConfigProperties() {
        assertEquals("aConstant", BuildResources.A_CONSTANT)
        assertEquals("file1.json", BuildResources.FILE1_JSON.path)
        assertEquals("file2.json", BuildResources.FILE2_JSON.path)
        assertEquals("config/local.properties", BuildResources.CONFIG_LOCAL_PROPERTIES.path)
        assertEquals("config/prod.properties", BuildResources.CONFIG_PROD_PROPERTIES.path)
    }

}
