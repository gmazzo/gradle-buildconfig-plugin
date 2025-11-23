package com.github.gmazzo.buildconfig.demos.kmp_android_legacy

import com.eygraber.uri.Uri
import com.github.gmazzo.buildconfig.demos.android.BuildConfig as AndroidBuildConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidBuildConfigTest : CommonBuildConfigTest(
    expectedPlatform = "android",
    expectedMobile = true,
    expectedDebug = AndroidBuildConfig.DEBUG,
    expectedProduct = "${AndroidBuildConfig.FLAVOR_brand}Value",
    expectedUri = Uri.parse("https://${ if (AndroidBuildConfig.FLAVOR_env == "stage") "stage." else "" }api.example.com"),
) {

    @Test
    fun testAndroidOnlyValues() {
        assertEquals("anAndroidValue", BuildConfig.ANDROID_VALUE)
    }

}
