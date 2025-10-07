package com.github.gmazzo.buildconfig.demos.kts_android

import com.github.gmazzo.buildconfig.demos.android.BuildConfig as AndroidBuildConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("com.github.gmazzo.buildconfig.demos.android", AndroidBuildConfig.APPLICATION_ID)

        assertEquals("kts-android", BuildConfig.APP_NAME)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET)
        assertTrue(System.currentTimeMillis() >= BuildConfig.BUILD_TIME)
        assertTrue(BuildConfig.FEATURE_ENABLED)
        assertEquals(listOf(1, 2, 3, 4), BuildConfig.MAGIC_NUMBERS.toList())
    }

    @Test
    fun testFlavoredBuildConfigProperties() {
        assertEquals(AndroidBuildConfig.DEBUG, BuildConfig.IS_DEBUG)
        assertEquals(AndroidBuildConfig.FLAVOR, BuildConfig.BRAND)
    }

}
