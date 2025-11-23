package com.github.gmazzo.buildconfig.demos.kmp

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
