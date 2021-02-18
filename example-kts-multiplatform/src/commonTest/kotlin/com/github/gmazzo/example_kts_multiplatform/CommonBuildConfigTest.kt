package com.github.gmazzo.example_kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonBuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
    }

}
