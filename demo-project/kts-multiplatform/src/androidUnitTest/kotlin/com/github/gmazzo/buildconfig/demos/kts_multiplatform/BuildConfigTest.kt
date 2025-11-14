package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import com.github.gmazzo.buildconfig.demos.android.BuildConfig as AndroidBuildConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("android", BuildConfig.PLATFORM)
        assertEquals(true, BuildConfig.IS_MOBILE)
        assertEquals(AndroidBuildConfig.DEBUG, BuildConfig.DEBUG)
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("anAndroidValue", BuildConfig.ANDROID_VALUE)
    }

}
