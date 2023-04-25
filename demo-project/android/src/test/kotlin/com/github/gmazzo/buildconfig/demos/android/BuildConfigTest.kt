package com.github.gmazzo.buildconfig.demos.android

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("com.github.gmazzo.buildconfig.demos.android", BuildConfig.APPLICATION_ID)

        assertEquals("android", NonAndroidBuildConfig.APP_NAME)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", NonAndroidBuildConfig.APP_SECRET)
        assertTrue(System.currentTimeMillis() >= NonAndroidBuildConfig.BUILD_TIME)
        assertTrue(NonAndroidBuildConfig.FEATURE_ENABLED)
        assertEquals(listOf(1, 2, 3, 4), NonAndroidBuildConfig.MAGIC_NUMBERS.toList())
    }

}