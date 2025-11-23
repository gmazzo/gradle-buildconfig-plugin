package com.github.gmazzo.buildconfig.demos.kmp_android_legacy

import kotlin.test.Test
import kotlin.test.assertEquals

class iOSBuildConfigTest : CommonBuildConfigTest(
    expectedPlatform = "ios",
    expectedMobile = true,
) {

    @Test
    fun testIOSOnlyValues() {
        assertEquals("anIOSValue", BuildConfig.IOS_VALUE)
    }

}
