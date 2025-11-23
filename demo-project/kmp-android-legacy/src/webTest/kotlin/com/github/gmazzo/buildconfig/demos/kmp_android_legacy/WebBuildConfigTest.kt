package com.github.gmazzo.buildconfig.demos.kmp_android_legacy

import kotlin.test.Test
import kotlin.test.assertEquals

class WebBuildConfigTest : CommonBuildConfigTest(
    expectedPlatform = "web",
) {

    @Test
    fun testWebOnlyValues() {
        assertEquals("aWebValue", BuildConfig.WEB_VALUE)
    }

}
