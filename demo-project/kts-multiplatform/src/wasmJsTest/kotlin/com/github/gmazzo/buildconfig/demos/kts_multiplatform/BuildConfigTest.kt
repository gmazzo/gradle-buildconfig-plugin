package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildConfigTest {

    @Test
    fun testBuildConfigProperties() {
        assertEquals("wasmJs", WasmJsMainBuildConfig.PLATFORM)
        assertEquals("aCommonValue", BuildConfig.COMMON_VALUE)
        assertEquals("aWebAssemblyJsValue", WasmJsMainBuildConfig.WASM_JS_VALUE)
    }

}
