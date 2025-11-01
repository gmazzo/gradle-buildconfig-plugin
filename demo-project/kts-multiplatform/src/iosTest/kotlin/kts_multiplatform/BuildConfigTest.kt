package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("ios", IosMainBuildConfig.PLATFORM)
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("anIOSValue", IosMainBuildConfig.IOS_VALUE)
    }

}
