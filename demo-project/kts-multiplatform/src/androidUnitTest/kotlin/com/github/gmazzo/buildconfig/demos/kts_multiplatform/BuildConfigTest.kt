package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("android", AndroidMainBuildConfig.PLATFORM)
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("anAndroidValue", AndroidMainBuildConfig.ANDROID_VALUE)
    }

}
