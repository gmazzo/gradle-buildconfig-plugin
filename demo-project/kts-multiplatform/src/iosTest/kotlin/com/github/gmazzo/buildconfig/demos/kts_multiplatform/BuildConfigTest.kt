package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("ios", BuildConfig.PLATFORM)
        assertEquals(true, BuildConfig.IS_MOBILE)
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("anIOSValue", BuildConfig.IOS_VALUE)
    }

}
