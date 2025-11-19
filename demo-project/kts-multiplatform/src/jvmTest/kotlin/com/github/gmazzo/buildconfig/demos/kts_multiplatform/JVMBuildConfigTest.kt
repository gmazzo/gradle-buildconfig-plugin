package com.github.gmazzo.buildconfig.demos.kts_multiplatform

import kotlin.test.Test
import kotlin.test.assertEquals

class JVMBuildConfigTest : CommonBuildConfigTest(
    expectedPlatform = "jvm",
) {

    @Test
    fun testJVMOnlyValues() {
        assertEquals("aJvmValue", BuildConfig.JVM_VALUE)
    }

}
