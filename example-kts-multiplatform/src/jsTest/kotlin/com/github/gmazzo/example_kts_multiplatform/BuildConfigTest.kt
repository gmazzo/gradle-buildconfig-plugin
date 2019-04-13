package com.github.gmazzo.example_kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("aJsValue", JsMainBuildConfig.JS_VALUE)
    }

}
