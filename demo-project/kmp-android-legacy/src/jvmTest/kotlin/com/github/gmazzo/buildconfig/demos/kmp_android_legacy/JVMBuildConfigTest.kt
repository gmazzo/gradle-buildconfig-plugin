package com.github.gmazzo.buildconfig.demos.kmp_android_legacy

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
