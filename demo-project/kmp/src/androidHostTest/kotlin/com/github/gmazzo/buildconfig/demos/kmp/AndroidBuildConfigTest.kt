package com.github.gmazzo.buildconfig.demos.kmp

import com.eygraber.uri.Uri
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidBuildConfigTest : CommonBuildConfigTest(
    expectedPlatform = "android",
    expectedMobile = true,
    expectedProduct = null,
    expectedUri = Uri.parse("https://api.example.com"),
) {

    @Test
    fun testAndroidOnlyValues() {
        assertEquals("anAndroidValue", BuildConfig.ANDROID_VALUE)
    }

}
