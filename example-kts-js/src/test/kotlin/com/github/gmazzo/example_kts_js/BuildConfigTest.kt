package com.github.gmazzo.example_kts_js

import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("example-kts", BuildConfig.APP_NAME)
        assertEquals("Z3JhZGxlLWphdmEtYnVpbGRjb25maWctcGx1Z2lu", BuildConfig.APP_SECRET)
        assertTrue(Date().getTime() >= BuildConfig.BUILD_TIME)
        assertTrue(BuildConfig.FEATURE_ENABLED)
        assertEquals(intArrayOf(1, 2, 3, 4), BuildConfig.MAGIC_NUMBERS)
    }

}
