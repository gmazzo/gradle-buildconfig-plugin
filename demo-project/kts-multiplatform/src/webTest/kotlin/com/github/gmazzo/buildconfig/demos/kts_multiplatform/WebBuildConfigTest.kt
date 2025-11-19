package com.github.gmazzo.buildconfig.demos.kts_multiplatform

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
