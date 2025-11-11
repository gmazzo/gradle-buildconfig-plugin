package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("wasmJs", BuildConfig.PLATFORM)
        assertEquals(false, BuildConfig.IS_MOBILE)
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("aWebAssemblyJsValue", BuildConfig.WASM_JS_VALUE)
    }

}
