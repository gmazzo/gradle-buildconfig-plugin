package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("jvm", BuildConfig.PLATFORM)
        assertEquals(false, BuildConfig.IS_MOBILE)
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("aJvmValue", BuildConfig.JVM_VALUE)
    }

}
